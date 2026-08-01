import uuid
import chromadb


class VectorStoreService:

    def __init__(self):
        # Local persistent folder එකක ChromaDB Data Save වේ
        self.client = chromadb.PersistentClient(path="./chroma_db")
        self.collection = self.client.get_or_create_collection(
            name="lms_lecture_notes"
        )

    def add_documents(self, documents: list[str], metadata: dict):
        ids = [f"doc_{uuid.uuid4().hex[:8]}" for _ in documents]
        metadatas = [metadata for _ in documents]

        self.collection.add(
            documents=documents, ids=ids, metadatas=metadatas
        )

    def query_relevant_chunks(self, query: str, n_results: int = 3) -> list[str]:
        results = self.collection.query(
            query_texts=[query], n_results=n_results
        )

        if results and results.get("documents") and results["documents"]:
            return results["documents"][0]
        return []


vector_store_service = VectorStoreService()