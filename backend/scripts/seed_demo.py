"""Carga los datos de demostración de Lumi.

Uso (desde `backend/`, con el entorno virtual activo):

    python scripts/seed_demo.py
    python scripts/seed_demo.py --password "MiClaveSegura"
    SEED_DEMO_PASSWORD=... python scripts/seed_demo.py

El script es idempotente: si vuelve a ejecutarse actualiza el catálogo y no
duplica usuarios, semestres, materias ni tareas. La contraseña del usuario demo
solo se imprime la primera vez (o cuando se pasa `--password`); nunca se guarda
en el repositorio.
"""

from __future__ import annotations

import argparse
import os
import secrets
import sys
import uuid
from datetime import UTC, date, datetime, time, timedelta
from pathlib import Path

from sqlalchemy import select
from sqlalchemy.exc import ProgrammingError, SQLAlchemyError
from sqlalchemy.orm import Session

# Permite ejecutar el script con `python scripts/seed_demo.py` desde backend/.
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from app.core.database import (
    get_session_factory,
    is_database_configured,
    reset_engine,
)
from app.core.security import hash_password
from app.modules.academics.entities import Semester, Subject, SubjectStatus
from app.modules.achievements.entities import (
    Achievement,
    UserAchievement,
)
from app.modules.auth.entities import AuthProvider, User
from app.modules.store.entities import StoreItem, StoreItemType
from app.modules.tasks.entities import (
    Subtask,
    Task,
    TaskPriority,
    TaskStatus,
)
from app.modules.users.entities import UserPreference

DEMO_EMAIL = "demo@lumi.app"
DEMO_NAME = "Estudiante Demo"
DEMO_TIME_ZONE = "America/Mexico_City"

# Espacio de nombres para ids estables entre ejecuciones y entornos. Así la
# semilla se puede repetir sin duplicar filas y las capturas de la rúbrica
# muestran siempre los mismos identificadores.
NAMESPACE = uuid.UUID("5d0f6b1a-9a55-4d7a-9f6e-2f1c3b4a5d6e")


def stable_id(kind: str, key: str) -> uuid.UUID:
    """Genera un UUID reproducible a partir del tipo y una clave."""
    return uuid.uuid5(NAMESPACE, f"{kind}:{key}")


ACHIEVEMENTS: list[tuple[str, str, str, int]] = [
    ("first_task", "Primera tarea", "Completa tu primera tarea", 1),
    ("tasks_10", "Diez tareas", "Completa diez tareas", 10),
    ("streak_3", "Racha de 3 días", "Estudia tres días seguidos", 3),
    ("streak_7", "Racha de 7 días", "Estudia siete días seguidos", 7),
    ("first_material", "Primer material", "Sube tu primer material de estudio", 1),
    ("first_lesson", "Primera lección", "Completa tu primera lección", 1),
    ("quiz_pass", "Cuestionario aprobado", "Aprueba un cuestionario", 1),
    ("quiz_perfect", "Cuestionario perfecto", "Acierta todas las preguntas", 1),
    ("study_60", "Una hora de estudio", "Acumula 60 minutos de estudio", 60),
]

STORE_ITEMS: list[tuple[str, str, str, int, StoreItemType, dict[str, object]]] = [
    (
        "boost_xp_2x",
        "Impulso de XP x2",
        "Duplica el XP de tu próxima sesión de estudio.",
        80,
        StoreItemType.BOOST,
        {"kind": "xp_multiplier", "multiplier": 2, "durationMinutes": 30},
    ),
    (
        "boost_focus_30",
        "Sesión de enfoque",
        "Treinta minutos de enfoque sin distracciones.",
        60,
        StoreItemType.BOOST,
        {"kind": "focus", "durationMinutes": 30},
    ),
    (
        "streak_freeze_1",
        "Congelador de racha",
        "Protege tu racha durante un día sin estudiar.",
        100,
        StoreItemType.STREAK_FREEZE,
        {"kind": "streak_freeze", "days": 1},
    ),
    (
        "streak_freeze_3",
        "Congelador de racha x3",
        "Protege tu racha durante tres días sin estudiar.",
        250,
        StoreItemType.STREAK_FREEZE,
        {"kind": "streak_freeze", "days": 3},
    ),
]


def _subject_id(email: str, name: str) -> uuid.UUID:
    return stable_id("subject", f"{email}:{name}")


def _task_id(email: str, title: str) -> uuid.UUID:
    return stable_id("task", f"{email}:{title}")


def _demo_user(session: Session, email: str, name: str, password: str | None) -> User:
    """Crea el usuario de demostración si no existe y fija su contraseña.

    `password` solo llega con valor cuando hay que crear la cuenta o cuando el
    usuario pidió explícitamente cambiarla con `--password`. Una ejecución normal
    sobre una cuenta existente no toca su contraseña.
    """
    user = session.scalar(select(User).where(User.email == email))
    if user is None:
        user = User(
            id=stable_id("user", email),
            name=name,
            email=email,
            auth_provider=AuthProvider.LOCAL,
            locale="es",
            time_zone=DEMO_TIME_ZONE,
        )
        session.add(user)
        print(f"Usuario demo creado: {email}")
    else:
        print(f"Usuario demo ya existía: {email}")

    if password is not None:
        user.password_hash = hash_password(password)
        print("Contraseña del usuario demo fijada.")

    return user


