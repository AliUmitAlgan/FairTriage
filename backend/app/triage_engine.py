"""Deterministic FairTriage scoring, safety rules, rationale, and queue sorting."""

from datetime import datetime, timezone
from typing import Any


TRIAGE_RANK = {"Critical": 0, "Urgent": 1, "Stable": 2}
MAX_WAITING_MINUTES = {"Urgent": 90, "Stable": 180}
URGENT_RISK_FLOOR = 45.0
CRITICAL_RISK_FLOOR = 75.0
HIGH_RISK_SYMPTOM_KEYWORDS = {
    "unconscious or unresponsive": 35,
    "reduced consciousness": 30,
    "airway obstruction": 35,
    "severe breathing difficulty": 30,
    "cyanosis": 30,
    "signs of shock": 35,
    "severe active bleeding": 30,
    "cardiac arrest concern": 40,
    "crushing chest pressure": 22,
    "radiating chest pain": 20,
    "syncope after exertion": 16,
    "severe hypertension symptoms": 12,
    "cold clammy skin": 18,
    "leg swelling with breathlessness": 14,
    "calf pain with swelling": 10,
    "pacemaker or icd symptom": 12,
    "chest pain": 20,
    "abdominal pain": 6,
    "arm or leg pain": 4,
    "back pain": 4,
    "headache": 4,
    "shortness of breath": 20,
    "confusion": 18,
    "seizure": 18,
    "weakness or numbness": 15,
    "facial droop or speech difficulty": 20,
    "severe sudden headache": 18,
    "bleeding": 12,
    "blood in vomit or stool": 15,
    "severe abdominal guarding": 15,
    "anaphylaxis concern": 30,
    "poisoning or overdose": 18,
    "pregnancy with bleeding": 20,
    "child with lethargy": 18,
    "trauma": 12,
    "head injury": 14,
    "fracture or deformity": 10,
    "burn injury": 10,
    "deep wound": 6,
    "infected wound": 8,
    "abscess": 5,
    "cellulitis spreading": 10,
    "pressure sore": 5,
    "large burn area": 16,
    "electrical burn": 20,
    "frostbite": 10,
    "severe localized pain": 10,
    "wheezing or asthma attack": 12,
    "fever or chills": 8,
    "cough": 3,
    "productive cough": 5,
    "suspected sepsis": 25,
    "rash or swelling": 4,
    "dizziness or fainting": 6,
    "vision changes": 6,
    "palpitations": 6,
    "irregular heartbeat": 8,
    "persistent vomiting": 8,
    "nausea or vomiting": 5,
    "diarrhea": 3,
    "dehydration": 8,
    "urinary pain": 3,
    "flank pain": 5,
    "chemical exposure": 10,
    "medication reaction": 6,
    "animal or insect bite": 6,
    "alcohol or drug intoxication": 8,
    "pregnancy with abdominal pain": 12,
    "newborn or infant concern": 16,
    "child with poor intake": 8,
    "child with persistent fever": 10,
    "sudden vision loss": 16,
    "chemical eye exposure": 18,
    "severe eye pain": 10,
    "severe nosebleed": 8,
    "ear pain": 3,
    "dental abscess": 6,
    "facial swelling": 8,
    "suicidal thoughts": 18,
    "self-harm injury": 18,
    "violent or unsafe behavior": 16,
    "acute psychosis": 12,
    "panic attack": 4,
    "severe insomnia with distress": 3,
    "substance withdrawal": 12,
    "social safety concern": 4,
    "urinary retention": 8,
    "testicular pain": 12,
    "pelvic pain": 5,
    "vaginal bleeding": 8,
    "severe menstrual bleeding": 10,
    "sexual assault concern": 10,
    "postpartum bleeding": 22,
    "reduced fetal movement": 10,
    "smoke inhalation": 24,
    "heat stroke concern": 24,
    "hypothermia concern": 18,
    "drowning or submersion": 24,
    "carbon monoxide exposure": 24,
    "crush injury": 20,
    "blast injury": 20,
    "needs decontamination": 14,
    "fall in older adult": 10,
    "unable to perform daily activities": 5,
    "new confusion in older adult": 18,
    "poor oral intake": 6,
    "caregiver concern": 3,
    "unsafe discharge risk": 6,
    "unable to walk": 8,
    "severe fatigue": 4,
    "severe anxiety or agitation": 5,
    "needs isolation": 6,
    "other clinical concern": 4,
    "worsening symptoms": 10,
}
HIGH_RISK_HISTORY_KEYWORDS = {
    "heart disease": 20,
    "prior heart attack": 20,
    "heart failure": 18,
    "arrhythmia history": 12,
    "pacemaker or icd": 12,
    "valve disease": 10,
    "peripheral vascular disease": 10,
    "history of blood clot": 12,
    "kidney disease": 18,
    "cancer treatment": 18,
    "immunosuppressed": 18,
    "pregnancy risk": 15,
    "stroke history": 15,
    "seizure disorder": 12,
    "neurologic disease": 8,
    "dementia or cognitive impairment": 10,
    "organ transplant": 18,
    "dialysis patient": 18,
    "home oxygen use": 15,
    "sleep apnea": 5,
    "cystic fibrosis": 15,
    "hiv or aids": 12,
    "long-term steroid use": 12,
    "autoimmune disease": 8,
    "uses blood thinners": 15,
    "bleeding disorder": 15,
    "sickle cell disease": 14,
    "severe anemia history": 10,
    "high-risk medication use": 8,
    "recent medication change": 6,
    "medication allergy": 4,
    "recent surgery": 12,
    "diabetes": 10,
    "hypertension": 5,
    "asthma or copd": 10,
    "liver disease": 10,
    "cirrhosis": 12,
    "insulin-dependent diabetes": 12,
    "adrenal insufficiency": 12,
    "thyroid disease": 4,
    "severe obesity": 6,
    "malnutrition risk": 8,
    "postpartum under 6 weeks": 12,
    "nursing home resident": 8,
    "lives alone with limited support": 5,
    "pediatric chronic illness": 10,
    "developmental disability": 6,
    "mobility limitation": 5,
    "older adult frailty": 12,
    "recent hospitalization": 8,
    "indwelling catheter": 8,
    "central line or port": 10,
    "feeding tube": 6,
    "ventricular shunt": 12,
    "recent chemotherapy": 14,
    "recent trauma admission": 8,
    "homelessness or housing insecurity": 5,
    "known infectious exposure": 6,
    "frequent ed visits": 5,
    "no regular medication access": 5,
}
CRITICAL_SYMPTOM_KEYWORDS = (
    "unconscious or unresponsive",
    "airway obstruction",
    "severe breathing difficulty",
    "cyanosis",
    "signs of shock",
    "severe active bleeding",
    "cardiac arrest concern",
    "crushing chest pressure",
    "cold clammy skin",
    "anaphylaxis concern",
    "postpartum bleeding",
    "smoke inhalation",
    "heat stroke concern",
    "drowning or submersion",
    "carbon monoxide exposure",
    "crush injury",
    "blast injury",
)
URGENT_SYMPTOM_KEYWORDS = (
    "reduced consciousness",
    "shortness of breath",
    "confusion",
    "seizure",
    "weakness or numbness",
    "facial droop or speech difficulty",
    "severe sudden headache",
    "blood in vomit or stool",
    "severe abdominal guarding",
    "poisoning or overdose",
    "pregnancy with bleeding",
    "child with lethargy",
    "suspected sepsis",
    "radiating chest pain",
    "syncope after exertion",
    "severe hypertension symptoms",
    "leg swelling with breathlessness",
    "sudden vision loss",
    "chemical eye exposure",
    "suicidal thoughts",
    "self-harm injury",
    "violent or unsafe behavior",
    "acute psychosis",
    "substance withdrawal",
    "testicular pain",
    "sexual assault concern",
    "reduced fetal movement",
    "hypothermia concern",
    "needs decontamination",
    "new confusion in older adult",
    "chest pain",
    "severe localized pain",
    "bleeding",
    "worsening symptoms",
)
OVERRIDE_CRITICAL_KEYWORDS = (
    "chest pain",
    "cardiac concern",
    "respiratory distress",
    "neurologic red flag",
    "clinical deterioration",
    "severe pain escalation",
)
OVERRIDE_URGENT_KEYWORDS = (
    "abnormal heart rate",
    "abnormal blood pressure",
    "persistent fever",
    "infection concern",
    "pain level increased",
    "uncontrolled",
    "high-risk chronic disease",
    "frailty",
    "waiting time",
    "underestimates bedside risk",
    "bedside assessment",
    "patient safety precaution",
    "faster physician review",
)


