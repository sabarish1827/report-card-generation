package com.evaluate.report_card_system.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class Exam {
    @NotEmpty(message = "Exam name is required")
    private String examName;

    @NotEmpty(message = "Subject marks are required")
    private Map<String, Double> subjectMarks;
    private Map<String, Double> weightedScores;
}
