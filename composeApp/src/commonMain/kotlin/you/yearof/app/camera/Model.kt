package you.yearof.app.camera

/**
 * The position of the camera on the device
 */
enum class CameraPosition {
    Front, Back;

    fun opposite() = when (this) {
        Front -> Back
        Back -> Front
    }
}

/**
 * The status of the camera flash
 */
enum class FlashMode {
    Off, Auto, On;

    fun next() = when (this) {
        Off -> Auto
        Auto -> On
        On -> Off
    }
}

/**
 * The camera lens to use on devices with multiple cameras.
 */
enum class CameraLens {
    UltraWide, Wide
}

/**
 * Configuration for camera
 */
data class CameraConfiguration(
    val position: CameraPosition = CameraPosition.Back,
    val flashMode: FlashMode = FlashMode.Auto,
    val lens: CameraLens = CameraLens.Wide
)

/**
 * A JPEG encoded photo with included EXIF data
 */
typealias Photo = ByteArray
