package com.lumi.app.features.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lumi.app.core.network.toUserMessage
import com.lumi.app.data.models.calendar.GoogleEvent
import com.lumi.app.data.models.calendar.GoogleEventInput
import com.lumi.app.data.models.calendar.GoogleEventTime
import com.lumi.app.data.repositories.GoogleCalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.lumi.app.core.i18n.AppText
import com.lumi.app.R

data class CalendarEvent(
    val id: String,
    val title: String,
    val date: LocalDate,
    // "HH:mm" o null si es de todo el día
    val time: String?
)

class CalendarViewModel(
    private val repository: GoogleCalendarRepository
) : ViewModel() {

    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> = _events.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private var loadedMonth: YearMonth? = null

    fun loadMonth(accessToken: String, month: YearMonth) {
        val zone = ZoneId.systemDefault()
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

        loadedMonth = month

        viewModelScope.launch {
            repository.listEvents(
                accessToken = accessToken,
                timeMin = formatter.format(month.atDay(1).atStartOfDay(zone)),
                timeMax = formatter.format(month.plusMonths(1).atDay(1).atStartOfDay(zone))
            ).onSuccess { list ->
                _events.value = list.mapNotNull { it.toCalendarEvent(zone) }
            }.onFailure {
                _message.value = it.toUserMessage(AppText.get(R.string.no_se_pudieron_cargar_tus))
            }
        }
    }

    fun createEvent(
        accessToken: String,
        title: String,
        description: String,
        date: LocalDate,
        allDay: Boolean,
        start: LocalTime,
        end: LocalTime,
        onCreated: () -> Unit
    ) {
        if (_saving.value) return

        val zone = ZoneId.systemDefault()
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

        val input = GoogleEventInput(
            summary = title.trim(),
            description = description.trim().ifBlank { null },
            start = if (allDay) {
                GoogleEventTime(date = date.toString())
            } else {
                GoogleEventTime(
                    dateTime = formatter.format(date.atTime(start).atZone(zone)),
                    timeZone = zone.id
                )
            },
            end = if (allDay) {
                // En Google, el fin de un evento de todo el día es el día siguiente
                GoogleEventTime(date = date.plusDays(1).toString())
            } else {
                GoogleEventTime(
                    dateTime = formatter.format(date.atTime(end).atZone(zone)),
                    timeZone = zone.id
                )
            }
        )

        viewModelScope.launch {
            _saving.value = true

            repository.createEvent(accessToken, input)
                .onSuccess {
                    _message.value = AppText.get(R.string.evento_agregado_a_google_calendar)
                    onCreated()
                    loadedMonth?.let { loadMonth(accessToken, it) }
                }
                .onFailure {
                    _message.value = it.toUserMessage(AppText.get(R.string.no_se_pudo_crear_el))
                }

            _saving.value = false
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    private fun GoogleEvent.toCalendarEvent(zone: ZoneId): CalendarEvent? {
        val eventId = id ?: return null
        val startInfo = start ?: return null

        return try {
            if (startInfo.dateTime != null) {
                val local = OffsetDateTime.parse(startInfo.dateTime)
                    .atZoneSameInstant(zone)
                    .toLocalDateTime()

                CalendarEvent(
                    id = eventId,
                    title = summary ?: AppText.get(R.string.sin_titulo),
                    date = local.toLocalDate(),
                    time = local.toLocalTime().toString().take(5)
                )
            } else if (startInfo.date != null) {
                CalendarEvent(
                    id = eventId,
                    title = summary ?: AppText.get(R.string.sin_titulo),
                    date = LocalDate.parse(startInfo.date),
                    time = null
                )
            } else {
                null
            }
        } catch (exception: Exception) {
            null
        }
    }
}

class CalendarViewModelFactory(
    private val repository: GoogleCalendarRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            return CalendarViewModel(repository) as T
        }

        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
