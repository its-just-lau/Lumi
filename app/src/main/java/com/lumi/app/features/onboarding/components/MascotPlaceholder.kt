package com.lumi.app.features.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import com.lumi.app.ui.theme.LumiColors
import androidx.annotation.RawRes
import androidx.compose.foundation.layout.aspectRatio
import com.lumi.app.mascot.MascotAnimation
import androidx.compose.ui.unit.Dp

@Composable
fun MascotPlaceholder(
    modifier: Modifier = Modifier,
    message: String? = null,
    @RawRes animationRes: Int? = null,
    animationHeight: Dp = 190.dp
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (animationRes != null) {
            MascotAnimation(
                resId = animationRes,
                modifier = Modifier
                    .height(animationHeight)
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(24.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(width = 280.dp, height = 220.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(LumiColors.Yellow.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🦎",
                    fontSize = 88.sp
                )
            }
        }

        if (message != null) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = message,
                color = LumiColors.Dark,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
