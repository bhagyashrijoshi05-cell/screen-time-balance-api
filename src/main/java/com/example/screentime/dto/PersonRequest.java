package com.example.screentime.dto;

import com.example.screentime.model.AgeGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PersonRequest(@NotBlank String name, @NotNull AgeGroup ageGroup) {
}
