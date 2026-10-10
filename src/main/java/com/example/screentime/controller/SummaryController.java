package com.example.screentime.controller;

import com.example.screentime.dto.WeeklySummary;
import com.example.screentime.model.ActivityLog;
import com.example.screentime.model.ActivityType;
import com.example.screentime.model.Person;
import com.example.screentime.repository.ActivityLogRepository;
import com.example.screentime.repository.PersonRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/persons/{personId}/summary")
public class SummaryController {

    private final PersonRepository personRepository;
    private final ActivityLogRepository activityLogRepository;

    public SummaryController(PersonRepository personRepository,
                             ActivityLogRepository activityLogRepository) {
        this.personRepository = personRepository;
        this.activityLogRepository = activityLogRepository;
    }

    @GetMapping("/weekly")
    public ResponseEntity<WeeklySummary> weekly(
            @PathVariable Long personId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Person person = personRepository.findById(personId).orElse(null);
        if (person == null) {
            return ResponseEntity.notFound().build();
        }

        // Find the start and end of the week that contains the given date
        LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue());
        LocalDate weekEnd = weekStart.plusDays(6);

        List<ActivityLog> logs =
                activityLogRepository.findByPersonIdAndDateBetween(personId, weekStart, weekEnd);

        Map<ActivityType, Integer> totals = new EnumMap<>(ActivityType.class);
        for (ActivityType type : ActivityType.values()) {
            totals.put(type, 0);
        }
        for (ActivityLog log : logs) {
            totals.merge(log.getType(), log.getMinutes(), Integer::sum);
        }

        int weeklyLimit = person.getAgeGroup().getDailyScreenLimitMinutes() * 7;
        boolean crossed = totals.get(ActivityType.SCREEN) > weeklyLimit;

        return ResponseEntity.ok(
                new WeeklySummary(personId, weekStart, weekEnd, totals, weeklyLimit, crossed));
    }
}