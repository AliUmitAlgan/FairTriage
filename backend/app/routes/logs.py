"""Decision log endpoints."""

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app import crud, schemas
from app.database import get_session


router = APIRouter(tags=["logs"])


@router.get(
    "/logs",
    response_model=list[schemas.DecisionLogRead],
    summary="List all decision logs",
)
async def get_logs(session: AsyncSession = Depends(get_session)):
    return await crud.list_logs(session)


@router.get(
    "/patients/{patient_id}/logs",
    response_model=list[schemas.DecisionLogRead],
    summary="List decision logs for one patient",
)
async def get_patient_logs(patient_id: int, session: AsyncSession = Depends(get_session)):
    patient = await crud.get_patient(session, patient_id)
    if patient is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Patient with id {patient_id} was not found.",
        )
    return await crud.list_patient_logs(session, patient_id)
