# Quiz Endpoints
from app.schemas.quiz import QuizRequest, QuizResponse
from app.services.groq_service import groq_service
from fastapi import APIRouter, HTTPException

router = APIRouter(prefix="/api/v1/quiz", tags=["Quiz Generator"])

@router.post("/generate", response_model=QuizResponse)
async def generate_quiz(request: QuizRequest):
    try:
        quiz_data = await groq_service.generate_quiz_json(
            topic=request.topic,
            num_questions=request.num_questions,
            difficulty=request.difficulty,
        )
        return quiz_data
    except Exception as e:
        raise HTTPException(
            status_code=500, detail=f"Quiz Generation Failed: {str(e)}"
        )