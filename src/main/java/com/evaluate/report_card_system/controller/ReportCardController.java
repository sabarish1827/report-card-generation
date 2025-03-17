package com.evaluate.report_card_system.controller;

import com.evaluate.report_card_system.model.Student;
import com.evaluate.report_card_system.repository.StudentRepository;
import com.evaluate.report_card_system.request.UpdateMarkRequest;
import com.evaluate.report_card_system.service.ReportCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportcard")
public class ReportCardController {

    private static final Logger logger = LoggerFactory.getLogger(ReportCardController.class);

    @Autowired
    private ReportCardService reportCardService;

    @Autowired
    private StudentRepository studentRepository;

    @PostMapping("/generate")
    public ResponseEntity<?> generateReportCard(@Valid @RequestBody Student student) {
        try {
            logger.info("Generating report card for student: rollNumber={}", student.getRollNumber());
            studentRepository.save(student);
            double finalScore = reportCardService.calculateFinalScore(student);
            studentRepository.save(student);
            logger.info("Report card generated successfully for rollNumber={}", student.getRollNumber());
            return ResponseEntity.ok(finalScore);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error for rollNumber={}: {}", student.getRollNumber(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error generating report card for rollNumber={}: {}", student.getRollNumber(), e.getMessage(), e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentReport(@PathVariable String id) {
        try {
            logger.info("Fetching student by ID: {}", id);
            return studentRepository.findById(id)
                    .map(student -> {
                        logger.info("Student found by ID: {}", id);
                        return ResponseEntity.ok(student);
                    })
                    .orElseGet(() -> {
                        logger.warn("Student not found by ID: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            logger.error("Error fetching student by ID={}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @GetMapping("/roll/{rollNumber}")
    public ResponseEntity<?> getStudentByRollNo(@PathVariable Integer rollNumber) {
        try {
            logger.info("Fetching student by rollNumber: {}", rollNumber);
            return studentRepository.findByRollNumber(rollNumber)
                    .map(student -> {
                        logger.info("Student found by rollNumber: {}", rollNumber);
                        return ResponseEntity.ok(student);
                    })
                    .orElseGet(() -> {
                        logger.warn("Student not found by rollNumber: {}", rollNumber);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            logger.error("Error fetching student by rollNumber={}: {}", rollNumber, e.getMessage(), e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PutMapping("/roll/{rollNumber}/marks")
    public ResponseEntity<?> updateExamMarks(@PathVariable Integer rollNumber, @Valid @RequestBody UpdateMarkRequest request) {
        try {
            logger.info("Updating marks for rollNumber={}, term={}, exam={}", rollNumber, request.getTermName(), request.getExamName());
            return studentRepository.findByRollNumber(rollNumber)
                    .map(student -> {
                        reportCardService.updateExamMarks(student, request);
                        studentRepository.save(student);
                        logger.info("Marks updated successfully for rollNumber={}", rollNumber);
                        return ResponseEntity.ok(student);
                    })
                    .orElseGet(() -> {
                        logger.warn("Student not found for rollNumber: {}", rollNumber);
                        return ResponseEntity.notFound().build();
                    });
        } catch (IllegalArgumentException e) {
            logger.error("Validation error updating marks for rollNumber={}: {}", rollNumber, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error updating marks for rollNumber={}: {}", rollNumber, e.getMessage(), e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable String id) {
        try {
            logger.info("Deleting student by ID: {}", id);
            if (studentRepository.existsById(id)) {
                studentRepository.deleteById(id);
                logger.info("Student deleted successfully by ID: {}", id);
                return ResponseEntity.noContent().build();
            }
            logger.warn("Student not found for deletion by ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting student by ID={}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}