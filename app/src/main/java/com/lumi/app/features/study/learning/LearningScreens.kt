package com.lumi.app.features.study.learning

import androidx.activity.compose.BackHandler
import com.lumi.app.mascot.MascotAnimation
import com.lumi.app.mascot.LumiAnimations
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumi.app.core.components.LumiButton
import com.lumi.app.data.models.learning.FlashcardResponse
import com.lumi.app.ui.theme.LumiColors
import kotlin.math.roundToInt
import androidx.compose.ui.res.stringResource
import com.lumi.app.R

@Composable
private fun LearningHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.volver),
                tint = LumiColors.Dark
            )
        }

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = LumiColors.Dark
        )
    }
}

@Composable
private fun EmptyContent(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            color = LumiColors.Dark.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

// ---------------------------------------------------------------- TARJETAS

@Composable
fun FlashcardsScreen(
    cards: List<FlashcardResponse>,
    onReview: (flashcardId: String, result: String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var index by remember { mutableIntStateOf(0) }
    var flipped by remember { mutableStateOf(false) }
    var misses by remember { mutableIntStateOf(0) }

    Scaffold(containerColor = LumiColors.Cream) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            LearningHeader(stringResource(R.string.tarjetas), onBack)

            if (cards.isEmpty()) {
                EmptyContent(stringResource(R.string.aun_no_tienes_tarjetas_sube))
                return@Column
            }

            if (index >= cards.size) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (misses > 0) {
                        MascotAnimation(
                            resId = LumiAnimations.CARD_MISS,
                            modifier = Modifier
                                .height(220.dp)
                                .aspectRatio(9f / 16f)
                                .clip(RoundedCornerShape(18.dp))
                        )
                    } else {
                        MascotAnimation(
                            resId = LumiAnimations.CARD_HIT,
                            modifier = Modifier
                                .height(220.dp)
                                .aspectRatio(9f / 16f)
                                .clip(RoundedCornerShape(18.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.repasaste_tarjetas, cards.size),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LumiColors.Dark
                    )
                }

                LumiButton(
                    text = stringResource(R.string.repasar_de_nuevo),
                    onClick = {
                        index = 0
                        flipped = false
                        misses = 0
                    }
                )
                return@Column
            }

            val card = cards[index]

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.tarjeta_de, index + 1, cards.size),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = LumiColors.Dark.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable { flipped = !flipped },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (flipped) LumiColors.Yellow else LumiColors.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (flipped) "RESPUESTA" else "PREGUNTA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LumiColors.Dark.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (flipped) card.answer else card.question,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LumiColors.Dark,
                        textAlign = TextAlign.Center
                    )

                    if (!flipped) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.toca_para_ver_la_respuesta),
                            fontSize = 13.sp,
                            color = LumiColors.Dark.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (flipped) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReviewButton(stringResource(R.string.olvide), "forgotten", Modifier.weight(1f)) {
                        onReview(card.id, it); misses++; index++; flipped = false
                    }
                    ReviewButton(stringResource(R.string.dificil), "difficult", Modifier.weight(1f)) {
                        onReview(card.id, it); misses++; index++; flipped = false
                    }
                    ReviewButton(stringResource(R.string.recorde), "remembered", Modifier.weight(1f)) {
                        onReview(card.id, it); index++; flipped = false
                    }
                }
            } else {
                LumiButton(text = stringResource(R.string.mostrar_respuesta), onClick = { flipped = true })
            }
        }
    }
}

