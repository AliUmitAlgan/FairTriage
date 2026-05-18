"""Simulation and demo-data endpoints."""

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app import crud, schemas
from app.database import get_session


router = APIRouter(prefix="/simulation", tags=["simulation"])


@router.post(
    "/seed-demo-data",
    response_model=schemas.CountMessageResponse,
    summary="Create eight idempotent demo patients",
)
async def seed_demo_data(session: AsyncSession = Depends(get_session)):
    message, count = await crud.seed_demo_data(session)
    return {"message": message, "count": count}


@router.delete(
    "/reset",
    response_model=schemas.CountMessageResponse,
    summary="Delete all patients and logs",
)
async def reset_simulation(session: AsyncSession = Depends(get_session)):
    count = await crud.reset_simulation(session)
    return {"message": "Simulation reset completed", "count": count}
