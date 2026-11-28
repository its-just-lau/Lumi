"""Pruebas de la semilla que no necesitan base de datos."""

from __future__ import annotations

from scripts.seed_demo import ACHIEVEMENTS, STORE_ITEMS, stable_id

CODIGOS_ESPERADOS = {
    "first_task",
    "tasks_10",
    "streak_3",
    "streak_7",
    "first_material",
    "first_lesson",
    "quiz_pass",
    "quiz_perfect",
    "study_60",
}


def test_el_catalogo_de_logros_tiene_los_nueve_codigos_acordados() -> None:
    assert {code for code, *_ in ACHIEVEMENTS} == CODIGOS_ESPERADOS


def test_la_tienda_solo_ofrece_boost_y_congeladores() -> None:
    assert {item[4].value for item in STORE_ITEMS} == {"boost", "streak_freeze"}


def test_los_ids_de_la_semilla_son_reproducibles() -> None:
    assert stable_id("user", "demo@lumi.app") == stable_id("user", "demo@lumi.app")
    assert stable_id("user", "demo@lumi.app") != stable_id("user", "otro@lumi.app")
