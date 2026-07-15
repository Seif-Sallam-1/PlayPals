// File: app/src/main/java/com/example/ui/screens/xo/XoOfflineScreen.kt
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.PlayerXColor
import com.example.ui.theme.PlayerOColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XoOfflineScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: OfflineXoViewModel = viewModel()
    
    // Explicit imports and proper delegation
    val board by viewModel.board.collectAsState()
    val isXTurn by viewModel.isXTurn.collectAsState()
    val winner by viewModel.winner.collectAsState()
    val winningLine by viewModel.winningLine.collectAsState()

    val scoreX by viewModel.scoreX.collectAsState()
    val scoreO by viewModel.scoreO.collectAsState()
    val scoreDraws by viewModel.scoreDraws.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pass & Play XO",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .testTag("xo_offline_back_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::resetScores,
                        modifier = Modifier
                            .testTag("reset_scores_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Scores",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Real-time Scoreboard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scoreboard_container"),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScoreCard(
                    playerName = "Player X",
                    score = scoreX,
                    color = PlayerXColor,
                    modifier = Modifier.weight(1f)
                )
                ScoreCard(
                    playerName = "Draws",
                    score = scoreDraws,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                ScoreCard(
                    playerName = "Player O",
                    score = scoreO,
                    color = PlayerOColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Turn Indicator Caption
            AnimatedContent(
                targetState = isXTurn,
                transitionSpec = {
                    slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut()
                },
                label = "TurnIndicator"
            ) { xTurn ->
                val symbol = if (xTurn) "X" else "O"
                val color = if (xTurn) PlayerXColor else PlayerOColor
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(color.copy(alpha = 0.12f))
                        .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Player $symbol's Turn",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

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
                                val cellValue = board[cellIndex]
                                val isWinningCell = winningLine?.contains(cellIndex) == true
                                
                                val cellColor = when {
                                    isWinningCell && winner == "X" -> PlayerXColor
                                    isWinningCell && winner == "O" -> PlayerOColor
                                    else -> Color.Transparent
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (isWinningCell) cellColor.copy(alpha = 0.18f)
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .border(
                                            width = if (isWinningCell) 2.5.dp else 1.dp,
                                            color = if (isWinningCell) cellColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable { viewModel.makeMove(cellIndex) }
                                        .testTag("cell_$cellIndex"),
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

            Spacer(modifier = Modifier.height(32.dp))

            // Game State Feedback Panel (Overlay or Banner)
            AnimatedVisibility(
                visible = winner != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                        .testTag("game_result_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val resultText = when (winner) {
                            "X" -> "Victory to Player X!"
                            "O" -> "Victory to Player O!"
                            "DRAW" -> "It's an even match!"
                            else -> ""
                        }
                        val resultColor = when (winner) {
                            "X" -> PlayerXColor
                            "O" -> PlayerOColor
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Text(
                            text = resultText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = resultColor,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = viewModel::resetBoard,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("rematch_button")
                                .minimumInteractiveComponentSize()
                        ) {
                            Text(
                                text = "Play Again",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            // Margin bottom for smooth scroll
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ScoreCard(
    playerName: String,
    score: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = playerName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = score.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
