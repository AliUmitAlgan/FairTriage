"""Pydantic v2 request and response schemas for FairTriage."""

from datetime import datetime, timezone
from typing import Literal

from pydantic import BaseModel, ConfigDict, Field, computed_field, field_validator, model_validator


Gender = Literal["Male", "Female", "Other"]
TriageLevel = Literal["Critical", "Urgent", "Stable"]
PatientStatus = Literal["waiting", "in_treatment", "completed"]
MAX_WAITING_MINUTES_BY_TRIAGE = {"Urgent": 90, "Stable": 180}
ActionType = Literal[
    "created",
    "score_calculated",
    "queue_updated",
    "doctor_override",
    "completed",
]


def _normalize_utc(value: datetime | None) -> datetime | None:
    if value is None:
        return None
    if value.tzinfo is None or value.utcoffset() is None:
        raise ValueError("Datetime must include timezone information.")
    return value.astimezone(timezone.utc)


class PatientCreate(BaseModel):
    """Payload for creating a patient."""

    full_name: str = Field(..., min_length=1, max_length=120)
    age: int = Field(..., ge=1, le=120)
    gender: Gender
    arrival_time: datetime | None = Field(
        default=None,
        description="UTC-aware arrival time. Defaults to current UTC time when omitted.",
    )
    symptoms_description: str = Field(..., min_length=1)
    pain_level: int = Field(..., ge=0, le=10)
    fever: bool
    heart_rate: int = Field(..., ge=30, le=250)
    blood_pressure_systolic: int = Field(..., ge=50, le=250)
    blood_pressure_diastolic: int = Field(..., ge=30, le=180)
    has_chronic_disease: bool
    chronic_disease_description: str | None = Field(default=None, max_length=500)
    image_score: float = Field(..., ge=0.0, le=1.0, description="Mock image score only.")

    @field_validator("arrival_time")
    @classmethod
    def validate_arrival_time(cls, value: datetime | None) -> datetime | None:
        """Require timezone-aware datetimes and normalize them to UTC."""
        return _normalize_utc(value)


class PatientUpdate(BaseModel):
    """Payload for updating editable patient fields."""

    full_name: str | None = Field(default=None, min_length=1, max_length=120)
    age: int | None = Field(default=None, ge=1, le=120)
    gender: Gender | None = None
    arrival_time: datetime | None = Field(default=None, description="UTC-aware datetime.")
    symptoms_description: str | None = Field(default=None, min_length=1)
    pain_level: int | None = Field(default=None, ge=0, le=10)
    fever: bool | None = None
    heart_rate: int | None = Field(default=None, ge=30, le=250)
    blood_pressure_systolic: int | None = Field(default=None, ge=50, le=250)
    blood_pressure_diastolic: int | None = Field(default=None, ge=30, le=180)
    has_chronic_disease: bool | None = None
    chronic_disease_description: str | None = Field(default=None, max_length=500)
    image_score: float | None = Field(default=None, ge=0.0, le=1.0)
    status: PatientStatus | None = None

    @field_validator("arrival_time")
    @classmethod
    def validate_arrival_time(cls, value: datetime | None) -> datetime | None:
        """Require timezone-aware datetimes and normalize them to UTC."""
        return _normalize_utc(value)

    @model_validator(mode="after")
    def reject_nulls_for_required_fields(self) -> "PatientUpdate":
        """Reject explicit null values for fields that are not nullable in storage."""
        nullable_fields = {"chronic_disease_description"}
        for field_name in self.model_fields_set - nullable_fields:
            if getattr(self, field_name) is None:
                raise ValueError(f"{field_name} cannot be null.")
        return self


