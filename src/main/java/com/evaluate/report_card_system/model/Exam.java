package com.evaluate.report_card_system.model;

import lombok.Data;

import java.util.Map;

@Data
public class Exam {
    private String examName;
    private Map<String, Double> subjectMarks;
    private Map<String, Double> weightedScores;
}
