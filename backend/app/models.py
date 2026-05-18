"""SQLAlchemy ORM models for FairTriage."""

from datetime import datetime, timezone
from typing import Any

from sqlalchemy import Boolean, Float, ForeignKey, Integer, String, Text
from sqlalchemy.orm import Mapped, mapped_column, relationship
from sqlalchemy.types import TypeDecorator

from app.database import Base


def utc_now() -> datetime:
    """Return the current UTC time as a timezone-aware datetime."""
    return datetime.now(timezone.utc)


class UTCDateTime(TypeDecorator):
    """Store timezone-aware UTC datetimes as ISO-8601 strings in SQLite."""

    impl = String(40)
    cache_ok = True

    def process_bind_param(self, value: datetime | None, _dialect: Any) -> str | None:
        if value is None:
            return None
        if value.tzinfo is None or value.utcoffset() is None:
            raise ValueError("UTC-aware datetime values are required.")
        return value.astimezone(timezone.utc).isoformat()

    def process_result_value(self, value: str | datetime | None, _dialect: Any) -> datetime | None:
        if value is None:
            return None
        if isinstance(value, datetime):
            parsed = value
        else:
            parsed = datetime.fromisoformat(value.replace("Z", "+00:00"))
        if parsed.tzinfo is None or parsed.utcoffset() is None:
            parsed = parsed.replace(tzinfo=timezone.utc)
        return parsed.astimezone(timezone.utc)


class Patient(Base):
    """Patient record with calculated triage scores and queue metadata."""

    __tablename__ = "patients"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    full_name: Mapped[str] = mapped_column(String(120), nullable=False)
    age: Mapped[int] = mapped_column(Integer, nullable=False)
    gender: Mapped[str] = mapped_column(String(20), nullable=False)
    arrival_time: Mapped[datetime] = mapped_column(UTCDateTime(), nullable=False, index=True)
    symptoms_description: Mapped[str] = mapped_column(Text, nullable=False)
    pain_level: Mapped[int] = mapped_column(Integer, nullable=False)
    fever: Mapped[bool] = mapped_column(Boolean, nullable=False)
    heart_rate: Mapped[int] = mapped_column(Integer, nullable=False)
    blood_pressure_systolic: Mapped[int] = mapped_column(Integer, nullable=False)
    blood_pressure_diastolic: Mapped[int] = mapped_column(Integer, nullable=False)
    has_chronic_disease: Mapped[bool] = mapped_column(Boolean, nullable=False)
    chronic_disease_description: Mapped[str | None] = mapped_column(Text, nullable=True)
    image_score: Mapped[float] = mapped_column(Float, nullable=False, default=0.0)
    symptom_score: Mapped[float] = mapped_column(Float, nullable=False, default=0.0)
    history_score: Mapped[float] = mapped_column(Float, nullable=False, default=0.0)
    clinical_risk_score: Mapped[float] = mapped_column(Float, nullable=False, default=0.0)
    waiting_time_factor: Mapped[float] = mapped_column(Float, nullable=False, default=0.0)
    final_priority_score: Mapped[float] = mapped_column(Float, nullable=False, default=0.0)
    triage_level: Mapped[str] = mapped_column(String(20), nullable=False, default="Stable", index=True)
    queue_position: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    decision_rationale: Mapped[str] = mapped_column(
        Text,
        nullable=False,
        default="Patient assigned Stable: score calculation is pending.",
    )
    status: Mapped[str] = mapped_column(String(20), nullable=False, default="waiting", index=True)
    overridden_by_doctor: Mapped[bool] = mapped_column(Boolean, nullable=False, default=False)
    doctor_override_reason: Mapped[str | None] = mapped_column(Text, nullable=True)
    created_at: Mapped[datetime] = mapped_column(UTCDateTime(), nullable=False, default=utc_now)
    updated_at: Mapped[datetime] = mapped_column(
        UTCDateTime(),
        nullable=False,
        default=utc_now,
        onupdate=utc_now,
    )

    logs: Mapped[list["DecisionLog"]] = relationship(
        back_populates="patient",
        cascade="all, delete-orphan",
        passive_deletes=True,
    )


class DecisionLog(Base):
    """Audit entry for patient scoring, queue, and clinician actions."""

    __tablename__ = "decision_logs"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    patient_id: Mapped[int] = mapped_column(
        ForeignKey("patients.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
    )
    action_type: Mapped[str] = mapped_column(String(40), nullable=False, index=True)
    old_triage_level: Mapped[str | None] = mapped_column(String(20), nullable=True)
    new_triage_level: Mapped[str | None] = mapped_column(String(20), nullable=True)
    old_priority_score: Mapped[float | None] = mapped_column(Float, nullable=True)
    new_priority_score: Mapped[float | None] = mapped_column(Float, nullable=True)
    explanation: Mapped[str] = mapped_column(Text, nullable=False)
    created_at: Mapped[datetime] = mapped_column(UTCDateTime(), nullable=False, default=utc_now)

    patient: Mapped[Patient] = relationship(back_populates="logs")
