package com.example.screentime.dto;

import com.example.screentime.model.ActivityType;

import java.time.LocalDate;

public record ActivityResponse(Long id, ActivityType type, int minutes, LocalDate date, String warning) {
}