def ensure_utc(value: datetime) -> datetime:
    """Return a timezone-aware UTC datetime and reject naive datetime values."""
    if value.tzinfo is None or value.utcoffset() is None:
        raise ValueError("Datetime must be timezone-aware.")
    return value.astimezone(timezone.utc)


def _read(patient: Any, field_name: str) -> Any:
    """Read a field from an ORM object, Pydantic model, or plain dictionary."""
    if isinstance(patient, dict):
        return patient[field_name]
    return getattr(patient, field_name)


def _set(patient: Any, field_name: str, value: Any) -> None:
    """Set a field on a mutable ORM object or plain dictionary."""
    if isinstance(patient, dict):
        patient[field_name] = value
        return
    setattr(patient, field_name, value)


def _has_ephemeral_flag(patient: Any, field_name: str) -> bool:
    """Read a transient queue-calculation flag from any supported patient object."""
    if isinstance(patient, dict):
        return bool(patient.get(field_name, False))
    return bool(getattr(patient, field_name, False))


def _round_score(value: float) -> float:
    """Round scores consistently for API responses and database storage."""
    return round(float(value), 2)


def _escalate(current_level: str, target_level: str) -> str:
    """Return the more urgent of two triage levels."""
    if TRIAGE_RANK[target_level] < TRIAGE_RANK[current_level]:
        return target_level
    return current_level


