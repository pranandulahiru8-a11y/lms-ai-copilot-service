package com.lms.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationRequestDto {
    private String question;
    @JsonProperty("reference_answer")
    private String markingScheme;
    @JsonProperty("student_answer")
    private String studentAnswer;

}