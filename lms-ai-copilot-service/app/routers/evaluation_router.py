# Evaluation Endpoints
from app.schemas.evaluation import EvaluationRequest, EvaluationResponse
from app.services.groq_service import groq_service
from fastapi import APIRouter, HTTPException
from app.services.guardrail_service import guardrail_service

router = APIRouter(prefix="/api/v1/assessment", tags=["Answer Evaluator"])


@router.post("/evaluate", response_model=EvaluationResponse)
async def evaluate_submission(request: EvaluationRequest):

    # 1. Input Security Guardrail Check
    guardrail_service.validate_input_prompt(request.question)
    guardrail_service.validate_input_prompt(request.reference_answer)
    guardrail_service.validate_input_prompt(request.student_answer)

    try:
        evaluation_data = await groq_service.evaluate_submission_json(
            question=request.question,
            reference_answer=request.reference_answer,
            student_answer=request.student_answer,
        )
        return evaluation_data
    except Exception as e:
        raise HTTPException(
            status_code=500, detail=f"Answer Evaluation Failed: {str(e)}"
        )