# Quiz Endpoints
from app.schemas.quiz import QuizRequest, QuizResponse
from app.services.groq_service import groq_service
from fastapi import APIRouter, HTTPException
from app.services.guardrail_service import guardrail_service
from app.services.cache_service import cache_service

router = APIRouter(prefix="/api/v1/quiz", tags=["Quiz Generator"])

@router.post("/generate", response_model=QuizResponse)
async def generate_quiz(request: QuizRequest):

    # 1. Input Security Guardrail Check
    guardrail_service.validate_input_prompt(request.topic)

    # 2. Check Cache
    cached_quiz = cache_service.get(
        prefix="quiz",
        topic=request.topic,
        num_questions=request.num_questions,
        difficulty=request.difficulty,
    )
    if cached_quiz:
        return cached_quiz
    
    try:
        # 3. Call LLM Service if Cache Miss
        quiz_data = await groq_service.generate_quiz_json(
            topic=request.topic,
            num_questions=request.num_questions,
            difficulty=request.difficulty,
        )

        # 4. Save to Cache for Future Requests
        cache_service.set(
            prefix="quiz",
            value=quiz_data,
            topic=request.topic,
            num_questions=request.num_questions,
            difficulty=request.difficulty,
        )
        
        return quiz_data
    except Exception as e:
        raise HTTPException(
            status_code=500, detail=f"Quiz Generation Failed: {str(e)}"
        )