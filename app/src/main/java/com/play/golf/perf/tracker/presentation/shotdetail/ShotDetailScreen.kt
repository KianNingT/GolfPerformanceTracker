package com.play.golf.perf.tracker.presentation.shotdetail

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.play.golf.perf.tracker.domain.model.Shot
import com.play.golf.perf.tracker.ui.theme.golfBodyMediumRegular
import com.play.golf.perf.tracker.ui.theme.golfBodySmallRegular
import com.play.golf.perf.tracker.ui.theme.golfHeadlineMedium
import com.play.golf.perf.tracker.ui.theme.golfLabelMedium
import com.play.golf.perf.tracker.ui.theme.golfLabelSmall
import com.play.golf.perf.tracker.ui.theme.golfStatLabel
import com.play.golf.perf.tracker.ui.theme.golfStatValue
import com.play.golf.perf.tracker.ui.theme.golfTitleLarge
import com.play.golf.perf.tracker.ui.theme.golfTitleMedium
import com.play.golf.perf.tracker.ui.theme.gold_C9A84C
import com.play.golf.perf.tracker.ui.theme.gold_E8C96D
import com.play.golf.perf.tracker.ui.theme.green_1A3C2E
import com.play.golf.perf.tracker.ui.theme.green_2D6A4F
import com.play.golf.perf.tracker.ui.theme.green_52B788

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShotDetailScreen(
    onBackClick: () -> Unit,
    viewModel: ShotDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            ShotDetailTopBar(
                shotId      = uiState.shot?.shotId,
                onBackClick = onBackClick,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color    = Color.green_1A3C2E,
                    )
                }

                uiState.hasError || uiState.shot == null -> {
                    ShotErrorState(
                        message     = uiState.errorMessage,
                        onBackClick = onBackClick,
                        modifier    = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    ShotDetailContent(shot = uiState.shot!!)
                }
            }
        }
    }
}

// Top App Bar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShotDetailTopBar(
    shotId: Int?,
    onBackClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text  = if (shotId != null) "Shot #$shotId" else "Shot Detail",
                style = TextStyle.golfHeadlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint               = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.green_1A3C2E,
        ),
    )
}

// Main Content

@Composable
private fun ShotDetailContent(shot: Shot) {
    // Animate the entire content fading + sliding in on first composition
    val contentAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(
            targetValue   = 1f,
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .alpha(contentAlpha.value)
            .verticalScroll(rememberScrollState()),
    ) {
        // Hero Banner
        ShotHeroBanner(shot = shot)

        Spacer(modifier = Modifier.height(16.dp))

        // Primary Metrics
        SectionHeader(title = "Flight Metrics")
        FlightMetricsCard(shot = shot)

        Spacer(modifier = Modifier.height(12.dp))

        // Distance Metrics
        SectionHeader(title = "Distance Breakdown")
        DistanceMetricsCard(shot = shot)

        Spacer(modifier = Modifier.height(12.dp))

        // Spin & Angle Metrics
        SectionHeader(title = "Spin & Trajectory")
        SpinTrajectoryCard(shot = shot)

        Spacer(modifier = Modifier.height(24.dp))

        // Animated metric bars
        SectionHeader(title = "Performance Gauges")
        AnimatedMetricBars(shot = shot)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Shot Hero Banner

@Composable
private fun ShotHeroBanner(shot: Shot) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.green_1A3C2E),
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Club icon circle
            Box(
                modifier         = Modifier
                    .size(64.dp)
                    .background(
                        color = Color.green_2D6A4F,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = "🏌️",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = androidx.compose.ui.unit.TextUnit(28f, androidx.compose.ui.unit.TextUnitType.Sp)),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text  = shot.club,
                style = TextStyle.golfHeadlineMedium,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text  = "Shot #${shot.shotId}",
                style = TextStyle.golfBodySmallRegular,
                color = Color.White.copy(alpha = 0.7f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Headline stat — total distance
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.Bottom,
            ) {
                Text(
                    text  = "${shot.distance}",
                    style = TextStyle.golfStatValue.copy(
                        fontSize = androidx.compose.ui.unit.TextUnit(
                            40f,
                            androidx.compose.ui.unit.TextUnitType.Sp
                        )
                    ),
                    color = Color.gold_C9A84C,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text     = "yds",
                    style    = TextStyle.golfTitleMedium,
                    color    = Color.gold_E8C96D,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            Text(
                text  = "Total Distance",
                style = TextStyle.golfStatLabel,
                color = Color.White.copy(alpha = 0.6f),
            )
        }

        // Gold accent bottom line
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(2.dp)
                .background(Color.gold_C9A84C)
        )
    }
}

// Flight Metrics Card

@Composable
private fun FlightMetricsCard(shot: Shot) {
    MetricCard {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            MetricItem(
                label = "Ball Speed",
                value = "${shot.ballSpeed}",
                unit  = "mph",
                color = Color.gold_C9A84C,
            )
            MetricDivider()
            MetricItem(
                label = "Launch Angle",
                value = "${shot.launchAngle}",
                unit  = "°",
                color = Color.green_52B788,
            )
            MetricDivider()
            MetricItem(
                label = "Peak Height",
                value = "${shot.peakHeight}",
                unit  = "ft",
                color = Color.gold_C9A84C,
            )
        }
    }
}

