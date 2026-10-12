package com.example.screentime.dto;

import com.example.screentime.model.ActivityType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ActivityRequest(
        @NotNull ActivityType type,
        @Min(value = 1, message = "minutes must be at least 1")
        @Max(value = 1440, message = "minutes cannot be more than 1440 (one day)")
        int minutes,
        @NotNull LocalDate date) {
}