package com.evaluate.report_card_system.service;

import com.evaluate.report_card_system.config.WeightConfig;
import com.evaluate.report_card_system.request.UpdateMarkRequest;
import com.evaluate.report_card_system.model.Exam;
import com.evaluate.report_card_system.model.Student;
import com.evaluate.report_card_system.model.Term;
import com.evaluate.report_card_system.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReportCardService {

    private static final Logger logger = LoggerFactory.getLogger(ReportCardService.class);
    private final StudentRepository studentRepository;

    public ReportCardService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Optional<Student> getStudentByRollNumber(int rollNumber) {
        if (rollNumber <= 0) {
            logger.warn("Invalid roll number provided: {}", rollNumber);
            throw new IllegalArgumentException("Roll number must be a positive integer");
        }
        Optional<Student> student = studentRepository.findByRollNumber(rollNumber);
        if (student.isPresent()) {
            logger.info("Student found for rollNumber={}", rollNumber);
        } else {
            logger.info("No student found for rollNumber={}", rollNumber);
        }
        return student;
    }

    private void validateSubjectMarks(Map<String, Double> subjectMarks, String context) {
        if (subjectMarks == null || subjectMarks.isEmpty()) {
            throw new IllegalArgumentException(context + ": Subject marks are required");
        }
        subjectMarks.forEach((subject, mark) -> {
            if (mark == null) {
                throw new IllegalArgumentException(context + ": Mark for " + subject + " cannot be null");
            }
            if (mark < 0 || mark > 100) {
                throw new IllegalArgumentException(context + ": Marks for " + subject + " must be between 0 and 100");
            }
        });
    }

    private double calculateScienceScore(Exam exam) {
        try {
            validateSubjectMarks(exam.getSubjectMarks(), "Exam " + exam.getExamName());
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
        } catch (IllegalArgumentException e) {
            logger.error("Validation error for exam {}: {}", exam.getExamName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error calculating science score for exam {}: {}", exam.getExamName(), e.getMessage(), e);
            throw new RuntimeException("Failed to calculate science score", e);
        }
    }

    private double calculateTermScore(Term term) {
        try {
            if (term.getTermName() == null || term.getTermName().isEmpty()) {
                throw new IllegalArgumentException("Term name is required");
            }
            List<Exam> exams = term.getExams();
            if (exams == null || exams.isEmpty()) {
                throw new IllegalArgumentException("Term " + term.getTermName() + ": At least one exam is required");
            }
            if (exams.size() != 3) {
                throw new IllegalArgumentException("Term " + term.getTermName() + ": Each term must have exactly 3 exams");
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
            if (student.getRollNumber() == null || student.getRollNumber() <= 0) {
                throw new IllegalArgumentException("Roll number must be a positive integer");
            }
            if (student.getName() == null || student.getName().isEmpty()) {
                throw new IllegalArgumentException("Name is required");
            }
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

    public Student generateReportCard(Student student) {
        if (student.getRollNumber() == null || student.getRollNumber() <= 0) {
            throw new IllegalArgumentException("Roll number must be a positive integer");
        }
        if (studentRepository.findByRollNumber(student.getRollNumber()).isPresent()) {
            logger.warn("Roll number {} already exists", student.getRollNumber());
            throw new IllegalArgumentException("Roll number already exists");
        }
        calculateFinalScore(student);
        studentRepository.save(student);
        return studentRepository.save(student);
    }

    public Student updateExamMarks(int rollNumber, UpdateMarkRequest request) {
        try {
            if (rollNumber <= 0) {
                throw new IllegalArgumentException("Roll number must be a positive integer");
            }
            if (request.getTermName() == null || request.getTermName().isEmpty()) {
                throw new IllegalArgumentException("Term name is required");
            }
            if (request.getExamName() == null || request.getExamName().isEmpty()) {
                throw new IllegalArgumentException("Exam name is required");
            }
            validateSubjectMarks(request.getSubjectMarks(), "Update request");

            Optional<Student> studentOpt = studentRepository.findByRollNumber(rollNumber);
            if (studentOpt.isEmpty()) {
                logger.warn("Student not found for rollNumber={}", rollNumber);
                throw new IllegalArgumentException("Student not found for rollNumber " + rollNumber);
            }
            Student student = studentOpt.get();

            boolean termFound = false;
            for (Term term : student.getTerms()) {
                if (term.getTermName().equals(request.getTermName())) {
                    termFound = true;
                    boolean examFound = false;
                    for (Exam exam : term.getExams()) {
                        if (exam.getExamName().equals(request.getExamName())) {
                            examFound = true;
                            logger.info("Updating marks for rollNumber={}, term={}, exam={}",
                                    rollNumber, request.getTermName(), request.getExamName());
                            var currentMarks = exam.getSubjectMarks();
                            if (currentMarks == null) {
                                currentMarks = new HashMap<>();
                                exam.setSubjectMarks(currentMarks);
                            }
                            currentMarks.putAll(request.getSubjectMarks());
                            calculateScienceScore(exam);
                            term.setTermScore(calculateTermScore(term));
                            break;
                        }
                    }
                    if (!examFound) {
                        throw new IllegalArgumentException("Exam " + request.getExamName() + " not found in term " + request.getTermName());
                    }
                    break;
                }
            }
            if (!termFound) {
                throw new IllegalArgumentException("Term " + request.getTermName() + " not found for student " + rollNumber);
            }
            return studentRepository.save(student);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error updating marks for rollNumber {}: {}", rollNumber, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating marks for rollNumber {}: {}", rollNumber, e.getMessage(), e);
            throw new RuntimeException("Failed to update exam marks", e);
        }
    }

    public void deleteStudent(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID is required");
        }
        if (!studentRepository.existsById(id)) {
            logger.warn("Student not found with id: {}", id);
            throw new IllegalArgumentException("Student not found with id " + id);
        }
        studentRepository.deleteById(id);
        logger.info("Student deleted successfully with id: {}", id);
    }
}