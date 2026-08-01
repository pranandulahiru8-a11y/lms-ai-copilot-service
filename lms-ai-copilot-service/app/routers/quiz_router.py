# Quiz Endpoints
from app.schemas.quiz import QuizRequest, QuizResponse
from app.services.groq_service import groq_service
from fastapi import APIRouter, HTTPException
from app.services.guardrail_service import guardrail_service

router = APIRouter(prefix="/api/v1/quiz", tags=["Quiz Generator"])

@router.post("/generate", response_model=QuizResponse)
async def generate_quiz(request: QuizRequest):

    # 1. Input Security Guardrail Check
    guardrail_service.validate_input_prompt(request.topic)
    
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