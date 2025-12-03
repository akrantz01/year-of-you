package you.yearof.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val SkeletonShimmerAngle = -30.0 / 180.0 * PI
private val SkeletonShimmerCos = cos(SkeletonShimmerAngle).toFloat()
private val SkeletonShimmerSin = sin(SkeletonShimmerAngle).toFloat()

@Composable
fun SkeletonText(
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    widthFraction: Float = 1f,
) {
    val transition = rememberInfiniteTransition()
    val translation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        ),
        start = Offset(
            x = translation - 200f * SkeletonShimmerCos,
            y = -(translation - 200f) * SkeletonShimmerSin,
        ),
        end = Offset(
            x = translation * SkeletonShimmerCos,
            y = -translation * SkeletonShimmerSin,
        ),
    )

    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(style.fontSize.value.dp * 1.2f)
            .background(brush = brush, shape = RoundedCornerShape(4.dp))
    )
}
