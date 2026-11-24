package com.lumi.app.features.profile

import com.lumi.app.core.components.LumiEmoji
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lumi.app.LumiApp
import com.lumi.app.core.components.LumiBottomBar
import com.lumi.app.core.components.SkeletonBox
import com.lumi.app.ui.theme.LumiColors
import androidx.compose.ui.res.stringResource
import com.lumi.app.R

private data class ProfileAvatar(
    val emoji: String,
    @androidx.annotation.StringRes val label: Int
)

@Composable
fun ProfileScreen(
    onNavigate: (String) -> Unit
) {
    val container = LumiApp.container

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(
            container.userRepository,
            container.progressRepository,
            container.subjectRepository,
            container.taskRepository
        )
    )
    val profile by profileViewModel.data.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.load()
    }

    val userName = profile.name
    val memberSince = profile.email
    val currentStreak = profile.streakDays
    val totalXp = profile.totalXp
    val activeSubjects = profile.activeSubjects
    val gems = profile.gems
    val totalStudyHours = profile.studyHours
    val completedLessons = profile.completedLessons
    val completedTasks = profile.completedTasks

    val avatars = listOf(
        ProfileAvatar("👩🏻‍💻", R.string.avatar_student),
        ProfileAvatar("🦎", R.string.avatar_lumi),
        ProfileAvatar("📚", R.string.avatar_reading),
        ProfileAvatar("🧑🏽‍🎓", R.string.avatar_graduation),
        ProfileAvatar("🐱", R.string.avatar_kitten),
        ProfileAvatar("🦊", R.string.avatar_fox),
        ProfileAvatar("🌻", R.string.avatar_sunflower),
        ProfileAvatar("🚀", R.string.avatar_explorer)
    )

    var selectedAvatar by remember { mutableStateOf(avatars.first()) }
    var showAvatarPicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = LumiColors.Cream,
        bottomBar = {
            LumiBottomBar(
                selectedRoute = "profile",
                onItemSelected = onNavigate
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LumiColors.Cream)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ENCABEZADO DEL PERFIL
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { onNavigate("settings") },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(44.dp)
                        .background(LumiColors.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.configuracion),
                        tint = LumiColors.Dark
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 52.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(104.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(LumiColors.Yellow)
                                .clickable { showAvatarPicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = selectedAvatar.emoji,
                                fontSize = 55.sp
                            )
                        }

                        // La insignia va fuera del círculo recortado para que no se corte
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .shadow(2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(LumiColors.White)
                                .clickable { showAvatarPicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.cambiar_foto_de_perfil),
                                tint = LumiColors.Dark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (userName.isBlank()) {
                        SkeletonBox(
                            modifier = Modifier
                                .width(150.dp)
                                .height(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        SkeletonBox(
                            modifier = Modifier
                                .width(110.dp)
                                .height(14.dp)
                        )
                    } else {
                        Text(
                            text = userName,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold,
                            color = LumiColors.Dark
                        )
                        Text(
                            text = memberSince,
                            fontSize = 13.sp,
                            color = LumiColors.Dark.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // RESUMEN
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Text(
                    text = stringResource(R.string.resumen),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileStatCard(
                            emoji = if (currentStreak > 0) "🔥" else "lumi:FlameOff",
                            value = currentStreak.toString(),
                            label = stringResource(R.string.dias_de_racha_2),
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatCard(
                            emoji = "⭐",
                            value = "%,d".format(totalXp),
                            label = stringResource(R.string.xp_totales),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileStatCard(
                            emoji = "📚",
                            value = activeSubjects.toString(),
                            label = stringResource(R.string.materias_activas),
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatCard(
                            emoji = "💎",
                            value = "%,d".format(gems),
                            label = stringResource(R.string.gemas),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // PROGRESO ACADÉMICO
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Text(
                    text = stringResource(R.string.mi_progreso),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = LumiColors.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        ProfileProgressRow(
                            emoji = "⏱️",
                            label = stringResource(R.string.tiempo_de_estudio),
                            value = "$totalStudyHours horas"
                        )
                        HorizontalDivider(color = LumiColors.WarmGray)
                        ProfileProgressRow(
                            emoji = "📖",
                            label = stringResource(R.string.lecciones_completadas),
                            value = completedLessons.toString()
                        )
                        HorizontalDivider(color = LumiColors.WarmGray)
                        ProfileProgressRow(
                            emoji = "☑️",
                            label = stringResource(R.string.tareas_finalizadas),
                            value = completedTasks.toString()
                        )
                    }
                }
            }

            // ACTIVIDAD SEMANAL: VISUAL DE EJEMPLO
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Text(
                    text = stringResource(R.string.actividad_semanal),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = LumiColors.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.tu_constancia_de_los_ultimos),
                            fontSize = 12.sp,
                            color = LumiColors.Dark.copy(alpha = 0.7f)
                        )
                        val dayLabels = listOf("L", "M", "M", "J", "V", "S", "D")
                        // Barras proporcionales al día con más minutos de la semana
                        val maxMinutes = (profile.weeklyMinutes.maxOrNull() ?: 0).coerceAtLeast(1)
                        val activity = profile.weeklyMinutes.map {
                            (it.toFloat() / maxMinutes).coerceIn(0.04f, 1f)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            dayLabels.forEachIndexed { index, day ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(25.dp)
                                            .height(78.dp),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight(activity[index])
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(LumiColors.Yellow)
                                        )
                                    }
                                    Text(
                                        text = day,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = LumiColors.Dark.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // LOGROS
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Text(
                    text = stringResource(R.string.logros),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AchievementItem(
                        emoji = "🌱",
                        title = stringResource(R.string.primeros_pasos),
                        subtitle = stringResource(R.string.primera_leccion),
                        unlocked = completedLessons >= 1,
                        modifier = Modifier.weight(1f)
                    )
                    AchievementItem(
                        emoji = "🔥",
                        title = stringResource(R.string.constancia),
                        subtitle = stringResource(R.string.racha_de_7_dias),
                        unlocked = currentStreak >= 7,
                        modifier = Modifier.weight(1f)
                    )
                    AchievementItem(
                        emoji = "🏆",
                        title = stringResource(R.string.estudioso),
                        subtitle = stringResource(R.string.s_50_lecciones),
                        unlocked = completedLessons >= 50,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }
    }

    if (showAvatarPicker) {
        AlertDialog(
            onDismissRequest = { showAvatarPicker = false },
            containerColor = LumiColors.White,
            title = {
                Text(
                    text = stringResource(R.string.elige_tu_foto_de_perfil),
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.selecciona_una_de_las_opciones),
                        fontSize = 13.sp,
                        color = LumiColors.Dark.copy(alpha = 0.7f)
                    )
                    avatars.chunked(4).forEach { avatarRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            avatarRow.forEach { avatar ->
                                val isSelected = avatar == selectedAvatar
                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) LumiColors.Cream
                                            else LumiColors.White
                                        )
                                        .clickable {
                                            selectedAvatar = avatar
                                            showAvatarPicker = false
                                        }
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(avatar.emoji, fontSize = 30.sp)
                                    Text(
                                        text = stringResource(avatar.label),
                                        fontSize = 10.sp,
                                        color = LumiColors.Dark,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarPicker = false }) {
                    Text(stringResource(R.string.cerrar), color = LumiColors.YellowStrong)
                }
            }
        )
    }
}

@Composable
private fun ProfileStatCard(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LumiColors.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LumiEmoji(emoji, size = 29.dp)
            Spacer(modifier = Modifier.width(9.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = LumiColors.Dark.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ProfileProgressRow(
    emoji: String,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LumiEmoji(emoji, size = 26.dp)
        Spacer(modifier = Modifier.width(11.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            color = LumiColors.Dark
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = LumiColors.Dark
        )
    }
}

@Composable
private fun AchievementItem(
    emoji: String,
    title: String,
    subtitle: String,
    unlocked: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.alpha(if (unlocked) 1f else 0.45f),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = LumiColors.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 13.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LumiEmoji(emoji, size = 36.dp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LumiColors.Dark,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = LumiColors.Dark.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )
        }
    }
}
