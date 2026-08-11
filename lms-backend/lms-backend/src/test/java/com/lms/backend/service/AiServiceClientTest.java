package com.lms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.backend.dto.EvaluationRequestDto;
import com.lms.backend.dto.EvaluationResponseDto;
import com.lms.backend.dto.QuizRequestDto;
import com.lms.backend.dto.QuizResponseDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class AiServiceClientTest {

    private static MockWebServer mockWebServer;
    private AiServiceClient aiServiceClient;
    private ObjectMapper objectMapper;

    @BeforeAll
    static void setUpAll() throws IOException {
        // Starting the Mock Web Server for the Test
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDownAll() throws IOException {
        // Shutting down the server
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        
        // Providing the URL of the MockWebServer as the Base URL to the WebClient
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        aiServiceClient = new AiServiceClient(WebClient.builder(), baseUrl);
    }

    @Test
    @DisplayName("Health Check: Should return status OK from FastAPI")
    void checkHealth_Success() throws InterruptedException {
        // Given (Queuing a Mock Response to the Server)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("FastAPI Healthy"));

        // When & Then (Testing Reactive Mono with StepVerifier)
        StepVerifier.create(aiServiceClient.checkHealth())
                .expectNext("FastAPI Healthy")
                .verifyComplete();

        // Recorded Request verification (checks if HTTP Path & Method is correct)
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo("/health");
    }

    @Test
    @DisplayName("Quiz Generation: Should send POST request and deserialize QuizResponseDto")
    void generateQuiz_Success() throws Exception {
        // Given
        QuizRequestDto requestDto = new QuizRequestDto();
        QuizResponseDto expectedResponse = new QuizResponseDto();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(expectedResponse)));

        // When & Then
        StepVerifier.create(aiServiceClient.generateQuiz(requestDto))
                .expectNextMatches(response -> response != null)
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/quiz/generate");
    }

    @Test
    @DisplayName("Assessment Evaluation: Should send request to FastAPI endpoint successfully")
    void evaluateAssessment_Success() throws Exception {
        // Given
        EvaluationRequestDto requestDto = new EvaluationRequestDto();
        EvaluationResponseDto expectedResponse = new EvaluationResponseDto();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(expectedResponse)));

        // When & Then
        StepVerifier.create(aiServiceClient.evaluateAssessment(requestDto))
                .expectNextMatches(response -> response != null)
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/assessment/evaluate");
    }

    @Test
    @DisplayName("PDF Ingestion: Should send multipart request to /rag/ingest-pdf")
    void ingestPdf_Success() throws InterruptedException {

        
        // Given
        byte[] pdfBytes = java.util.Objects.requireNonNull(
        "Dummy PDF Content".getBytes(java.nio.charset.StandardCharsets.UTF_8)
);

        Resource pdfResource = new ByteArrayResource(pdfBytes) {
            @Override
            public String getFilename() {
                return "test-document.pdf";
            }
        };
        

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("PDF Ingested Successfully"));

        // When & Then
        StepVerifier.create(aiServiceClient.ingestPdf(pdfResource))
                .expectNext("PDF Ingested Successfully")
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/rag/ingest-pdf");
        assertThat(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE)).contains("multipart/form-data");
    }

    @Test
    @DisplayName("RAG Quiz Generation: Should send POST request to /rag/generate-quiz")
    void generateRagQuiz_Success() throws Exception {
        // Given
        QuizRequestDto requestDto = new QuizRequestDto();
        QuizResponseDto expectedResponse = new QuizResponseDto();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(expectedResponse)));

        // When & Then
        StepVerifier.create(aiServiceClient.generateRagQuiz(requestDto))
                .expectNextMatches(response -> response != null)
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/rag/generate-quiz");
    }
}