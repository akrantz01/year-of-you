package you.yearof.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
