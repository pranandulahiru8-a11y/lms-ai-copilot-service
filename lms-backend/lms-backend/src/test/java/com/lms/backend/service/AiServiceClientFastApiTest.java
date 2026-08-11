package com.lms.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.backend.dto.QuizRequestDto;
import com.lms.backend.dto.QuizResponseDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;



class AiServiceClientFastApiTest {

    private MockWebServer mockWebServer;
    private AiServiceClient aiServiceClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // MockWebServer එකෙන් dynamic එකට ලැබෙන URL එක
        String mockBaseUrl = mockWebServer.url("/").toString();

        // Constructor එකට WebClient.builder() සහ mockBaseUrl එක Pass කිරීම
        aiServiceClient = new AiServiceClient(WebClient.builder(), mockBaseUrl);

        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("FastAPI Mock: Should successfully generate quiz from external AI microservice")
    void generateQuiz_FastApiSuccess() throws Exception {
        // 1. Prepare Mock Response
        QuizResponseDto mockResponse = new QuizResponseDto();
        mockResponse.setTopic("Spring Boot & Microservices");

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(mockResponse)));

        QuizRequestDto request = new QuizRequestDto();
        request.setTopic("Spring Boot & Microservices");

        // 2. Test Execution
        StepVerifier.create(aiServiceClient.generateQuiz(request))
                .expectNextMatches(response -> response != null && "Spring Boot & Microservices".equals(response.getTopic()))
                .verifyComplete();
    }

    @Test
    @DisplayName("FastAPI Mock: Should handle 500 Internal Server Error when FastAPI fails")
    void generateQuiz_FastApiServerError() {
        // Enqueue 500 Error Response from FastAPI
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("FastAPI Internal Server Error"));

        QuizRequestDto request = new QuizRequestDto();
        request.setTopic("Java Basics");

        StepVerifier.create(aiServiceClient.generateQuiz(request))
                .expectError()
                .verify();
    }

    @Test
    @DisplayName("FastAPI Mock: Should successfully mock PDF document ingestion endpoint")
    void ingestPdf_FastApiSuccess() throws Exception {
        // Mock Response එක JSON සහ TEXT 2ටම Support වන පරිදි Set කිරීම
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("\"PDF processed and vector index updated successfully\"")); // String as JSON

        byte[] pdfBytes = java.util.Objects.requireNonNull(
                "Sample PDF Text".getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        ByteArrayResource resource = new ByteArrayResource(pdfBytes) {
            @Override
            public String getFilename() {
                return "lecture.pdf";
            }
        };

        StepVerifier.create(aiServiceClient.ingestPdf(resource))
                .expectNextMatches(res -> res != null && res.contains("PDF processed"))
                .verifyComplete();
    }

    @Test
    @DisplayName("FastAPI Mock: Should return health status from external service")
    void checkHealth_FastApiSuccess() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("FastAPI Service Running"));

        StepVerifier.create(aiServiceClient.checkHealth())
                .expectNext("FastAPI Service Running")
                .verifyComplete();
    }
}