package com.lumi.app.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumi.app.core.components.LumiBottomBar
import com.lumi.app.ui.theme.LumiColors

private data class ProfileAvatar(
    val emoji: String,
    val label: String
)

@Composable
fun ProfileScreen(
    onNavigate: (String) -> Unit
) {
    // Datos provisionales: posteriormente se obtendrán del perfil y del progreso real.
    val userName = "Laura Chávez"
    val memberSince = "Miembro desde 2026"
    val currentStreak = 20
    val totalXp = 77452
    val activeSubjects = 5
    val gems = 853
    val totalStudyHours = 86
    val completedLessons = 42
    val completedTasks = 68

    val avatars = listOf(
        ProfileAvatar("👩🏻‍💻", "Estudiante"),
        ProfileAvatar("🦎", "Lumi"),
        ProfileAvatar("📚", "Lectura"),
        ProfileAvatar("🧑🏽‍🎓", "Graduación"),
        ProfileAvatar("🐱", "Gatito"),
        ProfileAvatar("🦊", "Zorrito"),
        ProfileAvatar("🌻", "Girasol"),
        ProfileAvatar("🚀", "Explorador")
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
                        contentDescription = "Configuración",
                        tint = LumiColors.Dark
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 52.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                            .background(LumiColors.Yellow)
                            .clickable { showAvatarPicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedAvatar.emoji,
                            fontSize = 55.sp
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(LumiColors.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Cambiar foto de perfil",
                                tint = LumiColors.Dark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

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

            // RESUMEN
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Text(
                    text = "Resumen",
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
                            emoji = "🔥",
                            value = currentStreak.toString(),
                            label = "Días de racha",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatCard(
                            emoji = "⭐",
                            value = "%,d".format(totalXp),
                            label = "XP totales",
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
                            label = "Materias activas",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatCard(
                            emoji = "💎",
                            value = "%,d".format(gems),
                            label = "Gemas",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // PROGRESO ACADÉMICO
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Text(
                    text = "Mi progreso",
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
                            label = "Tiempo de estudio",
                            value = "$totalStudyHours horas"
                        )
                        HorizontalDivider(color = LumiColors.WarmGray)
                        ProfileProgressRow(
                            emoji = "📖",
                            label = "Lecciones completadas",
                            value = completedLessons.toString()
                        )
                        HorizontalDivider(color = LumiColors.WarmGray)
                        ProfileProgressRow(
                            emoji = "☑️",
                            label = "Tareas finalizadas",
                            value = completedTasks.toString()
                        )
                    }
                }
            }

            // ACTIVIDAD SEMANAL: VISUAL DE EJEMPLO
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Text(
                    text = "Actividad semanal",
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
                            text = "Tu constancia de los últimos 7 días",
                            fontSize = 12.sp,
                            color = LumiColors.Dark.copy(alpha = 0.7f)
                        )
                        val dayLabels = listOf("L", "M", "M", "J", "V", "S", "D")
                        val activity = listOf(0.75f, 0.45f, 0.9f, 0.6f, 1f, 0.3f, 0.7f)

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
                    text = "Logros",
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
                        title = "Primeros pasos",
                        subtitle = "Primera lección",
                        modifier = Modifier.weight(1f)
                    )
                    AchievementItem(
                        emoji = "🔥",
                        title = "Constancia",
                        subtitle = "Racha de 7 días",
                        modifier = Modifier.weight(1f)
                    )
                    AchievementItem(
                        emoji = "🏆",
                        title = "Estudioso",
                        subtitle = "50 lecciones",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // OPCIONES
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Text(
                    text = "Cuenta",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )

                ProfileOptionRow(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    title = "Editar información",
                    onClick = { onNavigate("edit-profile") }
                )
                ProfileOptionRow(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    title = "Notificaciones",
                    onClick = { onNavigate("notifications") }
                )
                ProfileOptionRow(
                    icon = { Icon(Icons.Default.Security, contentDescription = null) },
                    title = "Privacidad y cuenta",
                    onClick = { onNavigate("settings") }
                )
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
                    text = "Elige tu foto de perfil",
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Selecciona una de las opciones de Lumi.",
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
                                        text = avatar.label,
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
                    Text("Cerrar", color = LumiColors.YellowStrong)
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
            Text(emoji, fontSize = 25.sp)
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
        Text(emoji, fontSize = 23.sp)
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = LumiColors.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 13.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 31.sp)
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

@Composable
private fun ProfileOptionRow(
    icon: @Composable () -> Unit,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LumiColors.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(
                LocalContentColor provides LumiColors.Dark
            ) {
                icon()
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = LumiColors.Dark
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = LumiColors.Dark.copy(alpha = 0.6f)
            )
        }
    }
}
