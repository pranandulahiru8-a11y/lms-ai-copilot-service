from app.services.pdf_service import pdf_service
from app.services.vector_store_service import vector_store_service
from fastapi import APIRouter, File, HTTPException, UploadFile
from app.schemas.quiz import QuizRequest, QuizResponse
from app.services.groq_service import groq_service
from app.services.guardrail_service import guardrail_service
from app.services.cache_service import cache_service

router = APIRouter(prefix="/api/v1/rag", tags=["RAG Engine"])


@router.post("/ingest-pdf")
async def ingest_pdf(file: UploadFile = File(...)):
    if not file.filename.endswith(".pdf"):
        raise HTTPException(
            status_code=400, detail="Only PDF files are supported!"
        )

    try:
        contents = await file.read()
        chunks = pdf_service.extract_text_and_chunk(contents)

        if not chunks:
            raise HTTPException(
                status_code=400,
                detail="Could not extract readable text from PDF.",
            )

        # ChromaDB වෙත Chunks එකතු කිරීම
        vector_store_service.add_documents(
            documents=chunks, metadata={"filename": file.filename}
        )

        return {
            "status": "success",
            "filename": file.filename,
            "total_chunks_stored": len(chunks),
            "message": (
                "Lecture PDF successfully processed and vectorized in"
                " ChromaDB."
            ),
        }

    except Exception as e:
        raise HTTPException(
            status_code=500, detail=f"PDF Ingestion Failed: {str(e)}"
        )


@router.post("/generate-quiz", response_model=QuizResponse)
async def generate_rag_quiz(request: QuizRequest):

    # 1. Input Security Guardrail Check
    guardrail_service.validate_input_prompt(request.topic)

    # 2. Check Cache First
    cached_rag_quiz = cache_service.get(
        prefix="rag_quiz",
        topic=request.topic,
        num_questions=request.num_questions,
        difficulty=request.difficulty,
    )
    if cached_rag_quiz:
        return cached_rag_quiz

    try:
        # Fetch from Vector DB & LLM on Cache Miss
        retrieved_chunks = vector_store_service.query_relevant_chunks(
            query=request.topic, n_results=4
        )

        if not retrieved_chunks:
            raise HTTPException(
                status_code=404,
                detail=(
                    "No relevant lecture notes found in ChromaDB for this"
                    " topic. Please upload a PDF first."
                ),
            )

        # 2. Context Strings එකතු කිරීම
        context_text = "\n---\n".join(retrieved_chunks)

        # 3. Context-aware Quiz එකක් Generate කිරීම
        quiz_data = await groq_service.generate_rag_quiz_json(
            topic=request.topic,
            context=context_text,
            num_questions=request.num_questions,
            difficulty=request.difficulty,
        )

        # Save to Cache
        cache_service.set(
            prefix="rag_quiz",
            value=quiz_data,
            topic=request.topic,
            num_questions=request.num_questions,
            difficulty=request.difficulty,
        )

        return quiz_data

    except Exception as e:
        raise HTTPException(
            status_code=500, detail=f"RAG Quiz Generation Failed: {str(e)}"
        )