package com.lms.backend.controller;

import com.lms.backend.dto.QuizRequestDto;
import com.lms.backend.dto.QuizResponseDto;
import com.lms.backend.service.AiServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/rag")
@RequiredArgsConstructor
public class RagController {

    private final AiServiceClient aiServiceClient;

    // 1. Ingest PDF Document
    @PostMapping(value = "/ingest-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<String>> ingestPdf(@RequestPart("file") MultipartFile file) throws IOException {
        ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        return aiServiceClient.ingestPdf(resource)
                .map(ResponseEntity::ok);
    }

    // 2. Generate Quiz from Ingested PDF
    @PostMapping("/generate-quiz")
    public Mono<ResponseEntity<QuizResponseDto>> generateRagQuiz(@RequestBody QuizRequestDto requestDto) {
        return aiServiceClient.generateRagQuiz(requestDto)
                .map(ResponseEntity::ok);
    }
}