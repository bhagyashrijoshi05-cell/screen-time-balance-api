package com.example.screentime.dto;

import com.example.screentime.model.ActivityType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ActivityRequest(@NotNull ActivityType type, int minutes, @NotNull LocalDate date) {
}
