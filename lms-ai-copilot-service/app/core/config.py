# Environment & App Configurations
import os
from dotenv import load_dotenv

load_dotenv()

class Settings:
    PROJECT_NAME: str = "LMS AI Copilot Microservice"
    VERSION: str = "1.0.0"
    GROQ_API_KEY: str = os.getenv("GROQ_API_KEY", "")

    if not GROQ_API_KEY:
        raise RuntimeError("GROQ_API_KEY environment variable is missing!")


settings = Settings()