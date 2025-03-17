package com.evaluate.report_card_system.request;

import lombok.Data;

import java.util.Map;

@Data
public class UpdateMarkRequest {
    private String termName;
    private String examName;
    private Map<String, Double> subjectMarks;
}
