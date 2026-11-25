package com.lumi.app.data.repositories

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressParsersTest {

    private fun json(text: String) = JsonParser.parseString(text).asJsonObject

    @Test
    fun semanaUsaElXpSemanalCuandoExiste() {
        val weekly = parseWeekly(json("""{"weeklyXp": 350, "days": []}"""))

        assertEquals(350, weekly.xp)
    }

    @Test
    fun semanaSumaElXpDeLosDiasCuandoNoHayTotal() {
        val weekly = parseWeekly(
            json("""{"days": [{"xp": 100, "minutes": 20}, {"xp": 50, "minutes": 10}]}""")
        )

        assertEquals(150, weekly.xp)
        assertEquals(listOf(20, 10), weekly.dailyMinutes)
    }

    @Test
    fun semanaConservaSoloLosUltimosSieteDias() {
        val days = (1..9).joinToString(",") { """{"minutes": $it}""" }
        val weekly = parseWeekly(json("""{"days": [$days]}"""))

        assertEquals(listOf(3, 4, 5, 6, 7, 8, 9), weekly.dailyMinutes)
    }

    @Test
    fun semanaAceptaNombresAlternativosDeCampos() {
        val weekly = parseWeekly(
            json("""{"xpEarned": 80, "items": [{"studyMinutes": 15}]}""")
        )

        assertEquals(80, weekly.xp)
        assertEquals(listOf(15), weekly.dailyMinutes)
    }

    @Test
    fun semanaVaciaDevuelveCeros() {
        val weekly = parseWeekly(json("{}"))

        assertEquals(0, weekly.xp)
        assertEquals(emptyList<Int>(), weekly.dailyMinutes)
    }

    @Test
    fun semanaIgnoraDiasSinMinutos() {
        val weekly = parseWeekly(json("""{"days": [{"xp": 10}, {"minutes": 30}]}"""))

        assertEquals(listOf(0, 30), weekly.dailyMinutes)
    }

    @Test
    fun resumenLeeMinutosYLecciones() {
        val overview = parseOverview(
            json("""{"totalStudyMinutes": 125, "completedLessons": 4}""")
        )

        assertEquals(125, overview.studyMinutes)
        assertEquals(4, overview.completedLessons)
    }

    @Test
    fun resumenConvierteHorasAMinutos() {
        val overview = parseOverview(json("""{"totalStudyHours": 3}"""))

        assertEquals(180, overview.studyMinutes)
    }

    @Test
    fun resumenSinCamposDevuelveCeros() {
        val overview = parseOverview(json("{}"))

        assertEquals(0, overview.studyMinutes)
        assertEquals(0, overview.completedLessons)
    }

    @Test
    fun resumenIgnoraValoresQueNoSonNumeros() {
        val overview = parseOverview(json("""{"completedLessons": "muchas"}"""))

        assertEquals(0, overview.completedLessons)
    }
}
