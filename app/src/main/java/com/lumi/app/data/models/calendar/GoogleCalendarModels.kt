package com.lumi.app.data.models.calendar

/** Modelos de la API REST de Google Calendar v3 (solo los campos que usa Lumi). */

data class GoogleEventTime(
    // Eventos de todo el día usan "date" (yyyy-MM-dd); los demás usan "dateTime" (RFC 3339)
    val date: String? = null,
    val dateTime: String? = null,
    val timeZone: String? = null
)

data class GoogleEventInput(
    val summary: String,
    val description: String? = null,
    val start: GoogleEventTime,
    val end: GoogleEventTime
)

data class GoogleEvent(
    val id: String? = null,
    val summary: String? = null,
    val start: GoogleEventTime? = null,
    val end: GoogleEventTime? = null
)

data class GoogleEventsResponse(
    val items: List<GoogleEvent>? = null
)
