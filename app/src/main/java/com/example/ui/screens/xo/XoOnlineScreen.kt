// File: app/src/main/java/com/example/ui/screens/xo/XoOnlineScreen.kt
package com.example.ui.screens.xo

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.PlayPalsApplication
import com.example.ui.theme.PlayerXColor
import com.example.ui.theme.PlayerOColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XoOnlineScreen(
    roomId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current.applicationContext as PlayPalsApplication
    val viewModel: OnlineXoViewModel = viewModel(
        factory = OnlineXoViewModel.provideFactory(
            roomId,
            context.container.authRepository,
            context.container.gameRepository
        )
    )

    val roomState by viewModel.roomState.collectAsState()
    val isLeaving by viewModel.isLeaving.collectAsState()

    // Handle system back gesture
    BackHandler {
        viewModel.leaveMatch()
    }

    // Trigger navigation back when leaving room completes
    LaunchedEffect(isLeaving) {
        if (isLeaving) {
            onNavigateBack()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Multiplayer Match: $roomId",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = viewModel::leaveMatch,
                        modifier = Modifier
                            .testTag("xo_online_back_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Leave Match",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (roomState == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Connecting to matchmaking server...",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            val room = roomState!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Players & Status Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                        .testTag("players_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(
                                    text = room.playerXName.ifEmpty { "Host" },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PlayerXColor,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Player X (Host)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "VS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(
                                    text = room.playerOName.ifEmpty { "Waiting..." },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PlayerOColor,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Player O (Guest)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                        Spacer(modifier = Modifier.height(12.dp))

                        // Match Turn Indicator
                        val statusText: String
                        val statusColor: Color
                        when (room.status) {
                            "WAITING" -> {
                                statusText = "Waiting for Guest to Join Room..."
                                statusColor = MaterialTheme.colorScheme.primary
                            }
                            "PLAYING" -> {
                                if (viewModel.isMyTurn) {
                                    val symbol = viewModel.playerSymbol
                                    statusText = "YOUR TURN ($symbol)"
                                    statusColor = if (symbol == "X") PlayerXColor else PlayerOColor
                                } else {
                                    val oppSymbol = if (viewModel.playerSymbol == "X") "O" else "X"
                                    statusText = "OPPONENT'S TURN ($oppSymbol): Waiting for ${viewModel.opponentName}..."
                                    statusColor = if (oppSymbol == "X") PlayerXColor else PlayerOColor
                                }
                            }
                            "WON_X" -> {
                                statusText = if (viewModel.isPlayerX) "YOU WON!" else "${room.playerXName} WON!"
                                statusColor = PlayerXColor
                            }
                            "WON_O" -> {
                                statusText = if (viewModel.isPlayerO) "YOU WON!" else "${room.playerOName} WON!"
                                statusColor = PlayerOColor
                            }
                            "DRAW" -> {
                                statusText = "IT'S A DRAW!"
                                statusColor = MaterialTheme.colorScheme.primary
                            }
                            else -> {
                                statusText = "Match Ended"
                                statusColor = MaterialTheme.colorScheme.error
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(statusColor.copy(alpha = 0.12f))
                                .border(1.dp, statusColor.copy(alpha = 0.25f), RoundedCornerShape(100.dp))
                                .padding(horizontal = 24.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = statusText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (room.status == "WAITING") {
                    // Show a gorgeous, dedicated waiting card for the host
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 16.dp)
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                            .testTag("waiting_for_guest_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            
                            Text(
                                text = "Waiting for Guest to Join...",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            
                            Text(
                                text = "Share this 5-digit Room Code with your opponent:",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            
                            // High-contrast, large code container
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 32.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = roomId,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    letterSpacing = 4.sp
                                )
                            }
                            
                            Text(
                                text = "The game will automatically start as soon as they join with this code.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                } else {
                    // Classic 3x3 Game Board Grid
                    Box(
                        modifier = Modifier
                            .size(320.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (row in 0..2) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    for (col in 0..2) {
                                        val cellIndex = row * 3 + col
                                        val cellValue = room.board[cellIndex]
                                        
                                        val isClickable = cellValue.isEmpty() && 
                                                room.status == "PLAYING" && 
                                                viewModel.isMyTurn

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(MaterialTheme.colorScheme.surface)
                                                .border(
                                                    width = 1.dp,
                                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                                .clickable(enabled = isClickable) { 
                                                    viewModel.makeMove(cellIndex) 
                                                }
                                                .testTag("online_cell_$cellIndex"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            androidx.compose.animation.AnimatedVisibility(
                                                visible = cellValue.isNotEmpty(),
                                                enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                                                exit = fadeOut()
                                            ) {
                                                Text(
                                                    text = cellValue,
                                                    fontSize = 42.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (cellValue == "X") PlayerXColor else PlayerOColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Rematch / Game Over Feedback Panel
                val isGameOver = room.status == "WON_X" || room.status == "WON_O" || room.status == "DRAW" || room.status == "ABANDONED"
                AnimatedVisibility(
                    visible = isGameOver,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                            .testTag("online_result_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val overText = when (room.status) {
                                "WON_X" -> if (viewModel.isPlayerX) "You achieved victory!" else "${room.playerXName} won the match!"
                                "WON_O" -> if (viewModel.isPlayerO) "You achieved victory!" else "${room.playerOName} won the match!"
                                "DRAW" -> "Even match! Well played both."
                                "ABANDONED" -> "Opponent abandoned the match."
                                else -> "Game Over"
                            }

                            Text(
                                text = overText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (room.status == "ABANDONED") {
                                Button(
                                    onClick = viewModel::leaveMatch,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("online_return_button")
                                        .minimumInteractiveComponentSize()
                                ) {
                                    Text(
                                        text = "Return to Menu",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onError
                                    )
                                }
                            } else {
                                Button(
                                    onClick = viewModel::rematch,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("online_rematch_button")
                                        .minimumInteractiveComponentSize()
                                ) {
                                    Text(
                                        text = "Play Again (Rematch)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