def _sentence(text: str) -> str:
    """Ensure a rationale fragment is a complete sentence."""
    clean = text.strip()
    if clean.endswith((".", "!", "?")):
        return clean
    return f"{clean}."


def _contains_any(text: str | None, keywords: tuple[str, ...]) -> bool:
    """Case-insensitive keyword search for checklist-driven clinical inputs."""
    normalized = (text or "").lower()
    return any(keyword in normalized for keyword in keywords)


def _keyword_score(text: str | None, weighted_keywords: dict[str, int]) -> int:
    """Add deterministic prototype risk points for selected clinical checklist terms."""
    normalized = (text or "").lower()
    return sum(points for keyword, points in weighted_keywords.items() if keyword in normalized)


def calculate_symptom_score(patient: Any) -> float:
    """Calculate symptom score from selected symptoms, pain, fever, heart rate, and pressure."""
    base = _read(patient, "pain_level") * 6
    base += _keyword_score(_read(patient, "symptoms_description"), HIGH_RISK_SYMPTOM_KEYWORDS)
    if _read(patient, "fever"):
        base += 15

    heart_rate = _read(patient, "heart_rate")
    systolic = _read(patient, "blood_pressure_systolic")
    diastolic = _read(patient, "blood_pressure_diastolic")

    if heart_rate < 50:
        base += 25
    elif heart_rate < 60:
        base += 8
    if heart_rate > 100:
        base += 10
    if heart_rate > 120:
        base += 10

    if systolic >= 180:
        base += 20
    elif systolic >= 160:
        base += 10
    elif systolic > 140:
        base += 5
    if systolic < 90:
        base += 25
    elif systolic < 100:
        base += 10

    if diastolic >= 110:
        base += 20
    elif diastolic >= 100:
        base += 10
    elif diastolic > 80:
        base += 5
    if diastolic < 50:
        base += 20
    elif diastolic < 60:
        base += 8

    return _round_score(min(base, 100))


