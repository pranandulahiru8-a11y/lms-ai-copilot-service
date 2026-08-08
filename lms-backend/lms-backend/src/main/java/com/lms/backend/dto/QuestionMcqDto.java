package com.lms.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionMcqDto {

    @JsonProperty("question_id")
    private int questionId;

    private String question;

    private List<String> options;

    @JsonProperty("correct_option_index")
    private int correctOptionIndex;

    private String explanation;
}