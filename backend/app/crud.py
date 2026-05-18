"""Database operations for FairTriage routes."""

from datetime import datetime, timedelta, timezone

from sqlalchemy import delete, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app import models, schemas
from app.triage_engine import (
    build_doctor_override_rationale,
    calculate_patient_scores,
    recalculate_waiting_queue,
)


PATIENT_DATETIME_FIELDS = ("arrival_time", "created_at", "updated_at")
LOG_DATETIME_FIELDS = ("created_at",)


def _as_utc_aware(value: datetime | None) -> datetime | None:
    """Normalize a datetime from SQLite or Python code to timezone-aware UTC."""
    if value is None:
        return None
    if value.tzinfo is None or value.utcoffset() is None:
        return value.replace(tzinfo=timezone.utc)
    return value.astimezone(timezone.utc)


def _complete_sentence(value: str | None, fallback: str) -> str:
    """Return a non-empty complete English sentence."""
    sentence = (value or "").strip() or fallback
    if sentence.endswith((".", "!", "?")):
        return sentence
    return f"{sentence}."


def normalize_patient_after_read(patient: models.Patient | None) -> models.Patient | None:
    """Normalize patient datetimes and rationale after every database read."""
    if patient is None:
        return None

    for field_name in PATIENT_DATETIME_FIELDS:
        setattr(patient, field_name, _as_utc_aware(getattr(patient, field_name)))

    patient.decision_rationale = _complete_sentence(
        patient.decision_rationale,
        f"Patient assigned {patient.triage_level}: no stored rationale was available",
    )
    return patient


def normalize_log_after_read(log: models.DecisionLog) -> models.DecisionLog:
    """Normalize decision-log datetimes after every database read."""
    for field_name in LOG_DATETIME_FIELDS:
        setattr(log, field_name, _as_utc_aware(getattr(log, field_name)))
    log.explanation = _complete_sentence(log.explanation, "Decision log entry recorded")
    return log


def normalize_patients_after_read(patients: list[models.Patient]) -> list[models.Patient]:
    """Normalize a list of patient rows read from the database."""
    return [patient for patient in (normalize_patient_after_read(patient) for patient in patients) if patient]


def normalize_logs_after_read(logs: list[models.DecisionLog]) -> list[models.DecisionLog]:
    """Normalize a list of decision-log rows read from the database."""
    return [normalize_log_after_read(log) for log in logs]


async def add_decision_log(
    session: AsyncSession,
    patient_id: int,
    action_type: schemas.ActionType,
    explanation: str,
    old_triage_level: str | None = None,
    new_triage_level: str | None = None,
    old_priority_score: float | None = None,
    new_priority_score: float | None = None,
) -> models.DecisionLog:
    """Create a decision log row inside the current transaction."""
    log = models.DecisionLog(
        patient_id=patient_id,
        action_type=action_type,
        old_triage_level=old_triage_level,
        new_triage_level=new_triage_level,
        old_priority_score=old_priority_score,
        new_priority_score=new_priority_score,
        explanation=_complete_sentence(explanation, "Decision log entry recorded"),
        created_at=datetime.now(timezone.utc),
    )
    session.add(log)
    return log


async def get_patient(session: AsyncSession, patient_id: int) -> models.Patient | None:
    """Return one patient by id, or None when missing."""
    patient = await session.get(models.Patient, patient_id)
    return normalize_patient_after_read(patient)


async def list_patients(session: AsyncSession) -> list[models.Patient]:
    """Return all patients ordered by id."""
    result = await session.execute(select(models.Patient).order_by(models.Patient.id))
    return normalize_patients_after_read(list(result.scalars().all()))


async def list_logs(session: AsyncSession) -> list[models.DecisionLog]:
    """Return all decision logs newest first."""
    result = await session.execute(
        select(models.DecisionLog).order_by(models.DecisionLog.created_at.desc(), models.DecisionLog.id.desc())
    )
    return normalize_logs_after_read(list(result.scalars().all()))


