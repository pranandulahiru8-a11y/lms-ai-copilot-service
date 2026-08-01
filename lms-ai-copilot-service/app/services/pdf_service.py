from io import BytesIO
from pypdf import PdfReader


class PDFService:

    @staticmethod
    def extract_text_and_chunk(
        file_bytes: bytes, chunk_size: int = 500, overlap: int = 50
    ) -> list[str]:
        pdf = PdfReader(BytesIO(file_bytes))
        full_text = ""

        # PDF එකේ සෑම පිටුවකින්ම Text ලබා ගැනීම
        for page in pdf.pages:
            text = page.extract_text()
            if text:
                full_text += text + "\n"

        if not full_text.strip():
            return []

        # Sliding Window මගින් Text එක Chunks වලට කඩීම
        chunks = []
        start = 0
        text_length = len(full_text)

        while start < text_length:
            end = start + chunk_size
            chunk = full_text[start:end].strip()
            if chunk:
                chunks.append(chunk)
            start += chunk_size - overlap

        return chunks


pdf_service = PDFService()