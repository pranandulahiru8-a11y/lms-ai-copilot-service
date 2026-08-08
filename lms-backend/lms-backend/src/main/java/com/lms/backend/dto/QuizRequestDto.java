package com.lms.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizRequestDto {

    private String topic;

    @JsonProperty("num_questions") // 👈 FastAPI එක බලන snake_case
    private Integer numQuestions = 3;

    private String difficulty = "Medium";
}