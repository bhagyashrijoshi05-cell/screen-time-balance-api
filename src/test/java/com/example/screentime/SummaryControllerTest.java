package com.example.screentime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long createPerson(String name, String ageGroup) throws Exception {
        String body = mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"ageGroup\":\"" + ageGroup + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private void log(long id, String type, int minutes, String date) throws Exception {
        mockMvc.perform(post("/api/persons/" + id + "/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"" + type + "\",\"minutes\":" + minutes
                                + ",\"date\":\"" + date + "\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void weeklySummaryAddsUpMinutesByType() throws Exception {
        long id = createPerson("Riya", "ADULT");
        log(id, "SCREEN", 60, "2026-10-13");
        log(id, "SCREEN", 30, "2026-10-14");
        log(id, "READING", 45, "2026-10-15");

        mockMvc.perform(get("/api/persons/" + id + "/summary/weekly?date=2026-10-14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minutesByType.SCREEN").value(90))
                .andExpect(jsonPath("$.minutesByType.READING").value(45))
                .andExpect(jsonPath("$.screenLimitCrossed").value(false));
    }

    @Test
    void weeklySummaryForUnknownPersonReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/persons/9999/summary/weekly?date=2026-10-14"))
                .andExpect(status().isNotFound());
    }
    @Test
    void weeklySummaryIgnoresDaysFromNeighbouringWeeks() throws Exception {
        long id = createPerson("Dev", "ADULT");
        log(id, "SCREEN", 11, "2026-10-11");   // Sunday before the week, excluded
        log(id, "SCREEN", 20, "2026-10-12");   // Monday, first day, included
        log(id, "SCREEN", 30, "2026-10-18");   // Sunday, last day, included
        log(id, "SCREEN", 99, "2026-10-19");   // Monday after the week, excluded

        mockMvc.perform(get("/api/persons/" + id + "/summary/weekly?date=2026-10-14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minutesByType.SCREEN").value(50));
    }
    @Test
    void sundayBelongsToTheWeekThatEndsOnThatSunday() throws Exception {
        long id = createPerson("Kabir", "ADULT");
        log(id, "SCREEN", 20, "2026-10-05");   // Monday
        log(id, "SCREEN", 50, "2026-10-11");   // Sunday
        log(id, "SCREEN", 100, "2026-10-12");  // next Monday, must be excluded

        mockMvc.perform(get("/api/persons/" + id + "/summary/weekly?date=2026-10-11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weekStart").value("2026-10-05"))
                .andExpect(jsonPath("$.weekEnd").value("2026-10-11"))
                .andExpect(jsonPath("$.minutesByType.SCREEN").value(70));
    }

    @Test
    void mondayStartsANewWeek() throws Exception {
        long id = createPerson("Ira", "ADULT");
        log(id, "READING", 30, "2026-10-12");

        mockMvc.perform(get("/api/persons/" + id + "/summary/weekly?date=2026-10-12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weekStart").value("2026-10-12"))
                .andExpect(jsonPath("$.weekEnd").value("2026-10-18"))
                .andExpect(jsonPath("$.minutesByType.READING").value(30));
    }




    @Test
    void screenLimitIsCrossedOnlyAboveTheWeeklyLimit() throws Exception {
        long exact = createPerson("Neel", "ADULT");   // weekly limit 240 x 7 = 1680
        log(exact, "SCREEN", 1000, "2026-10-12");
        log(exact, "SCREEN", 680, "2026-10-13");

        mockMvc.perform(get("/api/persons/" + exact + "/summary/weekly?date=2026-10-14"))
                .andExpect(jsonPath("$.weeklyScreenLimit").value(1680))
                .andExpect(jsonPath("$.screenLimitCrossed").value(false));

        long over = createPerson("Tara", "ADULT");
        log(over, "SCREEN", 1000, "2026-10-12");
        log(over, "SCREEN", 681, "2026-10-13");

        mockMvc.perform(get("/api/persons/" + over + "/summary/weekly?date=2026-10-14"))
                .andExpect(jsonPath("$.screenLimitCrossed").value(true));
    }
}