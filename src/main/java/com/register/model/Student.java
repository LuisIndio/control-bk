package com.register.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String studentName;
    private String lastName;
    private LocalDate birthDate;
    private LocalDate registrationDate;
    private LocalDate registrationEndDate;
    private String imagePath = "default.jpg";

}