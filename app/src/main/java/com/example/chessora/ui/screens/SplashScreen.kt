package com.example.chessora.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GraphiteDark
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onSplashFinished: () -> Unit
) {
  val scaleAnim = remember { Animatable(0.75f) }
  val alphaAnim = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    alphaAnim.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
    scaleAnim.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    delay(1200)
    onSplashFinished()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.radialGradient(
          colors = listOf(GraphiteDark, ObsidianBlack),
          radius = 1200f
        )
      )
      .testTag("splash_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .scale(scaleAnim.value)
        .alpha(alphaAnim.value)
    ) {
      // 3D Royal Logo
      Box(
        modifier = Modifier
          .size(150.dp)
          .clip(CircleShape)
          .background(
            Brush.sweepGradient(
              colors = listOf(RoyalGold, Color(0xFF2E374A), RoyalGoldLight, RoyalGold)
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.ic_chessora_logo),
          contentDescription = "Chessora Logo",
          modifier = Modifier
            .size(142.dp)
            .clip(CircleShape)
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      Text(
        text = "CHESSORA 3D",
        color = RoyalGold,
        fontWeight = FontWeight.Black,
        fontSize = 32.sp,
        letterSpacing = 4.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "THE ART OF STRATEGY",
        color = TextSecondary,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        letterSpacing = 3.sp
      )
    }
  }
}
