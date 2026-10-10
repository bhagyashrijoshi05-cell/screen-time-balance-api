package com.example.screentime.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private AgeGroup ageGroup;

    protected Person() {
    }

    public Person(String name, AgeGroup ageGroup) {
        this.name = name;
        this.ageGroup = ageGroup;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public AgeGroup getAgeGroup() {
        return ageGroup;
    }
}
