package com.ssajudn.barebudget.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssajudn.barebudget.presentation.R
import com.ssajudn.barebudget.ui.components.BaruangPrimaryButton
import com.ssajudn.barebudget.ui.components.AppTextButton
import com.ssajudn.barebudget.ui.theme.AppShapes
import com.ssajudn.barebudget.ui.theme.Spacing
import com.ssajudn.barebudget.utils.LanguageManager
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val description: String,
    val imageRes: Int,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isStarting by remember { mutableStateOf(false) }

    val pages = listOf(
        OnboardingPageData(stringResource(R.string.onboarding_slide1_title), stringResource(R.string.onboarding_slide1_desc), R.drawable.img_onboarding_expense),
        OnboardingPageData(stringResource(R.string.onboarding_slide2_title), stringResource(R.string.onboarding_slide2_desc), R.drawable.img_onboarding_runway),
        OnboardingPageData(stringResource(R.string.onboarding_slide3_title), stringResource(R.string.onboarding_slide3_desc), R.drawable.img_onboarding_bills),
        OnboardingPageData(stringResource(R.string.notif_perm_title), stringResource(R.string.notif_perm_desc), R.drawable.img_onboarding_notif)
    )

    val notifPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { _ ->
        isStarting = true
        viewModel.startLocalSession { onFinishOnboarding() }
    }

    var currentLang by remember { mutableStateOf(LanguageManager.getCurrentLanguageCode(context)) }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isLastPage = pagerState.currentPage == pages.size - 1

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = Spacing.ScreenHorizontal, vertical = Spacing.Medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top skip — label-md 14/600 +0.05em
            Row(modifier = Modifier.fillMaxWidth().height(44.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                if (!isLastPage) {
                    AppTextButton(onClick = { coroutineScope.launch { pagerState.animateScrollToPage(pages.size - 1) } }) {
                        Text(stringResource(R.string.tour_skip), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f).fillMaxWidth()) { pageIndex ->
                if (pageIndex == pages.size - 1) {
                    NotificationStep(
                        data = pages[pageIndex], isLoading = isStarting,
                        onAllowClick = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            else { isStarting = true; viewModel.startLocalSession { onFinishOnboarding() } }
                        },
                        onLaterClick = { isStarting = true; viewModel.startLocalSession { onFinishOnboarding() } }
                    )
                } else {
                    OnboardingSlide(
                        data = pages[pageIndex], isFirstSlide = pageIndex == 0,
                        currentLang = currentLang,
                        onLanguageChange = { code -> currentLang = code; LanguageManager.setLanguage(context, code) }
                    )
                }
            }

            if (!isLastPage) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = Spacing.Medium), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    // Bubbly dots — selected expands to pill 24dp honey
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        repeat(pages.size) { index ->
                            val isCurrent = pagerState.currentPage == index
                            Box(
                                modifier = Modifier.size(if (isCurrent) 22.dp else 8.dp, 8.dp).clip(CircleShape)
                                    .background(if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                            )
                        }
                    }
                    BaruangPrimaryButton(onClick = { coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }, modifier = Modifier.height(48.dp)) {
                        Text(stringResource(R.string.tour_next), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingSlide(
    data: OnboardingPageData, isFirstSlide: Boolean = false, currentLang: String = "", onLanguageChange: (String) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.Small), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        // Illustration — white pebble card rounded-xl 48dp, soft shadow tertiary tint §6
        Surface(
            modifier = Modifier.shadow(12.dp, MaterialTheme.shapes.extraLarge, spotColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f)),
            shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainerLowest, tonalElevation = 0.dp
        ) {
            Image(painter = painterResource(id = data.imageRes), contentDescription = data.title, modifier = Modifier.size(220.dp).padding(12.dp).clip(MaterialTheme.shapes.extraLarge))
        }
        Spacer(modifier = Modifier.height(Spacing.StackLg))

        if (isFirstSlide) {
            Surface(shape = AppShapes.Pill, color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.padding(bottom = Spacing.Medium)) {
                Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    LanguageManager.SUPPORTED_LANGUAGES.forEach { (code, label) ->
                        val isSelected = currentLang == code
                        Surface(
                            shape = AppShapes.Pill,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            onClick = { onLanguageChange(code) }
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (isSelected) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(text = label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium), color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        Text(text = data.title, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center), color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = data.description, style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center, lineHeight = 26.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun NotificationStep(data: OnboardingPageData, isLoading: Boolean, onAllowClick: () -> Unit, onLaterClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.Small), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(
            modifier = Modifier.shadow(12.dp, MaterialTheme.shapes.extraLarge, spotColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f)),
            shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainerLowest
        ) {
            Image(painter = painterResource(id = data.imageRes), contentDescription = data.title, modifier = Modifier.size(220.dp).padding(12.dp).clip(MaterialTheme.shapes.extraLarge))
        }
        Spacer(modifier = Modifier.height(Spacing.StackLg))
        // Honey pill badge
        Surface(shape = AppShapes.Pill, color = MaterialTheme.colorScheme.secondaryContainer) {
            Text(text = "Stay on track", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = data.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center), color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = data.description, style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center, lineHeight = 22.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(28.dp))
        BaruangPrimaryButton(onClick = onAllowClick, enabled = !isLoading, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
            else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(stringResource(R.string.notif_perm_allow), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        AppTextButton(onClick = onLaterClick, enabled = !isLoading, modifier = Modifier.fillMaxWidth().height(44.dp)) {
            Text(stringResource(R.string.notif_perm_later), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
