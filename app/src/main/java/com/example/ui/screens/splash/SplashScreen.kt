// File: app/src/main/java/com/example/ui/screens/splash/SplashScreen.kt
package com.example.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepPurple
import com.example.ui.theme.LightPurple
import com.example.ui.theme.Pink
import com.example.ui.theme.Turquoise

@Composable
fun SplashScreen(
    onGetStartedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Elegant floating animations for retro-gaming feel
    val infiniteTransition = rememberInfiniteTransition(label = "splash_floating")
    
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating_y"
    )

    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsing_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFEF7FF),
                        Color(0xFFF3EDF7),
                        Color(0xFFECE6F0)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background elements representing gaming sprites (Vibrant Palette)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            // Soft lavender circle
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (-30).dp, y = 50.dp)
                    .background(LightPurple.copy(alpha = 0.15f), RoundedCornerShape(75.dp))
            )
            // Soft pink diamond
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 20.dp, y = (-80).dp)
                    .background(Pink.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // Animated Controller / Gaming Icon
            Box(
                modifier = Modifier
                    .offset(y = floatAnim.dp)
                    .scale(scaleAnim)
                    .size(140.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(LightPurple.copy(alpha = 0.25f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Gamepad,
                    contentDescription = "PlayPals Game Controller",
                    tint = DeepPurple,
                    modifier = Modifier.size(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PlayPals App Brand Title
            Text(
                text = "PlayPals",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DeepPurple,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("app_title")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = "Vibrant Casual Multiplayer Hub",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DeepPurple.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Play / Get Started Action Button
            Button(
                onClick = onGetStartedClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepPurple,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp
                ),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(56.dp)
                    .testTag("get_started_button")
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GET STARTED",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                }
            }
        }
    }
}