class DoctorOverrideRequest(BaseModel):
    """Payload for clinician triage-level overrides."""

    new_triage_level: TriageLevel | None = None
    override_reason: str | None = Field(default=None, min_length=1, max_length=1000)
    override_reasons: list[str] = Field(default_factory=list, max_length=12)

    @field_validator("override_reasons")
    @classmethod
    def validate_override_reasons(cls, value: list[str]) -> list[str]:
        """Normalize checklist reasons submitted by the mobile app."""
        cleaned = [item.strip() for item in value if item and item.strip()]
        if any(len(item) > 160 for item in cleaned):
            raise ValueError("Each override reason must be 160 characters or fewer.")
        return cleaned

    @model_validator(mode="after")
    def require_override_reason(self) -> "DoctorOverrideRequest":
        """Accept either the new checklist list or the legacy free-text reason."""
        if not self.override_reasons and not (self.override_reason and self.override_reason.strip()):
            raise ValueError("At least one override reason is required.")
        return self

    @property
    def normalized_override_reason(self) -> str:
        """Return a single audit-friendly reason string for storage and logs."""
        if self.override_reasons:
            return "; ".join(self.override_reasons)
        return (self.override_reason or "").strip()


class PatientRead(BaseModel):
    """Patient response with calculated triage fields."""

    model_config = ConfigDict(from_attributes=True)

    id: int
    full_name: str
    age: int
    gender: Gender
    arrival_time: datetime
    symptoms_description: str
    pain_level: int
    fever: bool
    heart_rate: int
    blood_pressure_systolic: int
    blood_pressure_diastolic: int
    has_chronic_disease: bool
    chronic_disease_description: str | None
    image_score: float
    symptom_score: float
    history_score: float
    clinical_risk_score: float
    waiting_time_factor: float
    final_priority_score: float
    triage_level: TriageLevel
    queue_position: int
    decision_rationale: str
    status: PatientStatus
    overridden_by_doctor: bool
    doctor_override_reason: str | None
    created_at: datetime
    updated_at: datetime

    @computed_field
    @property
    def waiting_minutes(self) -> float:
        """Current waiting time in minutes for queue transparency."""
        if self.status != "waiting":
            return 0.0
        return round(max((datetime.now(timezone.utc) - self.arrival_time).total_seconds() / 60, 0), 1)

    @computed_field
    @property
    def max_waiting_minutes(self) -> int | None:
        """Configured maximum waiting threshold for fairness review."""
        if self.status != "waiting":
            return None
        return MAX_WAITING_MINUTES_BY_TRIAGE.get(self.triage_level)

    @computed_field
    @property
    def max_waiting_exceeded(self) -> bool:
        """Whether this patient has exceeded the fairness waiting cap."""
        threshold = self.max_waiting_minutes
        return threshold is not None and self.waiting_minutes >= threshold

    @computed_field
    @property
    def queue_policy_summary(self) -> str:
        """Human-readable queue policy status for the mobile client."""
        if self.triage_level == "Critical":
            return "Critical safety protection: stays above non-critical patients."
        if self.max_waiting_exceeded:
            return "Maximum waiting constraint active within this triage group."
        return "Ordered by triage level, final priority score, and arrival time."


class DecisionLogRead(BaseModel):
    """Decision log response."""

    model_config = ConfigDict(from_attributes=True)

    id: int
    patient_id: int
    action_type: ActionType
    old_triage_level: str | None
    new_triage_level: str | None
    old_priority_score: float | None
    new_priority_score: float | None
    explanation: str
    created_at: datetime


class MessageResponse(BaseModel):
    """Simple message response."""

    message: str


class CountMessageResponse(BaseModel):
    """Message response with an affected-row count."""

    message: str
    count: int


class ProductPolicyRead(BaseModel):
    """Report-aligned product capability and safety policy metadata."""

    product_name: str
    prototype_disclaimer: str
    clinical_control_policy: str
    scoring_formula: str
    safety_rules: list[str]
    fairness_policy: str
    max_waiting_minutes: dict[str, int]
    audit_log_actions: list[str]
    privacy_policy: str
    offline_policy: str

