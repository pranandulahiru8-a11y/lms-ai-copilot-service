package com.lms.backend.service;

import com.lms.backend.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AiServiceClient {

    private final WebClient webClient;

    public AiServiceClient(WebClient.Builder webClientBuilder,
            @Value("${ai.service.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    // 1. Health Check Endpoint (/api/v1/health)
    public Mono<String> checkHealth() {
        return this.webClient.get()
                .uri("/health")
                .retrieve()
                .bodyToMono(String.class);
    }

    // 2. Quiz Generation Endpoint (/api/v1/quiz/generate)
    public Mono<QuizResponseDto> generateQuiz(QuizRequestDto requestDto) {
        return this.webClient.post()
                .uri("/quiz/generate")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(QuizResponseDto.class);
    }

    // 3. Assessment Evaluation Endpoint (/api/v1/assessment/evaluate)
    public Mono<EvaluationResponseDto> evaluateAssessment(EvaluationRequestDto requestDto) {
        return this.webClient.post()
                .uri("/assessment/evaluate")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(EvaluationResponseDto.class);
    }

    // 4. RAG - PDF Ingestion Endpoint (/api/v1/rag/ingest-pdf)
    public Mono<String> ingestPdf(Resource pdfResource) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", pdfResource);

        return this.webClient.post()
                .uri("/rag/ingest-pdf")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .retrieve()
                .bodyToMono(String.class);
    }

    // 5. RAG - Generate Quiz from Ingested Context (/api/v1/rag/generate-quiz)
    public Mono<QuizResponseDto> generateRagQuiz(QuizRequestDto requestDto) {
        return this.webClient.post()
                .uri("/rag/generate-quiz")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(QuizResponseDto.class);
    }
}