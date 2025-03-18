package com.evaluate.report_card_system.controller;

import com.evaluate.report_card_system.request.UpdateMarkRequest;
import com.evaluate.report_card_system.model.Student;
import com.evaluate.report_card_system.repository.StudentRepository;
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
    private final StudentRepository studentRepository;

    public ReportCardController(ReportCardService reportCardService, StudentRepository studentRepository) {
        this.reportCardService = reportCardService;
        this.studentRepository = studentRepository;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateReportCard(@RequestBody Student student) {
        logger.info("Generating report card for student: rollNumber={}", student.getRollNumber());
        try {
            studentRepository.save(student);
            double finalScore = reportCardService.calculateFinalScore(student);
            studentRepository.save(student);
            logger.info("Report card generated successfully for rollNumber={}", student.getRollNumber());
            return ResponseEntity.ok(finalScore);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error for rollNumber={}: {}", student.getRollNumber(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/roll/{rollNumber}")
    public ResponseEntity<Student> getStudentByRollNo(@PathVariable int rollNumber) {
        logger.info("Fetching student by rollNumber: {}", rollNumber);
        Optional<Student> student = studentRepository.findByRollNumber(rollNumber);
        if (student.isPresent()) {
            logger.info("Student found by rollNumber: {}", rollNumber);
            return ResponseEntity.ok(student.get());
        } else {
            logger.warn("Student not found by rollNumber: {}", rollNumber);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/roll/{rollNumber}/marks")
    public ResponseEntity<?> updateExamMarks(@PathVariable int rollNumber, @RequestBody UpdateMarkRequest request) {
        logger.info("Updating marks for rollNumber={}, term={}, exam={}", rollNumber, request.getTermName(), request.getExamName());
        try {
            Optional<Student> studentOpt = studentRepository.findByRollNumber(rollNumber);
            if (studentOpt.isEmpty()) {
                logger.warn("Student not found for rollNumber={}", rollNumber);
                return ResponseEntity.notFound().build();
            }
            Student student = studentOpt.get();
            reportCardService.updateExamMarks(student, request);
            studentRepository.save(student);
            logger.info("Marks updated successfully for rollNumber={}", rollNumber);
            return ResponseEntity.ok(student);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error updating marks for rollNumber={}: {}", rollNumber, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable String id) {
        logger.info("Deleting student with id: {}", id);
        if (studentRepository.existsById(id)) {
            studentRepository.deleteById(id);
            logger.info("Student deleted successfully with id: {}", id);
            return ResponseEntity.noContent().build();
        } else {
            logger.warn("Student not found with id: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
}