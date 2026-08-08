package com.lms.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResponseDto {
    @JsonProperty("score_out_of_10")
    private int scoreOutOf10;

    private List<String> strengths;

    @JsonProperty("missing_points")
    private List<String> missingPoints;

    @JsonProperty("feedback_for_student")
    private String feedbackForStudent;
}