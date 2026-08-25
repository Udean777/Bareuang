package com.ssajudn.bareuang.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    onSplashFinished: (String) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val scale = remember { Animatable(0.6f) }
    val overallAlpha = remember { Animatable(1f) }
    val alpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) { scale.animateTo(1f, spring(dampingRatio = 0.65f, stiffness = 300f)) }
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        textAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        delay(700.milliseconds)
        overallAlpha.animateTo(0f, tween(350, easing = FastOutSlowInEasing))
        onSplashFinished(viewModel.computeStartDestination())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(overallAlpha.value)
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Soft warm blob behind logo — tonal layering §6 (surfaceContainerLowest on cream)
        Box(
            modifier = Modifier
                .size(220.dp)
                .alpha(alpha.value * 0.45f)
                .background(MaterialTheme.colorScheme.surfaceContainerLow, CircleShape),
            contentAlignment = Alignment.Center
        ) {}

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = Spacing.ScreenHorizontal)
        ) {
            // Logo hero — white pebble card (rounded-xl 48dp) + soft shadow
            Surface(
                modifier = Modifier
                    .size(124.dp)
                    .scale(scale.value)
                    .alpha(alpha.value)
                    .shadow(
                        elevation = 16.dp,
                        shape = MaterialTheme.shapes.extraLarge,
                        spotColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                        ambientColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                    ),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                tonalElevation = 0.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = stringResource(R.string.splash_brand),
                        modifier = Modifier
                            .size(88.dp)
                            .clip(MaterialTheme.shapes.large)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Brand — headline-xl 40/800 per DESIGN.MD
            Text(
                text = stringResource(R.string.splash_brand),
                style = MaterialTheme.typography.displayLarge, // headline-xl
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(textAlpha.value)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(textAlpha.value)
            )
            Spacer(modifier = Modifier.height(10.dp))
            // Pill tagline
            Surface(
                shape = AppShapes.Pill,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.alpha(textAlpha.value)
            ) {
                Text(
                    text = stringResource(R.string.settings_footer_tagline),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        // Footer — label-md uppercase
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .alpha(textAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "ꕤ", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.splash_runway_tracker),
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.7.sp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.splash_bubbly_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
