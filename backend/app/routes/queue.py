"""Queue endpoints."""

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app import crud, schemas
from app.database import get_session


router = APIRouter(tags=["queue"])


@router.get(
    "/queue",
    response_model=list[schemas.PatientRead],
    summary="Recalculate and return the waiting queue",
)
async def get_queue(session: AsyncSession = Depends(get_session)):
    waiting_queue = await crud.recalculate_queue(session)
    await session.commit()
    return waiting_queue
