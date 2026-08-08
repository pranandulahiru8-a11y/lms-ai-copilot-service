package com.lms.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponseDto {

    private String topic;

    private String difficulty;

    @JsonProperty("total_questions")
    private int totalQuestions;

    private List<QuestionMcqDto> questions;
}