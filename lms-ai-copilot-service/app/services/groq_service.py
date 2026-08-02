# Groq API Client & Business Logic
import json
from app.core.config import settings
from groq import Groq

groq_client = Groq(api_key=settings.GROQ_API_KEY)

class GroqAIService:
    
    @staticmethod
    async def generate_quiz_json(
        topic: str, num_questions: int, difficulty: str
    ) -> dict:
        prompt = f"""
        Generate a multiple-choice quiz based on the following specifications:
        Topic: {topic}
        Number of Questions: {num_questions}
        Difficulty Level: {difficulty}

        Return ONLY a valid JSON object matching this exact structure:
        {{
            "topic": "{topic}",
            "difficulty": "{difficulty}",
            "total_questions": {num_questions},
            "questions": [
                {{
                    "question_id": 1,
                    "question": "Question text here?",
                    "options": ["Option A", "Option B", "Option C", "Option D"],
                    "correct_option_index": 0,
                    "explanation": "Why Option A is correct."
                }}
            ]
        }}
        """

        response = groq_client.chat.completions.create(
            model="llama-3.3-70b-versatile",
            messages=[
                {
                    "role": "system",
                    "content": (
                        "You are an expert academic examiner. Always respond in"
                        " pure JSON format."
                    ),
                },
                {"role": "user", "content": prompt},
            ],
            response_format={"type": "json_object"},
            temperature=0.4,
        )

        return json.loads(response.choices[0].message.content)

    @staticmethod
    async def evaluate_submission_json(
        question: str, reference_answer: str, student_answer: str
    ) -> dict:
        prompt = f"""
        Evaluate the student's submission based on the question and reference answer provided.

        Question: {question}
        Reference (Model) Answer: {reference_answer}
        Student's Answer: {student_answer}

        Provide a constructive evaluation in ONLY this exact JSON format:
        {{
            "score_out_of_10": 8,
            "strengths": ["Clear understanding of concept A"],
            "missing_points": ["Did not mention detail B"],
            "feedback_for_student": "Encouraging summary feedback here."
        }}
        """

        response = groq_client.chat.completions.create(
            model="llama-3.3-70b-versatile",
            messages=[
                {
                    "role": "system",
                    "content": (
                        "You are an objective academic grader. Evaluate"
                        " fairly and respond ONLY in valid JSON."
                    ),
                },
                {"role": "user", "content": prompt},
            ],
            response_format={"type": "json_object"},
            temperature=0.2,
        )

        return json.loads(response.choices[0].message.content)

    @staticmethod
    async def generate_rag_quiz_json(
        topic: str, context: str, num_questions: int, difficulty: str
    ) -> dict:
        prompt = f"""
        Generate a multiple-choice quiz STRICTLY based on the provided Context below.
        Do NOT use outside knowledge. If the context is insufficient, generate as many questions as possible from the context.

        Context:
        {context}

        Specifications:
        Topic: {topic}
        Number of Questions: {num_questions}
        Difficulty Level: {difficulty}

        Return ONLY a valid JSON object matching this exact structure:
        {{
            "topic": "{topic}",
            "difficulty": "{difficulty}",
            "total_questions": {num_questions},
            "questions": [
                {{
                    "question_id": 1,
                    "question": "Question text based on context?",
                    "options": ["Option A", "Option B", "Option C", "Option D"],
                    "correct_option_index": 0,
                    "explanation": "Explanation based on provided lecture notes context."
                }}
            ]
        }}
        """

        response = groq_client.chat.completions.create(
            model="llama-3.3-70b-versatile",
            messages=[
                {
                    "role": "system",
                    "content": (
                        "You are an academic examiner creating questions"
                        " strictly from provided lecture materials. Always"
                        " respond in pure JSON."
                    ),
                },
                {"role": "user", "content": prompt},
            ],
            response_format={"type": "json_object"},
            temperature=0.3,
        )

        return json.loads(response.choices[0].message.content)

    


groq_service = GroqAIService()