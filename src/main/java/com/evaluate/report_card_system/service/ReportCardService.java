package com.evaluate.report_card_system.service;

import com.evaluate.report_card_system.config.WeightConfig;
import com.evaluate.report_card_system.request.UpdateMarkRequest;
import com.evaluate.report_card_system.model.Exam;
import com.evaluate.report_card_system.model.Student;
import com.evaluate.report_card_system.model.Term;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class ReportCardService {

    private double calculateScienceScore(Exam exam) {
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

        return scienceScore;
    }

    private double calculateTermScore(Term term) {
        List<Exam> exams = term.getExams();
        if (exams.size() != 3) {
            throw new IllegalArgumentException("Each term must have exactly 3 exams");
        }

        double exam1Science = calculateScienceScore(exams.get(0));
        double exam2Science = calculateScienceScore(exams.get(1));
        double exam3Science = calculateScienceScore(exams.get(2));

        return (exam1Science * WeightConfig.EXAM1_WEIGHT) +
                (exam2Science * WeightConfig.EXAM2_WEIGHT) +
                (exam3Science * WeightConfig.EXAM3_WEIGHT);
    }

    public double calculateFinalScore(Student student) {
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
        return totalTermScore / terms.size();
    }

    public void updateExamMarks(Student student, UpdateMarkRequest request) {
        for (Term term : student.getTerms()) {
            if (term.getTermName().equals(request.getTermName())) {
                for (Exam exam : term.getExams()) {
                    if (exam.getExamName().equals(request.getExamName())) {
                        exam.setSubjectMarks(request.getSubjectMarks());
                        calculateScienceScore(exam);
                        term.setTermScore(calculateTermScore(term));
                        return;
                    }
                }
                throw new IllegalArgumentException("Exam " + request.getExamName() + " not found in term " + request.getTermName());
            }
        }
        throw new IllegalArgumentException("Term " + request.getTermName() + " not found for student " + student.getRollNumber());
    }
}