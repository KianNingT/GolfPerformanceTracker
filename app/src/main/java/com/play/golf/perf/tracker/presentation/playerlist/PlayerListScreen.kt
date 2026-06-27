package com.play.golf.perf.tracker.presentation.playerlist

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.play.golf.perf.tracker.domain.model.Player
import com.play.golf.perf.tracker.ui.theme.golfBodyMediumRegular
import com.play.golf.perf.tracker.ui.theme.golfBodySmallRegular
import com.play.golf.perf.tracker.ui.theme.golfHeadlineMedium
import com.play.golf.perf.tracker.ui.theme.golfLabelMedium
import com.play.golf.perf.tracker.ui.theme.golfLabelSmall
import com.play.golf.perf.tracker.ui.theme.golfTitleLarge
import com.play.golf.perf.tracker.ui.theme.golfTitleMedium
import com.play.golf.perf.tracker.ui.theme.green_1A3C2E
import com.play.golf.perf.tracker.ui.theme.green_52B788
import com.play.golf.perf.tracker.ui.theme.gold_C9A84C
import com.play.golf.perf.tracker.presentation.components.SearchBar
import com.play.golf.perf.tracker.presentation.components.SearchBarField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerListScreen(
    onPlayerClick: (playerId: Int) -> Unit,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean,
    viewModel: PlayerListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagedPlayers = viewModel.pagedPlayers.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }

    // Search input state — hoisted here so SearchBar can control it
    val searchInput = rememberSaveable { mutableStateOf("") }

    // Show snackbar on non-connectivity errors
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissError()
        }
    }

    // Auto-refresh pager when network restores
    LaunchedEffect(uiState.isOffline) {
        if (!uiState.isOffline) {
            pagedPlayers.refresh()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            PlayerListTopBar(
                isDarkTheme   = isDarkTheme,
                onToggleTheme = onToggleTheme,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // Offline Banner
            AnimatedVisibility(
                visible = uiState.isOffline,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut(),
            ) {
                OfflineBanner()
            }

            // Search Bar
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeHolderText  = "Search by golfer name",
                isRequestFocus   = false,
                isShowCancelIcon = true,
                inputText        = searchInput,
                onTextChange     = { query ->
                    viewModel.onSearchQueryChange(query)
                },
                onDone = { query ->
                    viewModel.onSearchQueryChange(query)
                }
            )

            // Club Filter Chips
            AnimatedVisibility(visible = uiState.availableClubs.isNotEmpty()) {
                ClubFilterChips(
                    clubs          = uiState.availableClubs,
                    selectedClub   = uiState.selectedClub,
                    onChipSelected = viewModel::onClubFilterSelected,
                )
            }

            // Player Paged List
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh    = viewModel::refreshPlayers,
                modifier     = Modifier.fillMaxSize(),
            ) {
                PlayerPagedList(
                    pagedPlayers  = pagedPlayers,
                    onPlayerClick = onPlayerClick,
                )
            }
        }
    }
}

// Top App Bar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerListTopBar(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text  = "Golf Performance",
                style = TextStyle.golfHeadlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        },
        actions = {
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector        = if (isDarkTheme) Icons.Default.LightMode
                    else Icons.Default.DarkMode,
                    contentDescription = if (isDarkTheme) "Switch to light mode"
                    else "Switch to dark mode",
                    tint               = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.green_1A3C2E,
        ),
    )
}

// Offline Banner

@Composable
private fun OfflineBanner() {
    Row(
        modifier = Modifier
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

// Club Filter Chips

@Composable
private fun ClubFilterChips(
    clubs: List<String>,
    selectedClub: String,
    onChipSelected: (String) -> Unit,
) {
    LazyRow(
        modifier            = Modifier.fillMaxWidth(),
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(clubs) { club ->
            FilterChip(
                selected = selectedClub == club,
                onClick  = { onChipSelected(club) },
                label    = {
                    Text(
                        text  = club,
                        style = TextStyle.golfLabelMedium,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor     = Color.green_1A3C2E,
                    selectedLabelColor         = Color.White,
                    selectedLeadingIconColor   = Color.White,
                ),
            )
        }
    }
}

// Paged Player List

@Composable
private fun PlayerPagedList(
    pagedPlayers: LazyPagingItems<Player>,
    onPlayerClick: (playerId: Int) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {

        when {
            // Initial full-screen loading state
            pagedPlayers.loadState.refresh is LoadState.Loading &&
                    pagedPlayers.itemCount == 0 -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color    = Color.green_1A3C2E,
                )
            }

            // Empty state after loading
            pagedPlayers.loadState.refresh is LoadState.NotLoading &&
                    pagedPlayers.itemCount == 0 -> {
                EmptyState(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                LazyColumn(
                    modifier              = Modifier.fillMaxSize(),
                    contentPadding        = PaddingValues(
                        start  = 16.dp,
                        end    = 16.dp,
                        top    = 8.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement   = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        count = pagedPlayers.itemCount,
                        key   = pagedPlayers.itemKey { it.id },
                    ) { index ->
                        pagedPlayers[index]?.let { player ->
                            PlayerCard(
                                player       = player,
                                onPlayerClick = onPlayerClick,
                            )
                        }
                    }

                    // Append loading indicator at bottom of list
                    if (pagedPlayers.loadState.append is LoadState.Loading) {
                        item {
                            Box(
                                modifier        = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color    = Color.green_1A3C2E,
                                    strokeWidth = 2.dp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Player Card

@Composable
private fun PlayerCard(
    player: Player,
    onPlayerClick: (playerId: Int) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlayerClick(player.id) },
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier             = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Avatar
            AsyncImage(
                model               = player.avatarUrl,
                contentDescription  = "${player.name} avatar",
                contentScale        = ContentScale.Crop,
                modifier            = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )

            // Player Info
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text     = player.name,
                    style    = TextStyle.golfTitleLarge,
                    color    = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text  = player.country,
                    style = TextStyle.golfBodySmallRegular,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Club badge
                ClubBadge(club = player.club)
            }

            // Key Stats
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                StatBadge(
                    label = "Avg Speed",
                    value = "${player.averageSpeed} mph",
                )
                StatBadge(
                    label = "Avg Dist",
                    value = "${player.averageDistance} yds",
                )
            }
        }
    }
}

// Club Badge

@Composable
private fun ClubBadge(club: String) {
    Box(
        modifier = Modifier
            .background(
                color = Color.green_1A3C2E.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp),
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text  = club,
            style = TextStyle.golfLabelSmall,
            color = Color.green_1A3C2E,
        )
    }
}

// Stat Badge

@Composable
private fun StatBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text  = value,
            style = TextStyle.golfTitleMedium,
            color = Color.gold_C9A84C,
        )
        Text(
            text  = label,
            style = TextStyle.golfLabelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
    }
}

// Empty State

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
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
            text  = "No players found",
            style = TextStyle.golfHeadlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text  = "Try adjusting your search or filters",
            style = TextStyle.golfBodyMediumRegular,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
    }
}