@Composable
private fun ReviewButton(
    text: String,
    result: String,
    modifier: Modifier,
    onClick: (String) -> Unit
) {
    Button(
        onClick = { onClick(result) },
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (result == "remembered") LumiColors.Dark else LumiColors.White,
            contentColor = if (result == "remembered") LumiColors.White else LumiColors.Dark
        )
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

// ----------------------------------------------------------- CUESTIONARIOS

@Composable
fun QuizScreen(
    quizzes: List<QuizItem>,
    quizState: QuizSubmitState,
    onSubmit: (QuizItem, Map<String, String>) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    var current by remember { mutableStateOf<QuizItem?>(null) }
    var questionIndex by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    fun backToList() {
        current = null
        questionIndex = 0
        selected = emptyMap()
        onReset()
    }

    BackHandler {
        if (current != null) backToList() else onBack()
    }

    Scaffold(containerColor = LumiColors.Cream) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            LearningHeader(stringResource(R.string.cuestionario)) {
                if (current != null) backToList() else onBack()
            }

            val quizItem = current

            // ----- Lista de cuestionarios -----
            if (quizItem == null) {
                if (quizzes.isEmpty()) {
                    EmptyContent(stringResource(R.string.aun_no_tienes_cuestionarios_sube))
                    return@Column
                }

                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    quizzes.forEach { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { current = item },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = LumiColors.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = item.quiz.title ?: stringResource(R.string.cuestionario),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LumiColors.Dark
                                )
                                Text(
                                    text = stringResource(R.string.preguntas, item.materialTitle, item.quiz.questions.orEmpty().size),
                                    fontSize = 12.sp,
                                    color = LumiColors.Dark.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }
                }
                return@Column
            }

            val questions = quizItem.quiz.questions.orEmpty()

            // ----- Resultado -----
            if (quizState is QuizSubmitState.Done) {
                val result = quizState.result
                val score = (result.attempt?.score ?: 0.0).roundToInt()
                val feedback = result.feedback.orEmpty()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = LumiColors.Yellow)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            MascotAnimation(
                                resId = LumiAnimations.QUIZ_DONE,
                                modifier = Modifier
                                    .height(180.dp)
                                    .aspectRatio(9f / 16f)
                                    .clip(RoundedCornerShape(18.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.tu_calificacion), fontSize = 13.sp, color = LumiColors.Dark)
                            Text(
                                text = stringResource(R.string.s_100, score),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = LumiColors.Dark
                            )
                        }
                    }

                    questions.forEachIndexed { i, question ->
                        val item = feedback.firstOrNull { it.questionId == question.id }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = LumiColors.White)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = stringResource(R.string.text_2, i + 1, question.question),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LumiColors.Dark
                                )
                                if (item != null) {
                                    Text(
                                        text = if (item.correct) stringResource(R.string.correcta) else stringResource(R.string.incorrecta),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = LumiColors.Dark
                                    )
                                    item.explanation?.let {
                                        Text(
                                            text = it,
                                            fontSize = 13.sp,
                                            color = LumiColors.Dark.copy(alpha = 0.75f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                LumiButton(text = stringResource(R.string.terminar), onClick = { backToList() })
                return@Column
            }

            if (questions.isEmpty()) {
                EmptyContent(stringResource(R.string.este_cuestionario_no_tiene_preguntas))
                return@Column
            }

            // ----- Pregunta actual -----
            val question = questions[questionIndex.coerceIn(0, questions.lastIndex)]
            val isLast = questionIndex >= questions.lastIndex
            val sending = quizState is QuizSubmitState.Loading

            Text(
                text = stringResource(R.string.pregunta_de, questionIndex + 1, questions.size),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = LumiColors.Dark.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = question.question,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )

                question.options.orEmpty().forEach { option ->
                    val isSelected = selected[question.id] == option.optionId

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) LumiColors.YellowStrong else LumiColors.White,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable(enabled = !sending) {
                                selected = selected + (question.id to option.optionId)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) LumiColors.Yellow else LumiColors.White
                        )
                    ) {
                        Text(
                            text = option.text,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 15.sp,
                            color = LumiColors.Dark
                        )
                    }
                }

                if (quizState is QuizSubmitState.Error) {
                    Text(
                        text = quizState.message,
                        fontSize = 13.sp,
                        color = Color(0xFFD32F2F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (questionIndex > 0) {
                    OutlinedButton(
                        onClick = { questionIndex-- },
                        modifier = Modifier.height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !sending
                    ) {
                        Text(stringResource(R.string.anterior), color = LumiColors.Dark, fontWeight = FontWeight.Bold)
                    }
                }

                LumiButton(
                    text = when {
                        sending -> "Enviando..."
                        isLast -> stringResource(R.string.enviar_respuestas)
                        else -> stringResource(R.string.siguiente)
                    },
                    enabled = !sending && selected[question.id] != null,
                    onClick = {
                        if (isLast) onSubmit(quizItem, selected) else questionIndex++
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ----------------------------------------------------------------- LECCIÓN

@Composable
fun LessonScreen(
    item: LessonItem,
    onProgress: (percent: Int) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val scrollState = rememberScrollState()
    val lesson = item.lesson

    // El avance es el porcentaje de la lección que el usuario ya recorrió
    val percent by remember {
        derivedStateOf {
            if (scrollState.maxValue <= 0) 100
            else (scrollState.value * 100f / scrollState.maxValue).roundToInt()
        }
    }

    LaunchedEffect(percent) { onProgress(percent) }

    Scaffold(containerColor = LumiColors.Cream) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            LearningHeader(stringResource(R.string.leccion), onBack)

            LinearProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = LumiColors.YellowStrong,
                trackColor = LumiColors.WarmGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = lesson.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = LumiColors.Dark
                )
                Text(
                    text = item.materialTitle,
                    fontSize = 12.sp,
                    color = LumiColors.Dark.copy(alpha = 0.65f)
                )

                lesson.explanation?.let {
                    Text(text = it, fontSize = 16.sp, lineHeight = 24.sp, color = LumiColors.Dark)
                }

                lesson.examples.orEmpty().forEach { example ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LumiColors.White)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = example.title ?: stringResource(R.string.ejemplo),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = LumiColors.Dark
                            )
                            example.content?.let {
                                Text(text = it, fontSize = 14.sp, color = LumiColors.Dark)
                            }
                        }
                    }
                }

                lesson.sourceReferences.orEmpty().forEach { ref ->
                    ref.quote?.let {
                        Text(
                            text = "“$it”" + (ref.page?.let { p -> stringResource(R.string.pag, p) } ?: ""),
                            fontSize = 12.sp,
                            color = LumiColors.Dark.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LumiButton(
                text = stringResource(R.string.completar_leccion),
                onClick = onComplete,
                enabled = percent >= 90
            )
        }
    }
}