def calculate_history_score(patient: Any) -> float:
    """Calculate history score from age and selected medical-history risk flags."""
    base = 0
    if _read(patient, "age") > 60:
        base += 20
    if _read(patient, "age") > 75:
        base += 15
    if _read(patient, "has_chronic_disease"):
        base += 25
        base += _keyword_score(_read(patient, "chronic_disease_description"), HIGH_RISK_HISTORY_KEYWORDS)
    return _round_score(min(base, 100))


def calculate_clinical_risk_score(
    symptom_score: float,
    image_score: float,
    history_score: float,
) -> float:
    """Calculate weighted clinical risk score using symptom, mock image, and history scores."""
    image_score_normalized = image_score * 100
    clinical_risk_score = (
        (0.45 * symptom_score)
        + (0.25 * image_score_normalized)
        + (0.30 * history_score)
    )
    return _round_score(clinical_risk_score)


def safety_risk_floor(patient: Any) -> float:
    """Return the minimum clinical risk implied by safety-first triage rules.

    The weighted formula remains the baseline model, but obvious red/yellow
    triage findings must also be visible in the numeric clinical risk score.
    Otherwise a patient can correctly become Critical by safety rule while the
    displayed clinical risk still looks deceptively low.
    """
    symptoms_description = _read(patient, "symptoms_description")
    floor = 0.0

    heart_rate = _read(patient, "heart_rate")
    systolic = _read(patient, "blood_pressure_systolic")
    diastolic = _read(patient, "blood_pressure_diastolic")

    if heart_rate > 130:
        floor = max(floor, CRITICAL_RISK_FLOOR)
    if heart_rate < 50:
        floor = max(floor, CRITICAL_RISK_FLOOR)
    if systolic < 90:
        floor = max(floor, CRITICAL_RISK_FLOOR)
    if systolic >= 180:
        floor = max(floor, CRITICAL_RISK_FLOOR)
    if diastolic >= 110:
        floor = max(floor, CRITICAL_RISK_FLOOR)
    if diastolic < 50:
        floor = max(floor, CRITICAL_RISK_FLOOR)
    if _contains_any(symptoms_description, CRITICAL_SYMPTOM_KEYWORDS):
        floor = max(floor, CRITICAL_RISK_FLOOR)

    if _read(patient, "pain_level") >= 9 and _read(patient, "fever"):
        floor = max(floor, URGENT_RISK_FLOOR)
    if _contains_any(symptoms_description, URGENT_SYMPTOM_KEYWORDS):
        floor = max(floor, URGENT_RISK_FLOOR)
    if _contains_any(symptoms_description, ("chest pain",)) and (
        _read(patient, "heart_rate") > 100 or _read(patient, "pain_level") >= 7
    ):
        floor = max(floor, URGENT_RISK_FLOOR)

    return floor


def calculate_waiting_time_factor(
    arrival_time: datetime,
    now_utc: datetime | None = None,
) -> tuple[float, float]:
    """Calculate waiting time factor and waiting minutes from UTC-aware datetimes."""
    current_time = ensure_utc(now_utc or datetime.now(timezone.utc))
    arrival_time_utc = ensure_utc(arrival_time)
    waiting_minutes = (current_time - arrival_time_utc).total_seconds() / 60
    waiting_time_factor = min(waiting_minutes * 0.15, 20)
    return _round_score(waiting_time_factor), waiting_minutes


