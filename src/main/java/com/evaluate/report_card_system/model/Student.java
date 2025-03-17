package com.evaluate.report_card_system.model;

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
    @Indexed(unique = true) // Ensures rollNumber is unique in the collection
    private Integer rollNumber;
    private String name;
    private List<Term> terms;
}