// Distance Metrics Card

@Composable
private fun DistanceMetricsCard(shot: Shot) {
    MetricCard {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            MetricItem(
                label = "Total Distance",
                value = "${shot.distance}",
                unit  = "yds",
                color = Color.gold_C9A84C,
            )
            MetricDivider()
            MetricItem(
                label = "Carry Distance",
                value = "${shot.carryDistance}",
                unit  = "yds",
                color = Color.green_52B788,
            )
            MetricDivider()
            MetricItem(
                label = "Roll",
                value = "${(shot.distance - shot.carryDistance).let {
                    kotlin.math.round(it * 10) / 10.0
                }}",
                unit  = "yds",
                color = Color.gold_C9A84C,
            )
        }
    }
}

// Spin & Trajectory Card

@Composable
private fun SpinTrajectoryCard(shot: Shot) {
    MetricCard {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            MetricItem(
                label = "Spin Rate",
                value = "${shot.spinRate}",
                unit  = "rpm",
                color = Color.gold_C9A84C,
            )
            MetricDivider()
            MetricItem(
                label = "Landing Angle",
                value = "${shot.landingAngle}",
                unit  = "°",
                color = Color.green_52B788,
            )
        }
    }
}

// Animated Metric Bars

/**
 * Animates each progress bar from 0 to its target value on entry.
 * This is the primary animation deliverable for Screen 3.
 */
@Composable
private fun AnimatedMetricBars(shot: Shot) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            AnimatedStatBar(
                label     = "Ball Speed",
                value     = shot.ballSpeed,
                maxValue  = 220.0, // typical pro max ~220 mph
                unit      = "mph",
                color     = Color.gold_C9A84C,
                delayMs   = 0,
            )
            Spacer(modifier = Modifier.height(14.dp))

            AnimatedStatBar(
                label     = "Carry Distance",
                value     = shot.carryDistance,
                maxValue  = 350.0, // typical pro max carry
                unit      = "yds",
                color     = Color.green_52B788,
                delayMs   = 100,
            )
            Spacer(modifier = Modifier.height(14.dp))

            AnimatedStatBar(
                label     = "Peak Height",
                value     = shot.peakHeight,
                maxValue  = 150.0,
                unit      = "ft",
                color     = Color.gold_C9A84C,
                delayMs   = 200,
            )
            Spacer(modifier = Modifier.height(14.dp))

            AnimatedStatBar(
                label     = "Launch Angle",
                value     = shot.launchAngle,
                maxValue  = 30.0,
                unit      = "°",
                color     = Color.green_52B788,
                delayMs   = 300,
            )
        }
    }
}

@Composable
private fun AnimatedStatBar(
    label: String,
    value: Double,
    maxValue: Double,
    unit: String,
    color: Color,
    delayMs: Int,
) {
    val animatedProgress = remember { Animatable(0f) }
    val targetProgress   = (value / maxValue).coerceIn(0.0, 1.0).toFloat()

    LaunchedEffect(value) {
        // Stagger each bar using delay so they animate in sequence
        kotlinx.coroutines.delay(delayMs.toLong())
        animatedProgress.animateTo(
            targetValue   = targetProgress,
            animationSpec = tween(
                durationMillis = 800,
                easing         = FastOutSlowInEasing,
            ),
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text  = label,
                style = TextStyle.golfLabelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Text(
                text  = "$value $unit",
                style = TextStyle.golfLabelMedium,
                color = color,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        androidx.compose.material3.LinearProgressIndicator(
            progress       = { animatedProgress.value },
            modifier       = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color          = color,
            trackColor     = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
            strokeCap      = StrokeCap.Round,
        )
    }
}

// Shared Composables

@Composable
private fun MetricCard(content: @Composable () -> Unit) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(modifier = Modifier.padding(vertical = 16.dp)) {
            content()
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    unit: String,
    color: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.padding(horizontal = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text  = value,
                style = TextStyle.golfStatValue,
                color = color,
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text     = unit,
                style    = TextStyle.golfLabelSmall,
                color    = color.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 3.dp),
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text      = label,
            style     = TextStyle.golfStatLabel,
            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MetricDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(48.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text     = title,
        style    = TextStyle.golfTitleLarge,
        color    = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
    Spacer(modifier = Modifier.height(4.dp))
}

// Error State

@Composable
private fun ShotErrorState(
    message: String?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier            = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text  = "⛳",
            style = MaterialTheme.typography.displayLarge,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text  = "Shot not found",
            style = TextStyle.golfHeadlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text      = message ?: "Please go back and try again.",
            style     = TextStyle.golfBodyMediumRegular,
            color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier  = Modifier.clickable { onBackClick() },
            shape     = RoundedCornerShape(8.dp),
            colors    = CardDefaults.cardColors(
                containerColor = Color.green_1A3C2E,
            ),
        ) {
            Text(
                text     = "Go Back",
                style    = TextStyle.golfLabelMedium,
                color    = Color.White,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
    }
}