def determine_triage_level(patient: Any, clinical_risk_score: float) -> tuple[str, list[str]]:
    """Apply safety-first triage rules in order and return the level with rationale reasons."""
    triage_level = "Stable"
    reasons: list[str] = []
    symptoms_description = _read(patient, "symptoms_description")

    heart_rate = _read(patient, "heart_rate")
    systolic = _read(patient, "blood_pressure_systolic")
    diastolic = _read(patient, "blood_pressure_diastolic")

    if heart_rate > 130:
        triage_level = _escalate(triage_level, "Critical")
        reasons.append("heart rate exceeds 130 bpm (safety rule triggered)")

    if heart_rate < 50:
        triage_level = _escalate(triage_level, "Critical")
        reasons.append("heart rate is below 50 bpm (safety rule triggered)")

    if systolic < 90:
        triage_level = _escalate(triage_level, "Critical")
        reasons.append("systolic blood pressure is below 90 mmHg (safety rule triggered)")

    if systolic >= 180:
        triage_level = _escalate(triage_level, "Critical")
        reasons.append("systolic blood pressure is 180 mmHg or higher (safety rule triggered)")

    if diastolic >= 110:
        triage_level = _escalate(triage_level, "Critical")
        reasons.append("diastolic blood pressure is 110 mmHg or higher (safety rule triggered)")

    if diastolic < 50:
        triage_level = _escalate(triage_level, "Critical")
        reasons.append("diastolic blood pressure is below 50 mmHg (safety rule triggered)")

    if _read(patient, "pain_level") >= 9 and _read(patient, "fever"):
        triage_level = _escalate(triage_level, "Urgent")
        reasons.append(f"pain level is {_read(patient, 'pain_level')} and fever is present")

    if _contains_any(symptoms_description, CRITICAL_SYMPTOM_KEYWORDS):
        triage_level = _escalate(triage_level, "Critical")
        reasons.append("primary survey red-flag symptom triggered Critical safety escalation")

    if _contains_any(symptoms_description, URGENT_SYMPTOM_KEYWORDS):
        triage_level = _escalate(triage_level, "Urgent")
        reasons.append("high-risk symptom selection triggered safety escalation")

    if _contains_any(symptoms_description, ("chest pain",)) and (
        _read(patient, "heart_rate") > 100 or _read(patient, "pain_level") >= 7
    ):
        triage_level = _escalate(triage_level, "Urgent")
        reasons.append("chest pain with elevated clinical severity triggered safety escalation")

    if clinical_risk_score >= 75:
        triage_level = _escalate(triage_level, "Critical")
        reasons.append(f"clinical risk score is {clinical_risk_score:.1f}")
    elif 72 <= clinical_risk_score < 75:
        triage_level = _escalate(triage_level, "Critical")
        reasons.append(
            f"clinical risk score is {clinical_risk_score:.1f}, within 3 points of the Critical threshold"
        )
    elif clinical_risk_score >= 45:
        triage_level = _escalate(triage_level, "Urgent")
        reasons.append(f"clinical risk score is {clinical_risk_score:.1f}")
    elif 42 <= clinical_risk_score < 45:
        triage_level = _escalate(triage_level, "Urgent")
        reasons.append(
            f"clinical risk score is {clinical_risk_score:.1f}, within 3 points of the Urgent threshold"
        )

    if not reasons:
        reasons.append("no critical vital signs detected")

    if not any("clinical risk score" in reason for reason in reasons):
        reasons.append(f"clinical risk score is {clinical_risk_score:.1f}")

    return triage_level, reasons


def max_waiting_constraint_applies(triage_level: str, waiting_minutes: float) -> bool:
    """Return True when a non-critical patient has exceeded the fairness waiting cap."""
    max_minutes = MAX_WAITING_MINUTES.get(triage_level)
    if max_minutes is None:
        return False
    return waiting_minutes >= max_minutes


def build_decision_rationale(
    triage_level: str,
    reasons: list[str],
    waiting_time_factor: float,
    waiting_minutes: float,
    max_waiting_constraint_applied: bool = False,
) -> str:
    """Build a non-empty human-readable rationale for a triage decision."""
    rationale_parts = list(reasons)
    if waiting_time_factor > 0:
        rationale_parts.append(
            f"waiting time factor of {waiting_time_factor:.1f} applied due to {max(waiting_minutes, 0):.0f} minutes in queue"
        )
    if max_waiting_constraint_applied:
        rationale_parts.append(
            "maximum waiting time constraint reached; queue sorting applies a fairness boost within the same triage group while Critical patients remain protected"
        )

    if not rationale_parts:
        rationale_parts.append("no critical vital signs detected")

    sentences = " ".join(_sentence(part) for part in rationale_parts)
    return f"Patient assigned {triage_level}: {sentences}"


