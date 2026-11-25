package com.lumi.app.features.subjects

import com.lumi.app.data.models.subjects.SemesterResponse
import com.lumi.app.data.models.subjects.SubjectResponse
import com.lumi.app.features.subjects.mapper.toColorLongOrDefault
import com.lumi.app.features.subjects.mapper.toSemester
import com.lumi.app.features.subjects.mapper.toSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SubjectMappersTest {

    @Test
    fun aceptaColorConSeisDigitos() {
        assertEquals(0xFFFFD83DL, "#FFD83D".toColorLongOrDefault())
    }

    @Test
    fun aceptaColorConOchoDigitos() {
        assertEquals(0x80FFD83DL, "#80FFD83D".toColorLongOrDefault())
    }

    @Test
    fun usaElColorPorDefectoSiNoEsValido() {
        assertEquals(0xFFFFE6A3L, "rojo".toColorLongOrDefault())
        assertEquals(0xFFFFE6A3L, "#12".toColorLongOrDefault())
        assertEquals(0xFFFFE6A3L, null.toColorLongOrDefault())
    }

    @Test
    fun convierteLaRespuestaEnMateria() {
        val response = SubjectResponse(
            id = "subject-1",
            name = "Cálculo",
            description = null,
            color = "#B9D9FF",
            semesterId = "semester-1",
            status = "active",
            version = 3
        )

        val subject = response.toSubject()

        assertEquals("subject-1", subject.id)
        assertEquals("Cálculo", subject.name)
        assertEquals("semester-1", subject.semesterId)
        assertNull(subject.description)
        assertEquals(0xFFB9D9FFL, subject.color)
    }

    @Test
    fun convierteLaRespuestaEnSemestre() {
        val response = SemesterResponse(
            id = "semester-1",
            name = "2026-1",
            version = 1
        )

        val semester = response.toSemester()

        assertEquals("semester-1", semester.id)
        assertEquals("2026-1", semester.name)
    }
}
