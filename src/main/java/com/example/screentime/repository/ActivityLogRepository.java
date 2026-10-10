package com.example.screentime.repository;

import com.example.screentime.model.ActivityLog;
import com.example.screentime.model.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByPersonIdAndDate(Long personId, LocalDate date);
    List<ActivityLog> findByPersonIdAndDateBetween(Long personId, LocalDate start, LocalDate end);

    @Query("select coalesce(sum(a.minutes), 0) from ActivityLog a "
            + "where a.personId = :personId and a.type = :type and a.date = :date")
    long totalMinutes(Long personId, ActivityType type, LocalDate date);
}
