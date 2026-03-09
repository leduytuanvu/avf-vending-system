package com.avf.vending.feature.idle.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

data class CarouselSlide(
    val title: String,
    val subtitle: String,
    val imageUrl: String? = null,
)

@Composable
fun AttractCarousel(
    slides: List<CarouselSlide>,
    autoAdvanceMs: Long = 4_000L,
    modifier: Modifier = Modifier,
) {
    if (slides.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(slides, autoAdvanceMs) {
        while (true) {
            delay(autoAdvanceMs)
            currentIndex = (currentIndex + 1) % slides.size
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        AnimatedContent(
            targetState = currentIndex,
            transitionSpec = {
                (slideInHorizontally(tween(600)) { it } + fadeIn(tween(600))).togetherWith(
                    slideOutHorizontally(tween(600)) { -it } + fadeOut(tween(600))
                )
            },
            label = "carousel",
        ) { idx ->
            val slide = slides[idx]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize().padding(32.dp),
            ) {
                Text(
                    text = slide.title,
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = slide.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Pager dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 24.dp),
        ) {
            slides.forEachIndexed { i, _ ->
                Box(
                    modifier = Modifier
                        .size(if (i == currentIndex) 10.dp else 7.dp)
                        .background(
                            if (i == currentIndex) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            CircleShape,
                        )
                )
            }
        }
    }
}
