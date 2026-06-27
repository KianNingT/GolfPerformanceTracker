package com.play.golf.perf.tracker.presentation.playerdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.play.golf.perf.tracker.domain.model.PlayerDetail
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
import com.play.golf.perf.tracker.ui.theme.green_1A3C2E
import com.play.golf.perf.tracker.ui.theme.green_2D6A4F
import com.play.golf.perf.tracker.ui.theme.green_52B788

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailScreen(
    onBackClick: () -> Unit,
    onShotClick: (playerId: Int, shotId: Int) -> Unit,
    viewModel: PlayerDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show non-blocking snackbar for errors when we have cached data
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.playerDetail != null && uiState.errorMessage != null) {
            snackbarHostState.showSnackbar(uiState.errorMessage ?: "Something went wrong")
            viewModel.dismissError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            PlayerDetailTopBar(
                title       = uiState.playerDetail?.name ?: "Player Detail",
                isRefreshing = uiState.isRefreshing,
                onBackClick  = onBackClick,
                onRefresh    = viewModel::onRefresh,
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
                // Full-screen loading on first open with no cache
                uiState.isLoading && uiState.playerDetail == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color    = Color.green_1A3C2E,
                    )
                }

                // Full-screen error with no cache
                uiState.hasError && uiState.playerDetail == null -> {
                    ErrorState(
                        message   = uiState.errorMessage,
                        isOffline = uiState.isConnectivityError,
                        onRetry   = viewModel::loadPlayerDetail,
                        modifier  = Modifier.align(Alignment.Center),
                    )
                }

                // Content — with optional offline banner
                uiState.playerDetail != null -> {
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh    = viewModel::onRefresh,
                        modifier     = Modifier.fillMaxSize(),
                    ) {
                        PlayerDetailContent(
                            playerDetail       = uiState.playerDetail!!,
                            isOffline          = uiState.isOffline,
                            isStatsExpanded    = uiState.isStatsExpanded,
                            onToggleStats      = viewModel::toggleStatsExpanded,
                            onShotClick        = onShotClick,
                        )
                    }
                }
            }
        }
    }
}

// ── Top App Bar ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerDetailTopBar(
    title: String,
    isRefreshing: Boolean,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text     = title,
                style    = TextStyle.golfHeadlineMedium,
                color    = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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
        actions = {
            IconButton(
                onClick  = onRefresh,
                enabled  = !isRefreshing,
            ) {
                Icon(
                    imageVector        = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint               = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.green_1A3C2E,
        ),
    )
}

// ── Main Content ──────────────────────────────────────────────────────────────

@Composable
private fun PlayerDetailContent(
    playerDetail: PlayerDetail,
    isOffline: Boolean,
    isStatsExpanded: Boolean,
    onToggleStats: () -> Unit,
    onShotClick: (playerId: Int, shotId: Int) -> Unit,
) {
    LazyColumn(
        modifier        = Modifier.fillMaxSize(),
        contentPadding  = PaddingValues(bottom = 24.dp),
    ) {
        // ── Offline Banner ────────────────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = isOffline,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut(),
            ) {
                OfflineBanner()
            }
        }

        // ── Player Hero Section ───────────────────────────────────────────────
        item {
            PlayerHeroSection(playerDetail = playerDetail)
        }

        // ── Performance Stats Section (collapsible) ───────────────────────────
        item {
            PerformanceStatsSection(
                playerDetail    = playerDetail,
                isExpanded      = isStatsExpanded,
                onToggleExpand  = onToggleStats,
            )
        }

        // ── Career Stats Row ──────────────────────────────────────────────────
        item {
            CareerStatsRow(playerDetail = playerDetail)
        }

        // ── Shots Section Header ──────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text     = "Shot History",
                    style    = TextStyle.golfTitleLarge,
                    color    = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text  = "${playerDetail.shots.size} shots",
                    style = TextStyle.golfLabelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            }
        }

        // ── Shot Cards ────────────────────────────────────────────────────────
        items(
            items = playerDetail.shots,
            key   = { shot -> shot.shotId },
        ) { shot ->
            ShotSummaryCard(
                shot        = shot,
                playerId    = playerDetail.id,
                onShotClick = onShotClick,
                modifier    = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
    }
}

// ── Player Hero Section ───────────────────────────────────────────────────────

@Composable
private fun PlayerHeroSection(playerDetail: PlayerDetail) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.green_1A3C2E)
            .padding(24.dp),
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            // Avatar
            AsyncImage(
                model              = playerDetail.avatarUrl,
                contentDescription = "${playerDetail.name} avatar",
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.green_2D6A4F),
            )

            // Name, country, bio
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = playerDetail.name,
                    style    = TextStyle.golfHeadlineMedium,
                    color    = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text  = playerDetail.country,
                        style = TextStyle.golfBodySmallRegular,
                        color = Color.White.copy(alpha = 0.75f),
                    )
                    if (playerDetail.age != null) {
                        Text(
                            text  = "·",
                            color = Color.White.copy(alpha = 0.5f),
                        )
                        Text(
                            text  = "Age ${playerDetail.age}",
                            style = TextStyle.golfBodySmallRegular,
                            color = Color.White.copy(alpha = 0.75f),
                        )
                    }
                }
                playerDetail.bio?.let { bio ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text     = bio,
                        style    = TextStyle.golfBodySmallRegular,
                        color    = Color.White.copy(alpha = 0.65f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        // Gold accent line at the bottom of the hero
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(2.dp)
                .background(Color.gold_C9A84C)

        )
    }
}

// ── Performance Stats Section (Animated expand/collapse) ──────────────────────

