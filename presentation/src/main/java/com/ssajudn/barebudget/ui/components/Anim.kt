package com.ssajudn.barebudget.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ssajudn.barebudget.ui.theme.AppShapes
import com.ssajudn.barebudget.ui.theme.MinTouchTarget

/**
 * Punchy press / bounce effect: snaps down smoothly on press, springs back with a
 * bouncy overshoot on release.
 */
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.94f,
): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }

    LaunchedEffect(pressed) {
        if (pressed) {
            scale.animateTo(
                targetValue = pressedScale,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        } else {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

/**
 * Convenience modifier that combines [pressScale] with a clickable callback,
 * handling the [MutableInteractionSource] internally.
 */
fun Modifier.bounceClick(
    pressedScale: Float = 0.94f,
    onClick: () -> Unit,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this
        .pressScale(interactionSource, pressedScale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

/**
 * Smooth spring bounce animation for Selection Controls (DESIGN.MD §7 Selection Controls).
 * Pops slightly (scale 1.14f) and bounces back smoothly when state changes.
 */
fun Modifier.bounceToggle(
    checked: Boolean,
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(checked) {
        scale.animateTo(
            targetValue = 1.14f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            )
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }
    this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

/**
 * Shared Bouncing Button Components
 * All buttons inherit standard Material 3 styles while providing a unified,
 * punchy bounce press-and-release animation. `pressedScale` can be customized if needed.
 */

@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    pressedScale: Float = 0.94f,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.pressScale(interactionSource, pressedScale = pressedScale),
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun AppOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder(enabled),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    pressedScale: Float = 0.94f,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.pressScale(interactionSource, pressedScale = pressedScale),
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun AppTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.textShape,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    pressedScale: Float = 0.92f,
    content: @Composable RowScope.() -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.pressScale(interactionSource, pressedScale = pressedScale),
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun AppFilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.filledTonalShape,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.filledTonalButtonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    pressedScale: Float = 0.94f,
    content: @Composable RowScope.() -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.pressScale(interactionSource, pressedScale = pressedScale),
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun AppIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    pressedScale: Float = 0.88f,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.pressScale(interactionSource, pressedScale = pressedScale),
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        content = content
    )
}

// =============================================================================
// BaruangPrimaryButton — Tactile 3D CTA Button (DESIGN.MD §7 Buttons)
//
// Spec: Honey Yellow background (primaryContainer), Bear Brown text (onPrimaryContainer),
// 2px thick bottom border in a slightly darker shade simulating a 3D "plastic/tactile" look.
// When pressed the bottom border disappears and the button physically sinks (scale 0.96f),
// giving genuine tactile button feedback.
// =============================================================================

/**
 * Draws a single bottom border — used to give [BaruangPrimaryButton] its
 * signature 3D "raised plastic button" look.
 */
private fun Modifier.bottomBorder(
    color: Color,
    width: Dp = 2.dp,
): Modifier = this.drawBehind {
    val y = size.height - width.toPx() / 2f
    drawLine(
        color = color,
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = width.toPx(),
    )
}

/**
 * Bareuang's signature primary CTA button.
 *
 * - **Rest**: Honey-yellow container + bear-brown text + 2dp bottom shadow line → 3D look.
 * - **Pressed**: Bottom border removed, scale sinks to 0.96f → feels like pressing a real button.
 * - **Shape**: Full pill (`AppShapes.Pill`), minimum 48dp height (WCAG touch target).
 *
 * Use this for the single most important action on a screen (Save, Continue, Add, etc.).
 * For secondary actions use [AppFilledTonalButton] or [AppTextButton].
 */
@Composable
fun BaruangPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit,
) {
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile: 3D border color — slightly darker than primaryContainer
    val borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)

    Button(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = MinTouchTarget)
            .pressScale(interactionSource, pressedScale = 0.96f)
            .then(
                // Bottom border disappears on press, reappears on release
                if (enabled && !isPressed) Modifier.bottomBorder(borderColor) else Modifier
            ),
        enabled = enabled,
        shape = AppShapes.Pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        // No default M3 elevation — depth is communicated by the bottom border instead
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
        ),
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}
