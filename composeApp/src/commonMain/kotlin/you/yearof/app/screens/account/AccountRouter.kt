package you.yearof.app.screens.account

interface AccountRouter {
    suspend fun onSuccess()
    suspend fun toOpposite()
    suspend fun onCancel()
}
