package com.evaluate.report_card_system.service;

import com.evaluate.report_card_system.request.UpdateMarkRequest;
import com.evaluate.report_card_system.model.Exam;
import com.evaluate.report_card_system.model.Student;
import com.evaluate.report_card_system.model.Term;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReportCardServiceTest {

    private ReportCardService reportCardService;
    private Student student;

    @BeforeEach
    void setUp() {
        reportCardService = new ReportCardService();
        student = new Student();
        student.setRollNumber(101);
        student.setName("John Doe");

        Exam exam1 = new Exam();
        exam1.setExamName("Exam 1");
        exam1.setSubjectMarks(new HashMap<>() {{
            put("Physics", 78.0);
            put("Chemistry", 72.0);
            put("Biology", 80.0);
        }});

        Exam exam2 = new Exam();
        exam2.setExamName("Exam 2");
        exam2.setSubjectMarks(new HashMap<>() {{
            put("Physics", 80.0);
            put("Chemistry", 75.0);
            put("Biology", 82.0);
        }});

        Exam exam3 = new Exam();
        exam3.setExamName("Exam 3");
        exam3.setSubjectMarks(new HashMap<>() {{
            put("Physics", 85.0);
            put("Chemistry", 78.0);
            put("Biology", 84.0);
        }});

        Term term1 = new Term();
        term1.setTermName("Term 1");
        term1.setExams(List.of(exam1, exam2, exam3));

        student.setTerms(List.of(term1));
    }

    @Test
    void calculateFinalScore_ShouldReturnCorrectScore_WhenValidData() {
        double finalScore = reportCardService.calculateFinalScore(student);
        assertEquals(81.67, finalScore, 0.01, "Final score should match expected calculation");
    }

    @Test
    void calculateFinalScore_ShouldThrowException_WhenNoTerms() {
        student.setTerms(Collections.emptyList());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                reportCardService.calculateFinalScore(student));
        assertEquals("Student must have at least one term", exception.getMessage());
    }

    @Test
    void calculateFinalScore_ShouldThrowException_WhenInvalidExamCount() {
        Term term = new Term();
        term.setTermName("Term 1");
        term.setExams(List.of(new Exam()));
        student.setTerms(List.of(term));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                reportCardService.calculateFinalScore(student));
        assertEquals("Each term must have exactly 3 exams", exception.getMessage());
    }

    @Test
    void updateExamMarks_ShouldUpdateMarksAndRecalculate_WhenValidRequest() {
        UpdateMarkRequest request = new UpdateMarkRequest();
        request.setTermName("Term 1");
        request.setExamName("Exam 1");
        request.setSubjectMarks(new HashMap<>() {{
            put("Physics", 90.0);
            put("Chemistry", 85.0);
            put("Biology", 88.0);
        }});

        reportCardService.updateExamMarks(student, request);
        Exam updatedExam = student.getTerms().get(0).getExams().stream()
                .filter(exam -> exam.getExamName().equals("Exam 1"))
                .findFirst().orElse(null);
        assertNotNull(updatedExam);
        assertEquals(90.0, updatedExam.getSubjectMarks().get("Physics"), 0.01);
        assertEquals(87.9, updatedExam.getWeightedScores().get("Science"), 0.01);
        assertEquals(82.78, student.getTerms().get(0).getTermScore(), 0.01, "Term score should be recalculated");
    }

    @Test
    void updateExamMarks_ShouldPreserveOtherSubjects_WhenPartialUpdate() {
        UpdateMarkRequest request = new UpdateMarkRequest();
        request.setTermName("Term 1");
        request.setExamName("Exam 1");
        request.setSubjectMarks(new HashMap<>() {{
            put("Physics", 90.0);
        }});

        reportCardService.updateExamMarks(student, request);
        Exam updatedExam = student.getTerms().get(0).getExams().stream()
                .filter(exam -> exam.getExamName().equals("Exam 1"))
                .findFirst().orElse(null);
        assertNotNull(updatedExam);
        assertEquals(90.0, updatedExam.getSubjectMarks().get("Physics"), 0.01);
        assertEquals(72.0, updatedExam.getSubjectMarks().get("Chemistry"), 0.01, "Chemistry should remain unchanged");
        assertEquals(80.0, updatedExam.getSubjectMarks().get("Biology"), 0.01, "Biology should remain unchanged");
        assertEquals(81.6, updatedExam.getWeightedScores().get("Science"), 0.01, "Science score should reflect partial update");
        assertEquals(82.15, student.getTerms().get(0).getTermScore(), 0.01, "Term score should be recalculated");
    }

    @Test
    void updateExamMarks_ShouldThrowException_WhenTermNotFound() {
        UpdateMarkRequest request = new UpdateMarkRequest();
        request.setTermName("Term 2");
        request.setExamName("Exam 1");
        request.setSubjectMarks(new HashMap<>() {{
            put("Physics", 90.0);
        }});

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                reportCardService.updateExamMarks(student, request));
        assertEquals("Term Term 2 not found for student 101", exception.getMessage());
    }

    @Test
    void updateExamMarks_ShouldThrowException_WhenExamNotFound() {
        UpdateMarkRequest request = new UpdateMarkRequest();
        request.setTermName("Term 1");
        request.setExamName("Exam 4");
        request.setSubjectMarks(new HashMap<>() {{
            put("Physics", 90.0);
        }});

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                reportCardService.updateExamMarks(student, request));
        assertEquals("Exam Exam 4 not found in term Term 1", exception.getMessage());
    }
}