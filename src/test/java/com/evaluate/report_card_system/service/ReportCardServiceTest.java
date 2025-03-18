package com.evaluate.report_card_system.service;

import com.evaluate.report_card_system.model.Exam;
import com.evaluate.report_card_system.model.Student;
import com.evaluate.report_card_system.model.Term;
import com.evaluate.report_card_system.repository.StudentRepository;
import com.evaluate.report_card_system.request.UpdateMarkRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportCardServiceTest {

    private ReportCardService reportCardService;

    @Mock
    private StudentRepository studentRepository;

    private Student sampleStudent;

    @BeforeEach
    void setUp() {
        reportCardService = new ReportCardService(studentRepository);
        sampleStudent = new Student();
        sampleStudent.setRollNumber(101);
        sampleStudent.setName("John Doe");

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

        sampleStudent.setTerms(List.of(term1));
    }

    @Test
    void generateReportCard_ShouldReturnStudent_WhenValid() {
        when(studentRepository.findByRollNumber(101)).thenReturn(Optional.empty());
        when(studentRepository.save(any(Student.class))).thenReturn(sampleStudent);

        Student result = reportCardService.generateReportCard(sampleStudent);
        assertEquals(81.67, result.getTerms().get(0).getTermScore(), 0.01);
        verify(studentRepository, times(2)).save(sampleStudent);
    }

    @Test
    void generateReportCard_ShouldThrowException_WhenRollNumberNegative() {
        sampleStudent.setRollNumber(-1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                reportCardService.generateReportCard(sampleStudent));
        assertEquals("Roll number must be a positive integer", exception.getMessage());
        verify(studentRepository, never()).findByRollNumber(anyInt());
        verify(studentRepository, never()).save(any());
    }

    @Test
    void generateReportCard_ShouldThrowException_WhenTermNameNull() {
        sampleStudent.getTerms().get(0).setTermName(null);
        when(studentRepository.findByRollNumber(101)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                reportCardService.generateReportCard(sampleStudent));
        assertEquals("Term name is required", exception.getMessage());
        verify(studentRepository, times(1)).findByRollNumber(101);
        verify(studentRepository, never()).save(any());
    }

    @Test
    void generateReportCard_ShouldThrowException_WhenRollNumberExists() {
        when(studentRepository.findByRollNumber(101)).thenReturn(Optional.of(sampleStudent));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                reportCardService.generateReportCard(sampleStudent));
        assertEquals("Roll number already exists", exception.getMessage());
        verify(studentRepository, never()).save(any());
    }

    @Test
    void generateReportCard_ShouldThrowException_WhenNegativeMarks() {
        sampleStudent.getTerms().get(0).getExams().get(0).setSubjectMarks(new HashMap<>() {{
            put("Physics", -5.0);
        }});

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                reportCardService.generateReportCard(sampleStudent));
        assertEquals("Exam Exam 1: Marks for Physics must be between 0 and 100", exception.getMessage());
        verify(studentRepository, times(1)).findByRollNumber(101);
        verify(studentRepository, never()).save(any());
    }

    @Test
    void updateExamMarks_ShouldUpdate_WhenValid() {
        UpdateMarkRequest request = new UpdateMarkRequest();
        request.setTermName("Term 1");
        request.setExamName("Exam 1");
        request.setSubjectMarks(new HashMap<>() {{
            put("Physics", 90.0);
        }});

        when(studentRepository.findByRollNumber(101)).thenReturn(Optional.of(sampleStudent));
        when(studentRepository.save(sampleStudent)).thenReturn(sampleStudent);

        Student result = reportCardService.updateExamMarks(101, request);
        assertEquals(90.0, result.getTerms().get(0).getExams().get(0).getSubjectMarks().get("Physics"), 0.01);
        assertEquals(82.15, result.getTerms().get(0).getTermScore(), 0.01);
    }

    @Test
    void updateExamMarks_ShouldThrowException_WhenRollNumberNegative() {
        UpdateMarkRequest request = new UpdateMarkRequest();
        request.setTermName("Term 1");
        request.setExamName("Exam 1");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                reportCardService.updateExamMarks(-1, request));
        assertEquals("Roll number must be a positive integer", exception.getMessage());
        verify(studentRepository, never()).findByRollNumber(anyInt());
    }

    @Test
    void updateExamMarks_ShouldThrowException_WhenMissingFields() {
        UpdateMarkRequest request = new UpdateMarkRequest();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                reportCardService.updateExamMarks(101, request));
        assertEquals("Term name is required", exception.getMessage());
        verify(studentRepository, never()).findByRollNumber(anyInt());
    }

    @Test
    void deleteStudent_ShouldDelete_WhenValid() {
        when(studentRepository.existsById("1")).thenReturn(true);
        doNothing().when(studentRepository).deleteById("1");

        reportCardService.deleteStudent("1");
        verify(studentRepository, times(1)).deleteById("1");
    }

    @Test
    void deleteStudent_ShouldThrowException_WhenIdNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                reportCardService.deleteStudent(null));
        assertEquals("ID is required", exception.getMessage());
        verify(studentRepository, never()).existsById(anyString());
    }

    @Test
    void findStudentByRollNumber_ShouldReturnStudent_WhenExists() {
        when(studentRepository.findByRollNumber(101)).thenReturn(Optional.of(sampleStudent));

        Optional<Student> result = reportCardService.getStudentByRollNumber(101);
        assertTrue(result.isPresent());
        assertEquals(101, result.get().getRollNumber());
        verify(studentRepository, times(1)).findByRollNumber(101);
    }

    @Test
    void findStudentByRollNumber_ShouldReturnEmpty_WhenNotExists() {
        when(studentRepository.findByRollNumber(999)).thenReturn(Optional.empty());

        Optional<Student> result = reportCardService.getStudentByRollNumber(999);
        assertFalse(result.isPresent());
        verify(studentRepository, times(1)).findByRollNumber(999);
    }

    @Test
    void findStudentByRollNumber_ShouldThrowException_WhenRollNumberNegative() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                reportCardService.getStudentByRollNumber(-1));
        assertEquals("Roll number must be a positive integer", exception.getMessage());
        verify(studentRepository, never()).findByRollNumber(anyInt());
    }
}