async def list_patient_logs(session: AsyncSession, patient_id: int) -> list[models.DecisionLog]:
    """Return all logs for one patient newest first."""
    result = await session.execute(
        select(models.DecisionLog)
        .where(models.DecisionLog.patient_id == patient_id)
        .order_by(models.DecisionLog.created_at.desc(), models.DecisionLog.id.desc())
    )
    return normalize_logs_after_read(list(result.scalars().all()))


async def patient_count(session: AsyncSession) -> int:
    """Return the number of patient rows."""
    result = await session.scalar(select(func.count()).select_from(models.Patient))
    return int(result or 0)


async def create_patient(session: AsyncSession, payload: schemas.PatientCreate) -> models.Patient:
    """Create a patient, calculate scores, write logs, and refresh the queue."""
    data = payload.model_dump()
    data["arrival_time"] = data["arrival_time"] or datetime.now(timezone.utc)
    patient = models.Patient(**data)

    calculate_patient_scores(patient, preserve_doctor_override=False)
    patient.updated_at = datetime.now(timezone.utc)
    session.add(patient)
    await session.flush()

    await add_decision_log(
        session,
        patient.id,
        "created",
        "Patient record created and initial triage inputs captured.",
        new_triage_level=patient.triage_level,
        new_priority_score=patient.final_priority_score,
    )
    await add_decision_log(
        session,
        patient.id,
        "score_calculated",
        patient.decision_rationale,
        new_triage_level=patient.triage_level,
        new_priority_score=patient.final_priority_score,
    )

    await recalculate_queue(session)
    await session.commit()
    await session.refresh(patient)
    return normalize_patient_after_read(patient)


async def update_patient(
    session: AsyncSession,
    patient: models.Patient,
    payload: schemas.PatientUpdate,
) -> models.Patient:
    """Update a patient, recalculate scores, log the calculation, and refresh the queue."""
    old_triage_level = patient.triage_level
    old_priority_score = patient.final_priority_score
    data = payload.model_dump(exclude_unset=True)

    for field_name, value in data.items():
        setattr(patient, field_name, value)

    calculate_patient_scores(patient, preserve_doctor_override=True)
    if patient.status != "waiting":
        patient.queue_position = 0
    patient.updated_at = datetime.now(timezone.utc)

    await session.flush()
    await add_decision_log(
        session,
        patient.id,
        "score_calculated",
        "Patient data updated and triage scores recalculated.",
        old_triage_level=old_triage_level,
        new_triage_level=patient.triage_level,
        old_priority_score=old_priority_score,
        new_priority_score=patient.final_priority_score,
    )
    await recalculate_queue(session)
    await session.commit()
    await session.refresh(patient)
    return normalize_patient_after_read(patient)


async def delete_patient(session: AsyncSession, patient: models.Patient) -> None:
    """Delete a patient and refresh remaining queue positions."""
    normalize_patient_after_read(patient)
    await session.delete(patient)
    await session.flush()
    await recalculate_queue(session)
    await session.commit()


async def recalculate_patient(session: AsyncSession, patient: models.Patient) -> models.Patient:
    """Force score recalculation for one patient and refresh the queue."""
    old_triage_level = patient.triage_level
    old_priority_score = patient.final_priority_score

    calculate_patient_scores(patient, preserve_doctor_override=True)
    if patient.status != "waiting":
        patient.queue_position = 0
    patient.updated_at = datetime.now(timezone.utc)

    await session.flush()
    await add_decision_log(
        session,
        patient.id,
        "score_calculated",
        "Scores were force recalculated for this patient.",
        old_triage_level=old_triage_level,
        new_triage_level=patient.triage_level,
        old_priority_score=old_priority_score,
        new_priority_score=patient.final_priority_score,
    )
    await recalculate_queue(session)
    await session.commit()
    await session.refresh(patient)
    return normalize_patient_after_read(patient)


