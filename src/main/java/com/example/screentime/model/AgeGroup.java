package com.example.screentime.model;

public enum AgeGroup {
    CHILD(120),
    ADULT(240),
    SENIOR(180);

    private final int dailyScreenLimitMinutes;

    AgeGroup(int dailyScreenLimitMinutes) {
        this.dailyScreenLimitMinutes = dailyScreenLimitMinutes;
    }

    public int getDailyScreenLimitMinutes() {
        return dailyScreenLimitMinutes;
    }
}
