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


groq_service = GroqAIService()