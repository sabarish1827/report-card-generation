package com.evaluate.report_card_system.controller;

import com.evaluate.report_card_system.model.Student;
import com.evaluate.report_card_system.repository.StudentRepository;
import com.evaluate.report_card_system.request.UpdateMarkRequest;
import com.evaluate.report_card_system.service.ReportCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportcard")
public class ReportCardController {

    @Autowired
    private ReportCardService reportCardService;

    @Autowired
    private StudentRepository studentRepository;

    @PostMapping("/generate")
    public ResponseEntity<Double> generateReportCard(@RequestBody Student student) {
        studentRepository.save(student);
        double finalScore = reportCardService.calculateFinalScore(student);
        studentRepository.save(student);
        return ResponseEntity.ok(finalScore);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentReport(@PathVariable String id) {
        return studentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/roll/{rollNumber}")
    public ResponseEntity<Student> getStudentByRollNo(@PathVariable Integer rollNumber) {
        return studentRepository.findByRollNumber(rollNumber)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/roll/{rollNumber}/marks")
    public ResponseEntity<Student> updateExamMarks(@PathVariable Integer rollNumber, @RequestBody UpdateMarkRequest request) {
        return studentRepository.findByRollNumber(rollNumber)
                .map(student -> {
                    reportCardService.updateExamMarks(student, request);
                    studentRepository.save(student);
                    return ResponseEntity.ok(student);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable String id) {
        if (studentRepository.existsById(id)) {
            studentRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}