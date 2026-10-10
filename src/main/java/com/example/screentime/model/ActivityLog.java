package com.example.screentime.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long personId;

    @Enumerated(EnumType.STRING)
    private ActivityType type;

    private int minutes;

    private LocalDate date;

    protected ActivityLog() {
    }

    public ActivityLog(Long personId, ActivityType type, int minutes, LocalDate date) {
        this.personId = personId;
        this.type = type;
        this.minutes = minutes;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public Long getPersonId() {
        return personId;
    }

    public ActivityType getType() {
        return type;
    }

    public int getMinutes() {
        return minutes;
    }

    public LocalDate getDate() {
        return date;
    }
}
