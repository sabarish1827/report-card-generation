package com.evaluate.report_card_system.controller;

import com.evaluate.report_card_system.request.UpdateMarkRequest;
import com.evaluate.report_card_system.model.Student;
import com.evaluate.report_card_system.model.Term;
import com.evaluate.report_card_system.service.ReportCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/reportcard")
public class ReportCardController {

    private static final Logger logger = LoggerFactory.getLogger(ReportCardController.class);
    private final ReportCardService reportCardService;

    public ReportCardController(ReportCardService reportCardService) {
        this.reportCardService = reportCardService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateReportCard(@RequestBody Student student) {
        try {
            Student savedStudent = reportCardService.generateReportCard(student);
            return ResponseEntity.ok(savedStudent.getTerms().stream()
                    .mapToDouble(Term::getTermScore)
                    .average()
                    .orElse(0.0));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/roll/{rollNumber}")
    public ResponseEntity<Student> getStudentByRollNo(@PathVariable int rollNumber) {
        try {
            Optional<Student> student = reportCardService.getStudentByRollNumber(rollNumber);
            return student.isPresent() ? ResponseEntity.ok(student.get()) : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.error("Invalid roll number: {}", rollNumber, e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/roll/{rollNumber}/marks")
    public ResponseEntity<?> updateExamMarks(@PathVariable int rollNumber, @RequestBody UpdateMarkRequest request) {
        try {
            Student updatedStudent = reportCardService.updateExamMarks(rollNumber, request);
            return ResponseEntity.ok(updatedStudent);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStudent(@PathVariable String id) {
        try {
            reportCardService.deleteStudent(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}