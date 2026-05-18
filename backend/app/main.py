"""FastAPI application entrypoint for FairTriage."""

from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.database import init_db
from app.routes import logs, patients, product, queue, simulation


@asynccontextmanager
async def lifespan(_app: FastAPI):
    """Initialize database tables when the API starts."""
    await init_db()
    yield


app = FastAPI(
    title="FairTriage API",
    version="0.1.0",
    description=(
        "University prototype backend for AI-assisted clinical triage queueing. "
        "This system does not provide real medical diagnoses and must not be used "
        "as a substitute for professional clinical judgment."
    ),
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(patients.router)
app.include_router(queue.router)
app.include_router(logs.router)
app.include_router(product.router)
app.include_router(simulation.router)


@app.get("/health", tags=["health"])
async def health_check() -> dict[str, str]:
    """Return API health status."""
    return {"status": "ok"}
