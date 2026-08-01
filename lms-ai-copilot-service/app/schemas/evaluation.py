# Assessment Schemas
from typing import List
from pydantic import BaseModel

class EvaluationRequest(BaseModel):
    question: str
    reference_answer: str
    student_answer: str

class EvaluationResponse(BaseModel):
    score_out_of_10: int
    strengths: List[str]
    missing_points: List[str]
    feedback_for_student: str