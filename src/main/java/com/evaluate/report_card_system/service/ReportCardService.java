package com.evaluate.report_card_system.service;

import com.evaluate.report_card_system.config.WeightConfig;
import com.evaluate.report_card_system.request.UpdateMarkRequest;
import com.evaluate.report_card_system.model.Exam;
import com.evaluate.report_card_system.model.Student;
import com.evaluate.report_card_system.model.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class ReportCardService {

    private static final Logger logger = LoggerFactory.getLogger(ReportCardService.class);

    private double calculateScienceScore(Exam exam) {
        try {
            var marks = exam.getSubjectMarks();
            double physics = marks.getOrDefault("Physics", 0.0);
            double chemistry = marks.getOrDefault("Chemistry", 0.0);
            double biology = marks.getOrDefault("Biology", 0.0);

            double scienceScore = (physics * WeightConfig.PHYSICS_WEIGHT) +
                    (chemistry * WeightConfig.CHEMISTRY_WEIGHT) +
                    (biology * WeightConfig.BIOLOGY_WEIGHT);

            var weightedScores = exam.getWeightedScores() != null ? exam.getWeightedScores() : new HashMap<String, Double>();
            weightedScores.put("Science", scienceScore);
            exam.setWeightedScores(weightedScores);

            logger.debug("Calculated science score for exam {}: {}", exam.getExamName(), scienceScore);
            return scienceScore;
        } catch (Exception e) {
            logger.error("Error calculating science score for exam {}: {}", exam.getExamName(), e.getMessage(), e);
            throw new RuntimeException("Failed to calculate science score", e);
        }
    }

    private double calculateTermScore(Term term) {
        try {
            List<Exam> exams = term.getExams();
            if (exams.size() != 3) {
                throw new IllegalArgumentException("Each term must have exactly 3 exams");
            }

            double exam1Science = calculateScienceScore(exams.get(0));
            double exam2Science = calculateScienceScore(exams.get(1));
            double exam3Science = calculateScienceScore(exams.get(2));

            double termScore = (exam1Science * WeightConfig.EXAM1_WEIGHT) +
                    (exam2Science * WeightConfig.EXAM2_WEIGHT) +
                    (exam3Science * WeightConfig.EXAM3_WEIGHT);

            logger.debug("Calculated term score for {}: {}", term.getTermName(), termScore);
            return termScore;
        } catch (IllegalArgumentException e) {
            logger.error("Validation error for term {}: {}", term.getTermName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error calculating term score for {}: {}", term.getTermName(), e.getMessage(), e);
            throw new RuntimeException("Failed to calculate term score", e);
        }
    }

    public double calculateFinalScore(Student student) {
        try {
            List<Term> terms = student.getTerms();
            if (terms == null || terms.isEmpty()) {
                throw new IllegalArgumentException("Student must have at least one term");
            }

            double totalTermScore = 0.0;
            for (Term term : terms) {
                double termScore = calculateTermScore(term);
                term.setTermScore(termScore);
                totalTermScore += termScore;
            }
            double finalScore = totalTermScore / terms.size();
            logger.info("Final score calculated for rollNumber {}: {}", student.getRollNumber(), finalScore);
            return finalScore;
        } catch (IllegalArgumentException e) {
            logger.error("Validation error for rollNumber {}: {}", student.getRollNumber(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error calculating final score for rollNumber {}: {}", student.getRollNumber(), e.getMessage(), e);
            throw new RuntimeException("Failed to calculate final score", e);
        }
    }

    public void updateExamMarks(Student student, UpdateMarkRequest request) {
        try {
            for (Term term : student.getTerms()) {
                if (term.getTermName().equals(request.getTermName())) {
                    for (Exam exam : term.getExams()) {
                        if (exam.getExamName().equals(request.getExamName())) {
                            logger.info("Updating marks for rollNumber={}, term={}, exam={}",
                                    student.getRollNumber(), request.getTermName(), request.getExamName());

                            var currentMarks = exam.getSubjectMarks();
                            if (currentMarks == null) {
                                currentMarks = new HashMap<>();
                                exam.setSubjectMarks(currentMarks);
                            }
                            currentMarks.putAll(request.getSubjectMarks());

                            calculateScienceScore(exam);
                            term.setTermScore(calculateTermScore(term));
                            return;
                        }
                    }
                    throw new IllegalArgumentException("Exam " + request.getExamName() + " not found in term " + request.getTermName());
                }
            }
            throw new IllegalArgumentException("Term " + request.getTermName() + " not found for student " + student.getRollNumber());
        } catch (IllegalArgumentException e) {
            logger.error("Validation error updating marks for rollNumber {}: {}", student.getRollNumber(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating marks for rollNumber {}: {}", student.getRollNumber(), e.getMessage(), e);
            throw new RuntimeException("Failed to update exam marks", e);
        }
    }
}