def _seed_preferences(session: Session, user: User) -> None:
    preferences = session.get(UserPreference, user.id)
    if preferences is None:
        preferences = UserPreference(
            user_id=user.id,
            selected_goals=["organizar_tareas", "constancia"],
            daily_goal_minutes=30,
            locale="es",
            notifications_enabled=True,
        )
        session.add(preferences)
        print("Preferencias creadas.")
    else:
        print("Preferencias ya existían.")


def _seed_academics(session: Session, user: User) -> dict[str, uuid.UUID]:
    """Crea dos semestres y cuatro materias; devuelve los ids por nombre."""
    semesters = [
        ("2026-2", date(2026, 8, 3), date(2026, 12, 18)),
        ("2027-1", date(2027, 1, 18), date(2027, 5, 29)),
    ]
    for name, start, end in semesters:
        semester_id = stable_id("semester", f"{user.email}:{name}")
        if session.get(Semester, semester_id) is None:
            session.add(
                Semester(
                    id=semester_id,
                    user_id=user.id,
                    name=name,
                    start_date=start,
                    end_date=end,
                )
            )
            print(f"Semestre creado: {name}")

    session.flush()

    subjects = [
        ("Cálculo Diferencial", "#6C5CE7", "Límites, derivadas y aplicaciones."),
        ("Programación Orientada a Objetos", "#00B894", "Clases, objetos y patrones."),
        ("Bases de Datos", "#0984E3", "Modelo relacional, SQL y normalización."),
        ("Ética Profesional", "#E17055", "Responsabilidad y dilemas profesionales."),
    ]
    semester_id = stable_id("semester", f"{user.email}:2026-2")
    subject_ids: dict[str, uuid.UUID] = {}
    for name, color, description in subjects:
        subject_id = _subject_id(user.email, name)
        subject_ids[name] = subject_id
        if session.get(Subject, subject_id) is None:
            session.add(
                Subject(
                    id=subject_id,
                    user_id=user.id,
                    semester_id=semester_id,
                    name=name,
                    description=description,
                    color=color,
                    status=SubjectStatus.ACTIVE,
                )
            )
            print(f"Materia creada: {name}")

    session.flush()
    return subject_ids


def _seed_tasks(session: Session, user: User, subject_ids: dict[str, uuid.UUID]) -> None:
    """Crea tareas de ejemplo con subtareas; usa fechas relativas a hoy."""
    today = datetime.now(UTC).date()
    definitions = [
        (
            "Resolver límites por sustitución",
            "Cálculo Diferencial",
            today + timedelta(days=2),
            time(18, 0),
            TaskPriority.HIGH,
            ["Leer la sección 2.1", "Resolver los ejercicios 1 al 10"],
        ),
        (
            "Proyecto final de POO",
            "Programación Orientada a Objetos",
            today + timedelta(days=9),
            time(12, 0),
            TaskPriority.HIGH,
            ["Diseñar el diagrama de clases", "Implementar las pruebas"],
        ),
        (
            "Normalizar el modelo de la biblioteca",
            "Bases de Datos",
            today + timedelta(days=4),
            None,
            TaskPriority.MEDIUM,
            ["Identificar dependencias funcionales"],
        ),
        (
            "Ensayo sobre ética profesional",
            "Ética Profesional",
            today + timedelta(days=6),
            time(20, 30),
            TaskPriority.MEDIUM,
            ["Investigar un caso real", "Redactar 3 cuartillas"],
        ),
        (
            "Repasar derivadas",
            "Cálculo Diferencial",
            today - timedelta(days=2),
            None,
            TaskPriority.LOW,
            [],
        ),
        (
            "Entregar práctica de SQL",
            "Bases de Datos",
            today - timedelta(days=1),
            time(23, 59),
            TaskPriority.HIGH,
            ["Consultas con JOIN", "Subconsultas"],
        ),
        (
            "Leer capítulo de patrones de diseño",
            "Programación Orientada a Objetos",
            None,
            None,
            TaskPriority.LOW,
            [],
        ),
        (
            "Preparar exposición de ética",
            "Ética Profesional",
            today + timedelta(days=12),
            time(10, 0),
            TaskPriority.MEDIUM,
            ["Armar diapositivas", "Ensayar la presentación"],
        ),
    ]

    completed_titles = {"Repasar derivadas", "Entregar práctica de SQL"}

    for title, subject, due_date, due_time, priority, subtask_titles in definitions:
        task_id = _task_id(user.email, title)
        if session.get(Task, task_id) is not None:
            continue

        is_completed = title in completed_titles
        task = Task(
            id=task_id,
            user_id=user.id,
            subject_id=subject_ids[subject],
            title=title,
            description=None,
            due_date=due_date,
            due_time=due_time,
            all_day=due_time is None,
            time_zone=DEMO_TIME_ZONE,
            priority=priority,
            status=TaskStatus.COMPLETED if is_completed else TaskStatus.PENDING,
            completed_at=(datetime.now(UTC) - timedelta(days=1) if is_completed else None),
        )
        session.add(task)
        session.flush()
        for index, subtask_title in enumerate(subtask_titles):
            session.add(
                Subtask(
                    id=stable_id("subtask", f"{task_id}:{index}"),
                    task_id=task_id,
                    title=subtask_title,
                    completed=is_completed and index == 0,
                )
            )
        print(f"Tarea creada: {title}")


