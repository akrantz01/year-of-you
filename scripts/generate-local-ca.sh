#!/usr/bin/env bash
set -euo pipefail

# -----------------------------------------------------------------------------
# Dev TLS generator for Ktor
# Produces exactly two files in CWD:
#   1) ktor-dev.p12  (PKCS#12 keystore: alias=server)
#   2) dev-ca.crt    (CA certificate to install on devices/emulators)
#
# Requirements: openssl, bash
# Optional: keytool not required (we use PKCS#12, which Ktor supports directly)
#
# Ktor usage (application.conf):
#   ssl {
#     keyStore = ktor-dev.p12
#     keyAlias = server
#     keyStorePassword = changeit
#     privateKeyPassword = changeit
#   }
# -----------------------------------------------------------------------------

# -------- Config (override via env) ------------------------------------------
KS_FILE="${KS_FILE:-ktor-dev.p12}"     # output keystore filename
KS_PASS="${KS_PASS:-password}"         # keystore & key password (PKCS#12 uses single pass)
KS_ALIAS="${KS_ALIAS:-server}"         # key alias inside the keystore
CA_CN="${CA_CN:-Dev Root CA}"          # CA subject CN
SRV_CN="${SRV_CN:-api.dev.local}"      # server certificate CN
DAYS_CA="${DAYS_CA:-3650}"             # validity for CA
DAYS_SRV="${DAYS_SRV:-825}"            # validity for server leaf (<= 825 is common practice)
# Comma-separated SANs; auto-typed as DNS: or IP:
HOSTS_CSV="${HOSTS_CSV:-localhost,127.0.0.1,::1,$(ipconfig getifaddr en1)}"

CA_CRT="dev-ca.crt"                    # CA cert we keep; CA key is discarded

# -------- Safety: ensure only desired outputs remain -------------------------
rm -f -- "$KS_FILE" "$CA_CRT"

# -------- Temp workspace -----------------------------------------------------
WORKDIR="$(mktemp -d)"
cleanup() {
  rm -rf "$WORKDIR"
}
trap cleanup EXIT

# Files inside WORKDIR
CA_KEY="$WORKDIR/ca.key"
CA_CRT_TMP="$WORKDIR/ca.crt"
SRV_KEY="$WORKDIR/server.key"
SRV_CSR="$WORKDIR/server.csr"
SRV_CRT="$WORKDIR/server.crt"
EXTFILE="$WORKDIR/ext.cnf"

# -------- Build SAN list (DNS: / IP:) ---------------------------------------
to_san_entries() {
  IFS=',' read -ra parts <<< "$1"
  local dns_i=1 ip_i=1
  for h in "${parts[@]}"; do
    # trim
    h="${h#"${h%%[![:space:]]*}"}"; h="${h%"${h##*[![:space:]]}"}"
    [[ -z "$h" ]] && continue
    if [[ "$h" =~ ^([0-9]{1,3}\.){3}[0-9]{1,3}$ || "$h" =~ : ]]; then
      # IPv4 or IPv6
      echo "IP.$ip_i = $h"
      ip_i=$((ip_i+1))
    else
      echo "DNS.$dns_i = $h"
      dns_i=$((dns_i+1))
    fi
  done
}

SAN_BLOCK="$(to_san_entries "$HOSTS_CSV")"

cat > "$EXTFILE" <<EOF
[ v3_ca ]
basicConstraints = critical, CA:true, pathlen:0
keyUsage = critical, keyCertSign, cRLSign
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid:always,issuer

[ v3_srv ]
basicConstraints = CA:false
keyUsage = critical, digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names

[ alt_names ]
$SAN_BLOCK
EOF

# -------- Create CA (key never leaves temp) ----------------------------------
openssl req -x509 -newkey rsa:2048 -nodes \
  -subj "/CN=${CA_CN}" \
  -days "$DAYS_CA" \
  -keyout "$CA_KEY" -out "$CA_CRT_TMP" \
  -sha256 -config "$EXTFILE" -extensions v3_ca >/dev/null 2>&1

# -------- Create server key + CSR -------------------------------------------
openssl req -new -newkey rsa:2048 -nodes \
  -subj "/CN=${SRV_CN}" \
  -keyout "$SRV_KEY" -out "$SRV_CSR" \
  -sha256 >/dev/null 2>&1

# -------- Sign server cert with CA ------------------------------------------
openssl x509 -req -in "$SRV_CSR" \
  -CA "$CA_CRT_TMP" -CAkey "$CA_KEY" -CAcreateserial \
  -out "$SRV_CRT" -days "$DAYS_SRV" -sha256 \
  -extfile "$EXTFILE" -extensions v3_srv >/dev/null 2>&1

# -------- Build PKCS#12 keystore for Ktor -----------------------------------
# Contains: private key + leaf cert + CA chain, alias=$KS_ALIAS
openssl pkcs12 -export \
  -name "$KS_ALIAS" \
  -inkey "$SRV_KEY" \
  -in "$SRV_CRT" \
  -certfile "$CA_CRT_TMP" \
  -out "$KS_FILE" \
  -passout "pass:${KS_PASS}" >/dev/null 2>&1

# -------- Move only the two intended outputs --------------------------------
mv "$CA_CRT_TMP" "./$CA_CRT"

# Everything else (keys, CSR, serials, temp config) will be wiped by trap.
echo "Created:"
echo "  • $KS_FILE   (PKCS#12 keystore: alias=$KS_ALIAS, password=$KS_PASS)"
echo "  • $CA_CRT    (install this on device/emulator as a CA certificate)"
