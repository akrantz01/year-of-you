package you.yearof.app.camera

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

actual fun Photo.toImageBitmap(): ImageBitmap = Image.makeFromEncoded(this).toComposeImageBitmap()
