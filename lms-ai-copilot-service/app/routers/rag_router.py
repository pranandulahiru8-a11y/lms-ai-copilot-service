from app.services.pdf_service import pdf_service
from app.services.vector_store_service import vector_store_service
from fastapi import APIRouter, File, HTTPException, UploadFile

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