package com.lms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.backend.dto.QuizRequestDto;
import com.lms.backend.dto.QuizResponseDto;
import com.lms.backend.service.AiServiceClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiServiceClient aiServiceClient;

    @Test
    @WithMockUser(username = "instructor@lms.com", roles = { "INSTRUCTOR" })
    @DisplayName("POST /api/v1/rag/ingest-pdf: Should successfully upload PDF and return success message")
    void ingestPdf_Success() throws Exception {
        // Create Mock PDF File
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "sample-lecture.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Dummy PDF content for LMS RAG ingestion".getBytes());

        when(aiServiceClient.ingestPdf(any()))
                .thenReturn(Mono.just("PDF Ingested Successfully"));

        // Execute Async Multipart Request
        MvcResult mvcResult = mockMvc.perform(multipart("/api/v1/rag/ingest-pdf")
                .file(mockFile))
                .andExpect(request().asyncStarted())
                .andReturn();

        String responseString = mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(responseString).isEqualTo("PDF Ingested Successfully");
    }

    @Test
    @WithMockUser(username = "student@lms.com", roles = { "STUDENT" })
    @DisplayName("POST /api/v1/rag/generate-quiz: Should generate RAG quiz from ingested PDF")
    void generateRagQuiz_Success() throws Exception {
        QuizRequestDto requestDto = new QuizRequestDto();
        requestDto.setTopic("Spring Security & JWT");

        QuizResponseDto mockResponse = new QuizResponseDto();
        mockResponse.setTopic("Spring Security & JWT");

        when(aiServiceClient.generateRagQuiz(any(QuizRequestDto.class)))
                .thenReturn(Mono.just(mockResponse));

        // Execute Async Post Request
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/rag/generate-quiz")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(request().asyncStarted())
                .andReturn();

        String responseString = mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(responseString).contains("Spring Security & JWT");
    }
}