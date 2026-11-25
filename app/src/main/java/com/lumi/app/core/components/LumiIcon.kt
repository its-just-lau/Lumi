package com.lumi.app.core.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumi.app.R

/** Iconos propios de Lumi (estilo "gomita brillante"), sin recuadro. */
enum class LumiIconKind(@DrawableRes val res: Int) {
    Flame(R.drawable.lumi_icon_flame),
    Star(R.drawable.lumi_icon_star),
    Gem(R.drawable.lumi_icon_gem),
    Book(R.drawable.lumi_icon_book),
    Task(R.drawable.lumi_icon_task),
    Calendar(R.drawable.lumi_icon_calendar),
    Trophy(R.drawable.lumi_icon_trophy),
    Shield(R.drawable.lumi_icon_shield),
    AddSubject(R.drawable.lumi_icon_add_subject),
    AddTask(R.drawable.lumi_icon_add_task),
    Sunrise(R.drawable.lumi_icon_sunrise),
    Moon(R.drawable.lumi_icon_moon),
    GemsPile(R.drawable.lumi_icon_gems_pile),
    GemsBag(R.drawable.lumi_icon_gems_bag),
    GemsChest(R.drawable.lumi_icon_gems_chest),
    Potion(R.drawable.lumi_icon_potion),
    Promo(R.drawable.lumi_icon_promo),
    Clock(R.drawable.lumi_icon_clock),
    Steps(R.drawable.lumi_icon_steps),
    Cards(R.drawable.lumi_icon_cards),
    Quiz(R.drawable.lumi_icon_quiz),
    Notebook(R.drawable.lumi_icon_notebook),
    FlameOff(R.drawable.lumi_icon_flame_off)
}

@Composable
fun LumiIcon(kind: LumiIconKind, size: Dp = 24.dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(kind.res),
        contentDescription = null,
        modifier = modifier.size(size)
    )
}

private fun kindForEmoji(emoji: String): LumiIconKind? = when (emoji.trim()) {
    "🌅" -> LumiIconKind.Sunrise
    "▤" -> LumiIconKind.Cards
    "❔" -> LumiIconKind.Quiz
    "📄" -> LumiIconKind.Notebook
    "lumi:FlameOff" -> LumiIconKind.FlameOff
    "🌙" -> LumiIconKind.Moon
    "🧪" -> LumiIconKind.Potion
    "🎟️" -> LumiIconKind.Promo
    "⏱️" -> LumiIconKind.Clock
    "🌱" -> LumiIconKind.Steps
    "lumi:GemsPile" -> LumiIconKind.GemsPile
    "lumi:GemsBag" -> LumiIconKind.GemsBag
    "lumi:GemsChest" -> LumiIconKind.GemsChest
    "🔥" -> LumiIconKind.Flame
    "⭐" -> LumiIconKind.Star
    "💎" -> LumiIconKind.Gem
    "📚", "📖", "📘" -> LumiIconKind.Book
    "☑️", "✅" -> LumiIconKind.Task
    "📅", "🗓️" -> LumiIconKind.Calendar
    "🏆" -> LumiIconKind.Trophy
    "🛡️" -> LumiIconKind.Shield
    else -> null
}

/**
 * Muestra el icono de Lumi equivalente al emoji; si no hay equivalente
 * (p. ej. 🦎 o los avatares) muestra el emoji tal cual.
 */
@Composable
fun LumiEmoji(emoji: String, size: Dp = 24.dp, modifier: Modifier = Modifier) {
    val kind = kindForEmoji(emoji)
    if (kind != null) {
        LumiIcon(kind, size, modifier)
    } else {
        Text(text = emoji, fontSize = (size.value * 0.85f).sp, modifier = modifier)
    }
}
