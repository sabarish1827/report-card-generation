package com.evaluate.report_card_system.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class UpdateMarkRequest {
    @NotBlank(message = "Term name is required")
    private String termName;
    @NotBlank(message = "Exam name is required")
    private String examName;
    @NotNull(message = "Subject marks are required")
    private Map<String, Double> subjectMarks;
}