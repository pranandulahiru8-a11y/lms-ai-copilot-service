# Quiz Request/Response Schemas
from typing import List
from pydantic import BaseModel, Field

class QuizRequest(BaseModel):
    topic: str
    num_questions: int = Field(default=3, ge=1, le=10)
    difficulty: str = Field(
        default="Medium", description="Easy, Medium, or Hard"
    )

class QuestionMCQ(BaseModel):
    question_id: int
    question: str
    options: List[str]
    correct_option_index: int
    explanation: str

class QuizResponse(BaseModel):
    topic: str
    difficulty: str
    total_questions: int
    questions: List[QuestionMCQ]