@Composable
private fun PerformanceStatsSection(
    playerDetail: PlayerDetail,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessLow,
                )
            ),
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            // Header row — tap to expand/collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text  = "Performance Stats",
                    style = TextStyle.golfTitleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Icon(
                    imageVector        = if (isExpanded) Icons.Default.ExpandLess
                    else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }

            // Animated expandable content
            if (isExpanded) {
                HorizontalDivider(
                    color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    thickness = 0.5.dp,
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    // Key stats in a 2-column grid
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        StatValueItem(
                            label = "Avg Ball Speed",
                            value = "${playerDetail.averageSpeed} mph",
                        )
                        StatValueItem(
                            label = "Avg Distance",
                            value = "${playerDetail.averageDistance} yds",
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        StatValueItem(
                            label = "Total Shots",
                            value = "${playerDetail.totalShots}",
                        )
                        StatValueItem(
                            label = "Scoring Avg",
                            value = playerDetail.scoringAverage?.toString() ?: "—",
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Progress bar visualizations for percentage-based stats
                    playerDetail.greensInRegulation?.let { gir ->
                        StatProgressBar(
                            label    = "Greens in Regulation",
                            value    = gir,
                            maxValue = 100.0,
                            unit     = "%",
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    playerDetail.drivingAccuracy?.let { da ->
                        StatProgressBar(
                            label    = "Driving Accuracy",
                            value    = da,
                            maxValue = 100.0,
                            unit     = "%",
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    playerDetail.puttingAverage?.let { pa ->
                        StatProgressBar(
                            label    = "Putting Average",
                            value    = pa,
                            maxValue = 3.0,
                            unit     = " putts",
                        )
                    }
                }
            }
        }
    }
}

// ── Stat Progress Bar ─────────────────────────────────────────────────────────

@Composable
private fun StatProgressBar(
    label: String,
    value: Double,
    maxValue: Double,
    unit: String,
) {
    val progress = (value / maxValue).coerceIn(0.0, 1.0).toFloat()
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text  = label,
                style = TextStyle.golfLabelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Text(
                text  = "$value$unit",
                style = TextStyle.golfLabelMedium,
                color = Color.gold_C9A84C,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress       = { progress },
            modifier       = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color          = Color.green_52B788,
            trackColor     = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            strokeCap      = StrokeCap.Round,
        )
    }
}

// ── Stat Value Item ───────────────────────────────────────────────────────────

@Composable
private fun StatValueItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text  = value,
            style = TextStyle.golfStatValue,
            color = Color.gold_C9A84C,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text  = label,
            style = TextStyle.golfStatLabel,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}

// ── Career Stats Row ──────────────────────────────────────────────────────────

@Composable
private fun CareerStatsRow(playerDetail: PlayerDetail) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(
            containerColor = Color.green_1A3C2E.copy(alpha = 0.08f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            CareerStatItem(
                label = "Major Wins",
                value = playerDetail.majorWins?.toString() ?: "—",
            )
            VerticalDivider()
            CareerStatItem(
                label = "Total Wins",
                value = playerDetail.totalWins?.toString() ?: "—",
            )
            VerticalDivider()
            CareerStatItem(
                label = "Turned Pro",
                value = playerDetail.turnsProYear?.toString() ?: "—",
            )
        }
    }
}

@Composable
private fun CareerStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text  = value,
            style = TextStyle.golfStatValue,
            color = Color.green_1A3C2E,
        )
        Text(
            text  = label,
            style = TextStyle.golfStatLabel,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    )
}

// ── Shot Summary Card ─────────────────────────────────────────────────────────

@Composable
private fun ShotSummaryCard(
    shot: Shot,
    playerId: Int,
    onShotClick: (playerId: Int, shotId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier  = modifier
            .fillMaxWidth()
            .clickable { onShotClick(playerId, shot.shotId) },
        shape     = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Shot number badge
            Box(
                modifier         = Modifier
                    .size(36.dp)
                    .background(
                        color = Color.green_1A3C2E,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = "#${shot.shotId}",
                    style = TextStyle.golfLabelSmall,
                    color = Color.White,
                )
            }

            // Club name
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = shot.club,
                    style = TextStyle.golfTitleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text  = "Carry: ${shot.carryDistance} yds",
                    style = TextStyle.golfBodySmallRegular,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }

            // Key metrics
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text  = "${shot.ballSpeed} mph",
                    style = TextStyle.golfTitleMedium,
                    color = Color.gold_C9A84C,
                )
                Text(
                    text  = "Ball Speed",
                    style = TextStyle.golfLabelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        }
    }
}

// ── Offline Banner ────────────────────────────────────────────────────────────

@Composable
private fun OfflineBanner() {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector        = Icons.Default.WifiOff,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onErrorContainer,
            modifier           = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text  = "You're offline — showing cached data",
            style = TextStyle.golfLabelMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

// ── Error State ───────────────────────────────────────────────────────────────

@Composable
private fun ErrorState(
    message: String?,
    isOffline: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier            = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector        = if (isOffline) Icons.Default.WifiOff
            else Icons.Default.Refresh,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            modifier           = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text  = if (isOffline) "No internet connection"
            else "Failed to load player",
            style = TextStyle.golfHeadlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text  = message ?: "Please check your connection and try again",
            style = TextStyle.golfBodyMediumRegular,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier  = Modifier.clickable { onRetry() },
            shape     = RoundedCornerShape(8.dp),
            colors    = CardDefaults.cardColors(
                containerColor = Color.green_1A3C2E,
            ),
        ) {
            Text(
                text     = "Retry",
                style    = TextStyle.golfLabelMedium,
                color    = Color.White,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
        }
    }
}