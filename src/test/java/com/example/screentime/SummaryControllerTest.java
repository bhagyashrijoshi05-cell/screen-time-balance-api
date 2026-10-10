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
}