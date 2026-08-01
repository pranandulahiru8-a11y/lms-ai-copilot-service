# FastAPI Application Entry Point

from app.core.config import settings
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers.quiz_router import router as quiz_router
from app.routers.evaluation_router import router as evaluation_router
from app.routers.rag_router import router as rag_router

app = FastAPI(
    title=settings.PROJECT_NAME,
    version=settings.VERSION,
    description="Enterprise AI Microservice for LMS Assessment & RAG Copilot",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register Routers
app.include_router(quiz_router)
app.include_router(evaluation_router)
app.include_router(rag_router)

@app.get("/api/v1/health", tags=["Health"])
async def health_check():
    return {
        "status": "online",
        "service": settings.PROJECT_NAME,
        "version": settings.VERSION,
    }