package com.evaluate.report_card_system.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "students")
public class Student {
    @Id
    private String id;

    @Indexed(unique = true)
    @NotNull(message = "Roll number is required")
    private Integer rollNumber;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Terms are required")
    private List<Term> terms;
}