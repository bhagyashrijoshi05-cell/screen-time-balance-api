package com.example.screentime.controller;

import com.example.screentime.dto.ActivityRequest;
import com.example.screentime.dto.ActivityResponse;
import com.example.screentime.model.ActivityLog;
import com.example.screentime.model.ActivityType;
import com.example.screentime.model.Person;
import com.example.screentime.repository.ActivityLogRepository;
import com.example.screentime.repository.PersonRepository;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/persons/{personId}/activities")
public class ActivityController {

    private final PersonRepository personRepository;
    private final ActivityLogRepository activityLogRepository;

    public ActivityController(PersonRepository personRepository,
                              ActivityLogRepository activityLogRepository) {
        this.personRepository = personRepository;
        this.activityLogRepository = activityLogRepository;
    }

    @PostMapping
    public ResponseEntity<ActivityResponse> log(@PathVariable Long personId,
                                                @Valid @RequestBody ActivityRequest request) {
        Person person = personRepository.findById(personId).orElse(null);
        if (person == null) {
            return ResponseEntity.notFound().build();
        }

        ActivityLog saved = activityLogRepository.save(
                new ActivityLog(personId, request.type(), request.minutes(), request.date()));

        // Warn when today's screen time goes past the limit for this age group
        String warning = null;
        if (request.type() == ActivityType.SCREEN) {
            long total =activityLogRepository.totalMinutes(personId, ActivityType.SCREEN, request.date());
            int limit = person.getAgeGroup().getDailyScreenLimitMinutes();
            if (total > limit) {
                warning = "Screen time limit of " + limit + " minutes crossed (total " + total + ")";
            }
        }

        ActivityResponse response = new ActivityResponse(
                saved.getId(), saved.getType(), saved.getMinutes(), saved.getDate(), warning);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<ActivityLog> listForDay(@PathVariable Long personId,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return activityLogRepository.findByPersonIdAndDate(personId, date);
    }
}
