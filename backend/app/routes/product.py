"""Product policy and capability endpoints for FairTriage."""

from fastapi import APIRouter

from app import schemas
from app.triage_engine import MAX_WAITING_MINUTES


router = APIRouter(prefix="/product", tags=["product"])


@router.get(
    "/policy",
    response_model=schemas.ProductPolicyRead,
    summary="Return report-aligned product policy metadata",
)
async def get_product_policy() -> schemas.ProductPolicyRead:
    """Expose the operating policy used by backend and mobile clients."""
    return schemas.ProductPolicyRead(
        product_name="FairTriage",
        prototype_disclaimer="Prototype only. Not for real medical diagnosis.",
        clinical_control_policy=(
            "FairTriage is a clinical decision-support system. The doctor or triage nurse "
            "keeps final authority and can override any AI recommendation."
        ),
        scoring_formula="Clinical Risk = 0.45 * Symptoms + 0.25 * Image + 0.30 * History",
        safety_rules=[
            "Heart rate above 130 bpm assigns at least Critical priority.",
            "Systolic blood pressure below 90 mmHg assigns Critical priority.",
            "Pain level 9 or higher with fever assigns at least Urgent priority.",
            "Clinical risk within 3 points of a higher threshold escalates to the safer level.",
            "High-risk selected symptoms can escalate to Urgent priority.",
        ],
        fairness_policy=(
            "Critical patients remain protected above all non-critical patients. Within each "
            "triage group, final priority score, waiting duration, maximum waiting constraints, "
            "and arrival time determine ordering."
        ),
        max_waiting_minutes=MAX_WAITING_MINUTES,
        audit_log_actions=["created", "score_calculated", "queue_updated", "doctor_override", "completed"],
        privacy_policy=(
            "Collect only triage-relevant fields, require explicit prototype consent in the "
            "client, and avoid storing unrelated patient data."
        ),
        offline_policy=(
            "Mobile clients may show cached queue/log data and calculate local edge estimates "
            "while offline; backend remains the source of truth after synchronization."
        ),
    )
