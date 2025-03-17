package com.evaluate.report_card_system.repository;

import com.evaluate.report_card_system.model.Student;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface StudentRepository extends MongoRepository<Student, String> {
    Optional<Student> findByRollNumber(Integer rollNumber);
}