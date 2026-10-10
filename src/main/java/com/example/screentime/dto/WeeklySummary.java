package com.example.screentime.dto;

import com.example.screentime.model.ActivityType;

import java.time.LocalDate;
import java.util.Map;

public record WeeklySummary(Long personId,
                            LocalDate weekStart,
                            LocalDate weekEnd,
                            Map<ActivityType, Integer> minutesByType,
                            int weeklyScreenLimit,
                            boolean screenLimitCrossed) {
}