def _seed_catalogs(session: Session) -> dict[str, uuid.UUID]:
    """Crea o actualiza el catálogo de tienda y de logros."""
    achievement_ids: dict[str, uuid.UUID] = {}
    for code, title, description, target in ACHIEVEMENTS:
        achievement_id = stable_id("achievement", code)
        achievement_ids[code] = achievement_id
        achievement = session.get(Achievement, achievement_id)
        if achievement is None:
            session.add(
                Achievement(
                    id=achievement_id,
                    code=code,
                    title=title,
                    description=description,
                    target=target,
                )
            )
            print(f"Logro creado: {code}")
        else:
            achievement.title = title
            achievement.description = description
            achievement.target = target

    for code, name, description, price, item_type, effects in STORE_ITEMS:
        item_id = stable_id("store_item", code)
        item = session.get(StoreItem, item_id)
        if item is None:
            session.add(
                StoreItem(
                    id=item_id,
                    code=code,
                    name=name,
                    description=description,
                    price_gems=price,
                    type=item_type,
                    effects=effects,
                )
            )
            print(f"Artículo creado: {code}")
        else:
            item.name = name
            item.description = description
            item.price_gems = price
            item.type = item_type
            item.effects = effects
            item.active = True

    return achievement_ids


def _unlock_demo_achievements(
    session: Session, user: User, achievement_ids: dict[str, uuid.UUID]
) -> None:
    """Desbloquea un par de logros para que la demo no aparezca vacía."""
    for code in ("first_task", "tasks_10"):
        achievement_id = achievement_ids[code]
        exists = session.get(UserAchievement, (user.id, achievement_id))
        if exists is None:
            session.add(
                UserAchievement(
                    user_id=user.id,
                    achievement_id=achievement_id,
                    unlocked_at=datetime.now(UTC),
                )
            )
            print(f"Logro desbloqueado para la demo: {code}")


def seed(
    email: str,
    name: str,
    explicit_password: str | None,
    generated_password: str | None,
) -> bool:
    """Ejecuta toda la semilla en una transacción.

    Devuelve `True` si el usuario demo se creó en esta ejecución.
    """
    if not is_database_configured():
        raise SystemExit(
            "DATABASE_URL no está configurada o sigue con los marcadores de "
            ".env.example. Revisa backend/.env antes de sembrar."
        )

    reset_engine()
    factory = get_session_factory()
    with factory() as session:
        existing = session.scalar(select(User).where(User.email == email))
        if existing is None:
            password = explicit_password or generated_password
        else:
            # Solo se cambia la contraseña si se pidió explícitamente.
            password = explicit_password

        user = _demo_user(session, email, name, password)
        # Sin relaciones ORM entre módulos, el orden de inserción se fuerza con
        # flush explícito para respetar las claves foráneas.
        session.flush()
        _seed_preferences(session, user)
        subject_ids = _seed_academics(session, user)
        _seed_tasks(session, user, subject_ids)
        achievement_ids = _seed_catalogs(session)
        session.flush()
        _unlock_demo_achievements(session, user, achievement_ids)
        session.commit()

    print("Semilla completada.")
    return existing is None


def main() -> None:
    parser = argparse.ArgumentParser(description="Carga los datos demo de Lumi.")
    parser.add_argument("--email", default=DEMO_EMAIL, help="Correo del usuario demo.")
    parser.add_argument("--name", default=DEMO_NAME, help="Nombre del usuario demo.")
    parser.add_argument(
        "--password",
        default=os.getenv("SEED_DEMO_PASSWORD"),
        help=(
            "Contraseña del usuario demo. Si se omite, se genera una y se imprime "
            "solo cuando el usuario se crea por primera vez."
        ),
    )
    args = parser.parse_args()

    generated_password: str | None = None
    if not args.password:
        generated_password = secrets.token_urlsafe(12)

    try:
        created = seed(args.email, args.name, args.password, generated_password)
    except ProgrammingError as error:
        raise SystemExit("Faltan tablas. Ejecuta primero: alembic upgrade head") from error
    except SQLAlchemyError as error:
        raise SystemExit(f"Error de base de datos: {error}") from error

    if created and generated_password:
        print()
        print("Contraseña generada para el usuario demo (guárdala fuera del repo):")
        print(f"  {generated_password}")
        print("Vuelve a ejecutar con --password para cambiarla.")
    elif created:
        print("Contraseña del usuario demo: la indicada en --password/SEED_DEMO_PASSWORD.")


if __name__ == "__main__":
    main()
