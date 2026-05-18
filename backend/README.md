# FairTriage Backend

FairTriage is a university prototype backend for AI-assisted clinical triage and dynamic patient queueing. It calculates deterministic risk scores from symptoms, patient history, mock image analysis output, and waiting time.

Important safety note: this project does not provide real medical diagnosis capability. It is not a medical device and must not replace professional clinical judgment.

## Tech Stack

- Python 3.11+
- FastAPI
- Pydantic v2
- SQLAlchemy async ORM
- SQLite with aiosqlite
- Uvicorn

## Setup

```bash
pip install -r requirements.txt
```

## Run

```bash
uvicorn app.main:app --reload --port 500
```

The API will be available at:

- API: `http://127.0.0.1:500`
- Swagger docs: `http://127.0.0.1:500/docs`
- Health check: `http://127.0.0.1:500/health`

## Main Endpoints

- `GET /health`
- `POST /patients`
- `GET /patients`
- `GET /patients/{id}`
- `PUT /patients/{id}`
- `DELETE /patients/{id}`
- `GET /queue`
- `POST /patients/{id}/recalculate`
- `POST /patients/{id}/override`
- `POST /patients/{id}/complete`
- `GET /logs`
- `GET /patients/{id}/logs`
- `POST /simulation/seed-demo-data`
- `DELETE /simulation/reset`

## Doctor Override Body

```json
{
  "new_triage_level": "Critical",
  "override_reasons": [
    "Abnormal heart rate",
    "Symptoms suggest clinical deterioration",
    "Clinical judgement after in-person assessment"
  ],
  "override_reason": "Abnormal heart rate; Symptoms suggest clinical deterioration; Clinical judgement after in-person assessment"
}
```

## Datetime Policy

All datetimes are timezone-aware UTC datetimes. The code uses `datetime.now(timezone.utc)` and stores UTC ISO-8601 strings in SQLite so API responses remain timezone-aware.

## Demo Data

Seed the fixed eight-patient demo dataset:

```bash
curl -X POST http://127.0.0.1:500/simulation/seed-demo-data
```

Reset all patients and logs:

```bash
curl -X DELETE http://127.0.0.1:500/simulation/reset
```