def _format_override_reasons(override_reason: str) -> str:
    """Format selected override reasons as short audit-friendly clinical bullets."""
    reasons = [part.strip(" -") for part in override_reason.replace("\n", ";").split(";") if part.strip(" -")]
    if len(reasons) <= 1:
        return _sentence(override_reason)
    bullets = " ".join(f"- {_sentence(reason)}" for reason in reasons)
    return f"Selected clinical reasons: {bullets}"


def build_doctor_override_rationale(
    triage_level: str,
    override_reason: str,
    clinical_risk_score: float,
    waiting_time_factor: float,
) -> str:
    """Build a rationale when a clinician overrides the algorithmic triage level."""
    return (
        f"Patient assigned {triage_level} by clinician override review. "
        f"{_format_override_reasons(override_reason)} "
        f"Clinical risk score is {clinical_risk_score:.1f}. "
        f"Waiting time factor is {waiting_time_factor:.1f}."
    )


def infer_doctor_override_level(
    patient: Any,
    override_reason: str,
    requested_level: str | None = None,
) -> str:
    """Infer the safest override level from selected bedside clinical reasons.

    The mobile app no longer asks the clinician to manually choose Critical,
    Urgent, or Stable. It submits structured bedside concerns, and the backend
    derives the override level using the same safety-first ordering as the main
    triage engine. Legacy clients may still send requested_level; it can only
    escalate, never make the derived decision less safe.
    """
    baseline_level, _ = determine_triage_level(patient, _read(patient, "clinical_risk_score"))
    inferred_level = "Stable"
    normalized_reason = (override_reason or "").lower()

    if (
        _read(patient, "heart_rate") > 130
        or _read(patient, "heart_rate") < 50
        or _read(patient, "blood_pressure_systolic") < 90
        or _read(patient, "blood_pressure_systolic") >= 180
        or _read(patient, "blood_pressure_diastolic") >= 110
        or _read(patient, "blood_pressure_diastolic") < 50
    ):
        inferred_level = _escalate(inferred_level, "Critical")

    if _contains_any(normalized_reason, OVERRIDE_CRITICAL_KEYWORDS):
        inferred_level = _escalate(inferred_level, "Critical")
    elif _contains_any(normalized_reason, OVERRIDE_URGENT_KEYWORDS):
        inferred_level = _escalate(inferred_level, "Urgent")

    if requested_level:
        inferred_level = _escalate(inferred_level, requested_level)

    return _escalate(baseline_level, inferred_level)


def calculate_patient_scores(
    patient: Any,
    now_utc: datetime | None = None,
    preserve_doctor_override: bool = True,
) -> Any:
    """Calculate and write all score fields, triage level, and rationale for a patient."""
    symptom_score = calculate_symptom_score(patient)
    history_score = calculate_history_score(patient)
    clinical_risk_score = calculate_clinical_risk_score(
        symptom_score=symptom_score,
        image_score=_read(patient, "image_score"),
        history_score=history_score,
    )
    clinical_risk_score = _round_score(max(clinical_risk_score, safety_risk_floor(patient)))
    waiting_time_factor, waiting_minutes = calculate_waiting_time_factor(
        _read(patient, "arrival_time"),
        now_utc,
    )
    final_priority_score = _round_score(clinical_risk_score + waiting_time_factor)

    _set(patient, "symptom_score", symptom_score)
    _set(patient, "history_score", history_score)
    _set(patient, "clinical_risk_score", clinical_risk_score)
    _set(patient, "waiting_time_factor", waiting_time_factor)
    _set(patient, "final_priority_score", final_priority_score)

    is_overridden = bool(getattr(patient, "overridden_by_doctor", False))
    if preserve_doctor_override and is_overridden:
        triage_level = _read(patient, "triage_level")
        override_reason = getattr(patient, "doctor_override_reason", None) or "Clinician override applied."
        max_waiting_constraint_applied = max_waiting_constraint_applies(triage_level, waiting_minutes)
        rationale = build_doctor_override_rationale(
            triage_level,
            override_reason,
            clinical_risk_score,
            waiting_time_factor,
        )
        if max_waiting_constraint_applied:
            rationale += (
                " Maximum waiting time constraint is active for within-group queue fairness review; "
                "Critical patients remain protected above all non-critical cases."
            )
    else:
        triage_level, reasons = determine_triage_level(patient, clinical_risk_score)
        max_waiting_constraint_applied = max_waiting_constraint_applies(triage_level, waiting_minutes)
        rationale = build_decision_rationale(
            triage_level,
            reasons,
            waiting_time_factor,
            waiting_minutes,
            max_waiting_constraint_applied,
        )
        _set(patient, "triage_level", triage_level)

    _set(patient, "_max_waiting_constraint_applied", max_waiting_constraint_applied)
    _set(patient, "decision_rationale", rationale)
    return patient


