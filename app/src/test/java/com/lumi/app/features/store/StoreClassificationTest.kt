package com.lumi.app.features.store

import com.lumi.app.data.models.store.StoreItemResponse
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreClassificationTest {

    private fun item(name: String, type: String? = null) =
        StoreItemResponse(id = name, name = name, type = type)

    @Test
    fun reconocePorElTipo() {
        assertTrue(isStreakItem(item("Escudo", type = "streak_freeze")))
    }

    @Test
    fun reconocePorElNombreEnEspanolOIngles() {
        assertTrue(isStreakItem(item("Protector de racha")))
        assertTrue(isStreakItem(item("Streak Saver")))
    }

    @Test
    fun noConfundeUnPotenciadorDeXpConUnaRacha() {
        assertFalse(isStreakItem(item("Potenciador x2", type = "xp_boost")))
    }

    @Test
    fun ignoraMayusculas() {
        assertTrue(isStreakItem(item("RACHA EXTRA")))
    }
}
