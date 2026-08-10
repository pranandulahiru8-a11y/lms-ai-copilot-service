package com.lms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.backend.dto.EvaluationRequestDto;
import com.lms.backend.dto.EvaluationResponseDto;
import com.lms.backend.dto.QuizRequestDto;
import com.lms.backend.dto.QuizResponseDto;
import com.lms.backend.entity.AssessmentResult;
import com.lms.backend.repository.AssessmentRepository;
import com.lms.backend.repository.AssessmentResultRepository;
import com.lms.backend.service.AiServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean; // 💡 Spring Boot 3.4+ නම් org.springframework.test.context.bean.override.mockito.MockitoBean ලෙස මාරු කරන්න
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AssessmentControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private AssessmentRepository assessmentRepository;

        @Autowired
        private AssessmentResultRepository assessmentResultRepository;

        @MockBean
        private AiServiceClient aiServiceClient;

        @BeforeEach
        void setUp() {
                assessmentResultRepository.deleteAll();
                assessmentRepository.deleteAll();
        }

        @Test
        @DisplayName("Security: Unauthorized access to protected endpoints should be rejected")
        void generateAssessment_Unauthenticated_ShouldBeRejected() throws Exception {
                QuizRequestDto requestDto = new QuizRequestDto();
                requestDto.setTopic("Spring Boot Security");

                mockMvc.perform(post("/api/v1/quiz/generate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                                .andExpect(status().is4xxClientError());
        }

        @Test
        @WithMockUser(username = "baba@lms.com", roles = { "STUDENT" })
        @DisplayName("POST /quiz/generate: Authorized student should generate and save quiz to DB")
        void generateAssessment_AuthenticatedStudent_Success() throws Exception {
                QuizRequestDto requestDto = new QuizRequestDto();
                requestDto.setTopic("Java Concurrency");

                QuizResponseDto mockAiResponse = new QuizResponseDto();
                mockAiResponse.setTopic("Java Concurrency");

                when(aiServiceClient.generateQuiz(any(QuizRequestDto.class)))
                                .thenReturn(Mono.just(mockAiResponse));

                MvcResult mvcResult = mockMvc.perform(post("/api/v1/quiz/generate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                                .andExpect(request().asyncStarted())
                                .andReturn();

                mockMvc.perform(asyncDispatch(mvcResult))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Java Concurrency Quiz"));

                assertThat(assessmentRepository.count()).isEqualTo(1);
        }

        @Test
        @WithMockUser(username = "instructor@lms.com", roles = { "INSTRUCTOR" })
        @DisplayName("POST /assessment/evaluate: Should process evaluation and save result to DB")
        void evaluateAssessment_Success() throws Exception {
                EvaluationRequestDto requestDto = new EvaluationRequestDto();
                requestDto.setQuestion("What is Dependency Injection?");
                requestDto.setStudentAnswer("It is a design pattern used to implement IoC.");
                requestDto.setMarkingScheme("Design pattern implementation for IoC.");

                EvaluationResponseDto mockEvaluationResponse = new EvaluationResponseDto();
                mockEvaluationResponse.setScoreOutOf10(9);
                mockEvaluationResponse.setFeedbackForStudent("Great explanation!");
                mockEvaluationResponse.setStrengths(java.util.List.of("Clear concept"));
                mockEvaluationResponse.setMissingPoints(java.util.List.of("None"));

                when(aiServiceClient.evaluateAssessment(any(EvaluationRequestDto.class)))
                                .thenReturn(Mono.just(mockEvaluationResponse));

                MvcResult mvcResult = mockMvc.perform(post("/api/v1/assessment/evaluate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                                .andExpect(request().asyncStarted())
                                .andReturn();

                String responseString = mockMvc.perform(asyncDispatch(mvcResult))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();

                // 💡 Verify HTTP 200 OK, DB Persistence & Response Content
                assertThat(responseString).contains("Great explanation!");
                assertThat(assessmentResultRepository.count()).isEqualTo(1);
        }

        @Test
        @WithMockUser(username = "admin@lms.com", roles = { "ADMIN" })
        @DisplayName("GET /assessment/results: Should return all saved results from DB")
        void getAllResults_Success() throws Exception {
                AssessmentResult dummyResult = AssessmentResult.builder()
                                .question("What is Spring Boot?")
                                .studentAnswer("Framework")
                                .scoreOutOf10(8)
                                .build();
                assessmentResultRepository.save(dummyResult);

                mockMvc.perform(get("/api/v1/assessment/results")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1))
                                .andExpect(jsonPath("$[0].question").value("What is Spring Boot?"));
        }
}