def refresh_waiting_priority(patient: Any, now_utc: datetime | None = None) -> Any:
    """Refresh waiting time, final priority, and rationale without changing clinical inputs."""
    waiting_time_factor, waiting_minutes = calculate_waiting_time_factor(
        _read(patient, "arrival_time"),
        now_utc,
    )
    final_priority_score = _round_score(_read(patient, "clinical_risk_score") + waiting_time_factor)
    _set(patient, "waiting_time_factor", waiting_time_factor)
    _set(patient, "final_priority_score", final_priority_score)

    if bool(getattr(patient, "overridden_by_doctor", False)):
        override_reason = getattr(patient, "doctor_override_reason", None) or "Clinician override applied."
        max_waiting_constraint_applied = max_waiting_constraint_applies(_read(patient, "triage_level"), waiting_minutes)
        rationale = build_doctor_override_rationale(
            _read(patient, "triage_level"),
            override_reason,
            _read(patient, "clinical_risk_score"),
            waiting_time_factor,
        )
        if max_waiting_constraint_applied:
            rationale += (
                " Maximum waiting time constraint is active for within-group queue fairness review; "
                "Critical patients remain protected above all non-critical cases."
            )
    else:
        triage_level, reasons = determine_triage_level(patient, _read(patient, "clinical_risk_score"))
        _set(patient, "triage_level", triage_level)
        max_waiting_constraint_applied = max_waiting_constraint_applies(triage_level, waiting_minutes)
        rationale = build_decision_rationale(
            triage_level,
            reasons,
            waiting_time_factor,
            waiting_minutes,
            max_waiting_constraint_applied,
        )

    _set(patient, "_max_waiting_constraint_applied", max_waiting_constraint_applied)
    _set(patient, "decision_rationale", rationale)
    return patient


def queue_protection_rank(patient: Any) -> int:
    """Protect triage groups exactly as described in the product report."""
    return TRIAGE_RANK.get(_read(patient, "triage_level"), 2)


def fairness_rank(patient: Any) -> int:
    """Prioritize max-waiting patients within their own triage group."""
    return 0 if _has_ephemeral_flag(patient, "_max_waiting_constraint_applied") else 1


def sort_waiting_patients(waiting_patients: list[Any]) -> list[Any]:
    """Sort by triage group, max-waiting fairness, priority score, then arrival time."""
    return sorted(
        waiting_patients,
        key=lambda patient: (
            queue_protection_rank(patient),
            fairness_rank(patient),
            -_read(patient, "final_priority_score"),
            _read(patient, "arrival_time"),
        ),
    )


def recalculate_waiting_queue(
    waiting_patients: list[Any],
    now_utc: datetime | None = None,
) -> list[Any]:
    """Refresh scores for waiting patients, sort them, and assign queue positions."""
    current_time = ensure_utc(now_utc or datetime.now(timezone.utc))
    for patient in waiting_patients:
        refresh_waiting_priority(patient, current_time)

    sorted_patients = sort_waiting_patients(waiting_patients)
    for index, patient in enumerate(sorted_patients, start=1):
        _set(patient, "queue_position", index)
    return sorted_patients

