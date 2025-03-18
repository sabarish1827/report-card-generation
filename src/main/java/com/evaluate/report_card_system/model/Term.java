package com.evaluate.report_card_system.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class Term {
    @NotEmpty(message = "Term name is required")
    private String termName;

    @NotEmpty(message = "At least one exam is required")
    private List<Exam> exams;
    private double termScore;
}