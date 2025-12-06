package you.yearof.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
            .setOnExitAnimationListener { provider ->
                provider.iconView
                    .animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction { provider.remove() }
                    .start()
            }

        enableEdgeToEdge()

        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}
