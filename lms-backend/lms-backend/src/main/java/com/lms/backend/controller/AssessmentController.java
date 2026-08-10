package com.lms.backend.controller;

import com.lms.backend.dto.EvaluationRequestDto;
import com.lms.backend.dto.EvaluationResponseDto;
import com.lms.backend.dto.QuizRequestDto;
import com.lms.backend.dto.QuizResponseDto;
import com.lms.backend.entity.Assessment;
import com.lms.backend.entity.AssessmentResult;
import com.lms.backend.repository.AssessmentRepository;
import com.lms.backend.repository.AssessmentResultRepository;
import com.lms.backend.service.AiServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AssessmentController {

    private final AiServiceClient aiServiceClient;
    private final AssessmentRepository assessmentRepository;
    private final AssessmentResultRepository assessmentResultRepository;
    private final ObjectMapper objectMapper;

    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR', 'ADMIN')")
    @PostMapping("/quiz/generate")
    public Mono<ResponseEntity<Assessment>> generateAssessment(@RequestBody QuizRequestDto requestDto) {
        log.info("🚀 1. Quiz request received for topic: {}", requestDto.getTopic());

        System.out.println("==============================================");
        System.out.println(">>> REQUEST RECEIVED IN CONTROLLER: " + requestDto.getTopic());
        System.out.println("==============================================");

        return aiServiceClient.generateQuiz(requestDto)
                .doOnNext(aiResponse -> log.info("✅ 2. AI Response received from FastAPI: {}", aiResponse.getTopic()))
                .doOnError(error -> log.error("❌ Error from AI Microservice: ", error))
                .publishOn(Schedulers.boundedElastic())
                .map(this::buildAndSaveAssessment)
                .doOnNext(response -> log.info("🎉 4. Successfully returned response to client!"))
                .doOnError(error -> log.error("❌ DB Save Failure: ", error));

    }

    @SneakyThrows
    @Transactional
    private ResponseEntity<Assessment> buildAndSaveAssessment(QuizResponseDto aiResponse) {
        log.info("⚙️ 3. Converting Quiz Object to JSON and saving to DB...");

        String jsonString = objectMapper.writeValueAsString(aiResponse);

        Assessment assessment = Assessment.builder()
                .title(aiResponse.getTopic() + " Quiz")
                .quizDataJson(jsonString)
                .build();

        Assessment savedAssessment = assessmentRepository.saveAndFlush(assessment);

        log.info("💾 Saved Assessment ID: {}", savedAssessment.getId());

        return ResponseEntity.ok(savedAssessment);
    }

    @PostMapping("/assessment/evaluate")
    public Mono<ResponseEntity<EvaluationResponseDto>> evaluateAssessment(
            @RequestBody EvaluationRequestDto requestDto) {
        return aiServiceClient.evaluateAssessment(requestDto)
                .publishOn(Schedulers.boundedElastic())
                .map(responseDto -> saveAndReturnEvaluation(requestDto, responseDto));
    }

    // 2. Fetch All Saved Evaluation Results
    @GetMapping("/assessment/results")
    public ResponseEntity<List<AssessmentResult>> getAllResults() {
        return ResponseEntity.ok(assessmentResultRepository.findAll());
    }

    @SneakyThrows
    private ResponseEntity<EvaluationResponseDto> saveAndReturnEvaluation(EvaluationRequestDto requestDto,
            EvaluationResponseDto responseDto) {
        AssessmentResult result = AssessmentResult.builder()
                .question(requestDto.getQuestion())
                .referenceAnswer(requestDto.getMarkingScheme())
                .studentAnswer(requestDto.getStudentAnswer())
                .scoreOutOf10(responseDto.getScoreOutOf10())
                .strengthsJson(objectMapper.writeValueAsString(responseDto.getStrengths()))
                .missingPointsJson(objectMapper.writeValueAsString(responseDto.getMissingPoints()))
                .feedbackForStudent(responseDto.getFeedbackForStudent())
                .build();

        assessmentResultRepository.save(result);
        return ResponseEntity.ok(responseDto);
    }

}