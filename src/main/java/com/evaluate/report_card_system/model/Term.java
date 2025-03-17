package com.evaluate.report_card_system.model;

import lombok.Data;

import java.util.List;

@Data
public class Term {
    private String termName;
    private List<Exam> exams;
    private double termScore;
}