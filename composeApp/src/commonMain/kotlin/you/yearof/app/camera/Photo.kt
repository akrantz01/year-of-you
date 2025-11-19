package you.yearof.app.camera

import androidx.compose.ui.graphics.ImageBitmap

/**
 * A JPEG encoded photo with included EXIF data
 */
typealias Photo = ByteArray

/**
 * Take a photo and turn it into a bitmap that can be displayed.
 */
expect fun Photo.toImageBitmap(): ImageBitmap