async def override_patient_triage(
    session: AsyncSession,
    patient: models.Patient,
    payload: schemas.DoctorOverrideRequest,
) -> models.Patient:
    """Apply a clinician triage override and refresh the queue."""
    normalize_patient_after_read(patient)
    old_triage_level = patient.triage_level
    old_priority_score = patient.final_priority_score

    override_reason = payload.normalized_override_reason

    patient.triage_level = payload.new_triage_level
    patient.overridden_by_doctor = True
    patient.doctor_override_reason = override_reason
    patient.decision_rationale = build_doctor_override_rationale(
        patient.triage_level,
        override_reason,
        patient.clinical_risk_score,
        patient.waiting_time_factor,
    )
    patient.updated_at = datetime.now(timezone.utc)

    await session.flush()
    await add_decision_log(
        session,
        patient.id,
        "doctor_override",
        f"Doctor override applied with selected clinical reasons: {override_reason}",
        old_triage_level=old_triage_level,
        new_triage_level=patient.triage_level,
        old_priority_score=old_priority_score,
        new_priority_score=patient.final_priority_score,
    )
    await recalculate_queue(session)
    await session.commit()
    await session.refresh(patient)
    return normalize_patient_after_read(patient)


async def complete_patient(session: AsyncSession, patient: models.Patient) -> models.Patient:
    """Mark a patient completed and remove the patient from the waiting queue."""
    normalize_patient_after_read(patient)
    if patient.status == "completed":
        return patient

    old_triage_level = patient.triage_level
    old_priority_score = patient.final_priority_score
    patient.status = "completed"
    patient.queue_position = 0
    patient.updated_at = datetime.now(timezone.utc)

    await session.flush()
    await add_decision_log(
        session,
        patient.id,
        "completed",
        "Patient marked completed and removed from the waiting queue.",
        old_triage_level=old_triage_level,
        new_triage_level=patient.triage_level,
        old_priority_score=old_priority_score,
        new_priority_score=patient.final_priority_score,
    )
    await recalculate_queue(session)
    await session.commit()
    await session.refresh(patient)
    return normalize_patient_after_read(patient)


async def recalculate_queue(
    session: AsyncSession,
    log_position_changes: bool = True,
) -> list[models.Patient]:
    """Recalculate waiting times, final scores, and queue positions for all waiting patients."""
    result = await session.execute(
        select(models.Patient).where(models.Patient.status == "waiting")
    )
    waiting_patients = normalize_patients_after_read(list(result.scalars().all()))
    old_positions = {patient.id: patient.queue_position for patient in waiting_patients}
    old_scores = {patient.id: patient.final_priority_score for patient in waiting_patients}

    sorted_patients = recalculate_waiting_queue(waiting_patients)
    now = datetime.now(timezone.utc)
    for patient in waiting_patients:
        patient.updated_at = now

    if log_position_changes:
        for patient in sorted_patients:
            old_position = old_positions.get(patient.id, 0)
            if old_position != patient.queue_position:
                await add_decision_log(
                    session,
                    patient.id,
                    "queue_updated",
                    f"Queue position updated from {old_position} to {patient.queue_position}.",
                    old_triage_level=patient.triage_level,
                    new_triage_level=patient.triage_level,
                    old_priority_score=old_scores.get(patient.id),
                    new_priority_score=patient.final_priority_score,
                )

    await session.flush()
    return sorted_patients


async def reset_simulation(session: AsyncSession) -> int:
    """Delete all patients and decision logs, returning the removed patient count."""
    count = await patient_count(session)
    await session.execute(delete(models.DecisionLog))
    await session.execute(delete(models.Patient))
    await session.commit()
    return count


