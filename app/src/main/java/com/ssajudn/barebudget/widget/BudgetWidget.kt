package com.ssajudn.barebudget.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.ssajudn.barebudget.MainActivity
import com.ssajudn.barebudget.domain.model.DashboardSummary
import com.ssajudn.barebudget.utils.CurrencyFormatter
import dagger.hilt.android.EntryPointAccessors

class BudgetWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetDataEntryPoint::class.java
        )
        val summary = entryPoint.getDashboardSummary().invoke().getOrNull()
        val hideBalance = entryPoint.widgetPreferences().hideBalance.value

        provideContent {
            GlanceTheme {
                if (summary == null) {
                    Text(
                        modifier = GlanceModifier.padding(14.dp),
                        text = "Buka Bare Budget untuk memuat data",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 13.sp,
                        ),
                    )
                } else {
                    Content(summary, hideBalance)
                }
            }
        }
    }

    @Composable
    private fun Content(summary: DashboardSummary, hideBalance: Boolean) {
        val size = LocalSize.current
        val isWide = size.width >= 220.dp
        val isTall = size.height >= 120.dp
        val context = LocalContext.current

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // Bareuang widget — Modern Bubbly Minimalism (§5 rounded-xl, §6 tonal layering)
        // Outer: white card (surfaceContainerLowest) on cream canvas, soft outline
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .cornerRadius(24.dp)
                .clickable(actionStartActivity(mainIntent)),
            contentAlignment = Alignment.TopStart,
        ) {
            // Inner white pebble card
            Box(
                modifier = GlanceModifier.fillMaxSize().padding(8.dp).background(GlanceTheme.colors.surface).cornerRadius(20.dp),
                contentAlignment = Alignment.TopStart,
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Header: bear avatar + pill badge + net worth — strong bear identity
                    Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        // Bear avatar — honey pot B (32dp bubble, DESIGN.MD rounded-full)
                        Box(
                            modifier = GlanceModifier.background(GlanceTheme.colors.surfaceVariant).cornerRadius(50.dp).padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(com.ssajudn.barebudget.R.drawable.ic_app_logo),
                                contentDescription = "Bareuang",
                                modifier = GlanceModifier.size(28.dp).cornerRadius(14.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Box(
                            modifier = GlanceModifier.background(GlanceTheme.colors.primaryContainer).cornerRadius(50.dp).padding(horizontal = 9.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SISA RUNWAY",
                                style = TextStyle(color = GlanceTheme.colors.onPrimaryContainer, fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            )
                        }
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        // Net worth badge — secondary green + Rp coin hint
                        Box(
                            modifier = GlanceModifier.background(GlanceTheme.colors.secondaryContainer).cornerRadius(50.dp).padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⬢ ${mask(CurrencyFormatter.formatCompact(summary.netWorth), hideBalance)}",
                                style = TextStyle(color = GlanceTheme.colors.onSecondaryContainer, fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                maxLines = 1,
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    // Price display — honey primary, bear brown context
                    Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = mask(CurrencyFormatter.formatRupiah(summary.remainingBudget), hideBalance),
                            style = TextStyle(color = GlanceTheme.colors.primary, fontSize = if (isWide) 22.sp else 19.sp, fontWeight = FontWeight.Bold),
                            maxLines = 1,
                        )
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        // Mini bear paw honey coin — tertiaryContainer leaf accent
                        Box(
                            modifier = GlanceModifier.background(GlanceTheme.colors.tertiaryContainer).cornerRadius(50.dp).padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (summary.remainingBudget <= 0) "🐻‍❄️" else "🐻",
                                style = TextStyle(fontSize = 13.sp),
                            )
                        }
                    }
                    Text(
                        text = if (summary.remainingBudget <= 0) "Beruang khawatir — ayo rem dulu!" else "Beruang senang menemanimu ✨",
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 10.sp),
                    )

                    Spacer(modifier = GlanceModifier.height(6.dp))
                    RunwayBar(summary)
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    // Runway message preview (first line)
                    Text(
                        text = summary.runwayMessage.take(48),
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 10.sp),
                        maxLines = 1,
                    )

                    if (isTall || isWide) {
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            // Daily burn — outlineVariant subtle card
                            Box(
                                modifier = GlanceModifier.defaultWeight().background(GlanceTheme.colors.surfaceVariant).cornerRadius(12.dp).padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Column(modifier = GlanceModifier.fillMaxWidth()) {
                                    Text(text = "Harian", style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                                    Text(text = mask(CurrencyFormatter.formatCompact(summary.averageDailySpend), hideBalance), style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                                }
                            }
                            Spacer(modifier = GlanceModifier.width(8.dp))
                            if (summary.unpaidDueBillsSum > 0) {
                                Box(
                                    modifier = GlanceModifier.background(GlanceTheme.colors.errorContainer).cornerRadius(12.dp).padding(horizontal = 8.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "Tagihan", style = TextStyle(color = GlanceTheme.colors.onErrorContainer, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                                        Text(text = mask(CurrencyFormatter.formatCompact(summary.unpaidDueBillsSum), hideBalance), style = TextStyle(color = GlanceTheme.colors.onErrorContainer, fontSize = 11.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                                    }
                                }
                            } else if (summary.monthlyBudget > 0) {
                                Box(
                                    modifier = GlanceModifier.background(GlanceTheme.colors.tertiaryContainer).cornerRadius(12.dp).padding(horizontal = 8.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "Terpakai", style = TextStyle(color = GlanceTheme.colors.onTertiaryContainer, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                                        val percent = ((summary.totalSpent.toFloat() / summary.monthlyBudget) * 100).toInt().coerceIn(0, 999)
                                        Text(text = "$percent%", style = TextStyle(color = GlanceTheme.colors.onTertiaryContainer, fontSize = 11.sp, fontWeight = FontWeight.Bold), maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun RunwayBar(summary: DashboardSummary) {
        val progress = if (summary.monthlyBudget > 0) {
            (summary.totalSpent.toFloat() / summary.monthlyBudget).coerceIn(0f, 1f)
        } else 0f
        val overSpent = progress >= 1f || (summary.estimatedDeathDay in 1 until summary.daysPassed)
        // Thick bubbly track 8dp, rounded pill — bear progress vibe (dot simulated via color)
        LinearProgressIndicator(
            progress = progress,
            modifier = GlanceModifier.fillMaxWidth().height(8.dp).cornerRadius(50.dp),
            color = if (overSpent) GlanceTheme.colors.error else GlanceTheme.colors.primary,
            backgroundColor = GlanceTheme.colors.surfaceVariant,
        )
    }

    private fun mask(formatted: String, hidden: Boolean): String =
        if (hidden) "Rp \u2022\u2022\u2022\u2022\u2022\u2022" else formatted
}

class BudgetWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BudgetWidget()
}
