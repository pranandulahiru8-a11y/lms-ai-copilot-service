import re
from fastapi import HTTPException

# Security Patterns for Prompt Injections
PROMPT_INJECTION_PATTERNS = [
    r"ignore previous instructions",
    r"bypass security",
    r"system prompt",
    r"reveal secret key",
    r"act as root",
    r"forget your rules",
    r"drop database",
]


class GuardrailService:

    @staticmethod
    def validate_input_prompt(user_text: str) -> bool:
        # Check 1: Empty or extremely short input
        if not user_text or len(user_text.strip()) < 3:
            raise HTTPException(
                status_code=400,
                detail=(
                    "Input validation failed: Prompt is too short or empty."
                ),
            )

        # Check 2: Prompt Injection Detection using Regex
        for pattern in PROMPT_INJECTION_PATTERNS:
            if re.search(pattern, user_text, re.IGNORECASE):
                raise HTTPException(
                    status_code=400,
                    detail=(
                        "Security Guardrail Blocked: Malicious prompt injection"
                        " attempt detected."
                    ),
                )

        return True


guardrail_service = GuardrailService()