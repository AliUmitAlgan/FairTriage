"""Patient CRUD and workflow endpoints."""

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app import crud, schemas
from app.database import get_session


router = APIRouter(prefix="/patients", tags=["patients"])


async def _get_patient_or_404(patient_id: int, session: AsyncSession):
    patient = await crud.get_patient(session, patient_id)
    if patient is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Patient with id {patient_id} was not found.",
        )
    return patient


@router.post(
    "",
    response_model=schemas.PatientRead,
    status_code=status.HTTP_201_CREATED,
    summary="Create patient",
)
async def create_patient(
    payload: schemas.PatientCreate,
    session: AsyncSession = Depends(get_session),
):
    return await crud.create_patient(session, payload)


@router.get(
    "",
    response_model=list[schemas.PatientRead],
    summary="List all patients",
)
async def list_patients(session: AsyncSession = Depends(get_session)):
    await crud.recalculate_queue(session, log_position_changes=False)
    await session.commit()
    return await crud.list_patients(session)


@router.get(
    "/{patient_id}",
    response_model=schemas.PatientRead,
    summary="Get a single patient",
)
async def get_patient(patient_id: int, session: AsyncSession = Depends(get_session)):
    await crud.recalculate_queue(session, log_position_changes=False)
    await session.commit()
    return await _get_patient_or_404(patient_id, session)


@router.put(
    "/{patient_id}",
    response_model=schemas.PatientRead,
    summary="Update patient and recalculate scores",
)
async def update_patient(
    patient_id: int,
    payload: schemas.PatientUpdate,
    session: AsyncSession = Depends(get_session),
):
    patient = await _get_patient_or_404(patient_id, session)
    return await crud.update_patient(session, patient, payload)


@router.delete(
    "/{patient_id}",
    response_model=schemas.MessageResponse,
    summary="Delete patient",
)
async def delete_patient(patient_id: int, session: AsyncSession = Depends(get_session)):
    patient = await _get_patient_or_404(patient_id, session)
    await crud.delete_patient(session, patient)
    return {"message": f"Patient with id {patient_id} deleted."}


@router.post(
    "/{patient_id}/recalculate",
    response_model=schemas.PatientRead,
    summary="Force recalculate scores for one patient",
)
async def recalculate_patient(patient_id: int, session: AsyncSession = Depends(get_session)):
    patient = await _get_patient_or_404(patient_id, session)
    return await crud.recalculate_patient(session, patient)


@router.post(
    "/{patient_id}/override",
    response_model=schemas.PatientRead,
    summary="Doctor override triage level",
)
async def override_patient(
    patient_id: int,
    payload: schemas.DoctorOverrideRequest,
    session: AsyncSession = Depends(get_session),
):
    patient = await _get_patient_or_404(patient_id, session)
    return await crud.override_patient_triage(session, patient, payload)


@router.post(
    "/{patient_id}/complete",
    response_model=schemas.PatientRead,
    summary="Mark patient as completed",
)
async def complete_patient(patient_id: int, session: AsyncSession = Depends(get_session)):
    patient = await _get_patient_or_404(patient_id, session)
    return await crud.complete_patient(session, patient)
