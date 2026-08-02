# Base Image
FROM python:3.10-slim

# Set Working Directory
WORKDIR /app

# Set Environment Variables
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# Install Dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy Project Files
COPY . .

# Expose FastAPI Port
EXPOSE 8000

# Start Uvicorn Server
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]