async def seed_demo_data(session: AsyncSession) -> tuple[str, int]:
    """Seed the fixed eight-patient demo dataset if the database is empty."""
    existing_count = await patient_count(session)
    if existing_count:
        return "Demo data already seeded", existing_count

    now = datetime.now(timezone.utc)
    demo_patients = [
        {
            "full_name": "Ali YÄ±lmaz",
            "age": 72,
            "gender": "Male",
            "arrival_time": now - timedelta(minutes=15),
            "symptoms_description": "Severe chest discomfort with fever and abnormal vital signs.",
            "pain_level": 9,
            "fever": True,
            "heart_rate": 140,
            "blood_pressure_systolic": 85,
            "blood_pressure_diastolic": 55,
            "has_chronic_disease": True,
            "chronic_disease_description": "Cardiac arrhythmia",
            "image_score": 0.85,
        },
        {
            "full_name": "Fatma Demir",
            "age": 55,
            "gender": "Female",
            "arrival_time": now - timedelta(minutes=40),
            "symptoms_description": "High pain with fever and elevated pulse.",
            "pain_level": 7,
            "fever": True,
            "heart_rate": 108,
            "blood_pressure_systolic": 135,
            "blood_pressure_diastolic": 88,
            "has_chronic_disease": True,
            "chronic_disease_description": "Type 2 diabetes",
            "image_score": 0.55,
        },
        {
            "full_name": "Mehmet Kaya",
            "age": 38,
            "gender": "Male",
            "arrival_time": now - timedelta(minutes=55),
            "symptoms_description": "Moderate pain with elevated heart rate and blood pressure.",
            "pain_level": 6,
            "fever": False,
            "heart_rate": 112,
            "blood_pressure_systolic": 145,
            "blood_pressure_diastolic": 92,
            "has_chronic_disease": False,
            "chronic_disease_description": None,
            "image_score": 0.40,
        },
        {
            "full_name": "AyÅŸe Ã‡elik",
            "age": 65,
            "gender": "Female",
            "arrival_time": now - timedelta(minutes=30),
            "symptoms_description": "High pain with fever and known hypertension.",
            "pain_level": 8,
            "fever": True,
            "heart_rate": 105,
            "blood_pressure_systolic": 130,
            "blood_pressure_diastolic": 85,
            "has_chronic_disease": True,
            "chronic_disease_description": "Hypertension",
            "image_score": 0.60,
        },
        {
            "full_name": "Hasan Ã–ztÃ¼rk",
            "age": 25,
            "gender": "Male",
            "arrival_time": now - timedelta(minutes=20),
            "symptoms_description": "Mild localized pain without fever.",
            "pain_level": 3,
            "fever": False,
            "heart_rate": 78,
            "blood_pressure_systolic": 120,
            "blood_pressure_diastolic": 78,
            "has_chronic_disease": False,
            "chronic_disease_description": None,
            "image_score": 0.10,
        },
        {
            "full_name": "Zeynep Arslan",
            "age": 30,
            "gender": "Female",
            "arrival_time": now - timedelta(minutes=150),
            "symptoms_description": "Low pain without fever, waiting for an extended period.",
            "pain_level": 2,
            "fever": False,
            "heart_rate": 80,
            "blood_pressure_systolic": 118,
            "blood_pressure_diastolic": 76,
            "has_chronic_disease": False,
            "chronic_disease_description": None,
            "image_score": 0.05,
        },
        {
            "full_name": "Emre YÄ±ldÄ±z",
            "age": 45,
            "gender": "Male",
            "arrival_time": now - timedelta(minutes=60),
            "symptoms_description": "Mild to moderate pain with stable vital signs.",
            "pain_level": 4,
            "fever": False,
            "heart_rate": 85,
            "blood_pressure_systolic": 125,
            "blood_pressure_diastolic": 80,
            "has_chronic_disease": False,
            "chronic_disease_description": None,
            "image_score": 0.20,
        },
        {
            "full_name": "Selin KoÃ§",
            "age": 22,
            "gender": "Female",
            "arrival_time": now - timedelta(minutes=10),
            "symptoms_description": "Very mild symptoms and normal vital signs.",
            "pain_level": 1,
            "fever": False,
            "heart_rate": 72,
            "blood_pressure_systolic": 115,
            "blood_pressure_diastolic": 72,
            "has_chronic_disease": False,
            "chronic_disease_description": None,
            "image_score": 0.05,
        },
    ]

    patients: list[models.Patient] = []
    for patient_data in demo_patients:
        patient = models.Patient(**patient_data)
        calculate_patient_scores(patient, preserve_doctor_override=False)
        session.add(patient)
        patients.append(patient)

    await session.flush()
    for patient in patients:
        await add_decision_log(
            session,
            patient.id,
            "created",
            "Demo patient created.",
            new_triage_level=patient.triage_level,
            new_priority_score=patient.final_priority_score,
        )
        await add_decision_log(
            session,
            patient.id,
            "score_calculated",
            patient.decision_rationale,
            new_triage_level=patient.triage_level,
            new_priority_score=patient.final_priority_score,
        )

    await recalculate_queue(session)
    await session.commit()
    return "Demo data seeded", len(patients)

