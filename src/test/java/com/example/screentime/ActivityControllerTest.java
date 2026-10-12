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
class ActivityControllerTest {

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

    @Test
    void childOverScreenLimitGetsWarning() throws Exception {
        long id = createPerson("Aarav", "CHILD");

        mockMvc.perform(post("/api/persons/" + id + "/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SCREEN\",\"minutes\":100,\"date\":\"2026-10-12\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.warning").doesNotExist());

        mockMvc.perform(post("/api/persons/" + id + "/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SCREEN\",\"minutes\":40,\"date\":\"2026-10-12\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.warning").exists());
    }

    @Test
    void readingDoesNotCountTowardScreenLimit() throws Exception {
        long id = createPerson("Asha", "SENIOR");

        mockMvc.perform(post("/api/persons/" + id + "/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"READING\",\"minutes\":300,\"date\":\"2026-10-12\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.warning").doesNotExist());
    }

    @Test
    void unknownPersonReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/persons/9999/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SCREEN\",\"minutes\":10,\"date\":\"2026-10-12\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listsActivitiesForADay() throws Exception {
        long id = createPerson("Meera", "ADULT");

        mockMvc.perform(post("/api/persons/" + id + "/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"OUTDOOR\",\"minutes\":45,\"date\":\"2026-10-13\"}"));

        mockMvc.perform(get("/api/persons/" + id + "/activities?date=2026-10-13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].minutes").value(45));
    }
    @Test
    void negativeMinutesAreRejected() throws Exception {
        long id = createPerson("Neel", "CHILD");

        mockMvc.perform(post("/api/persons/" + id + "/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SCREEN\",\"minutes\":-5,\"date\":\"2026-10-12\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.minutes").exists());
    }

    @Test
    void zeroMinutesAreRejected() throws Exception {
        long id = createPerson("Zoya", "ADULT");

        mockMvc.perform(post("/api/persons/" + id + "/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"READING\",\"minutes\":0,\"date\":\"2026-10-12\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.minutes").value("minutes must be at least 1"));
    }

    @Test
    void moreThanOneDayOfMinutesIsRejectedButExactlyOneDayIsAllowed() throws Exception {
        long id = createPerson("Om", "ADULT");

        mockMvc.perform(post("/api/persons/" + id + "/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"OUTDOOR\",\"minutes\":1441,\"date\":\"2026-10-12\"}"))
                              .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.minutes")
                        .value("minutes cannot be more than 1440 in a single entry"));

        mockMvc.perform(post("/api/persons/" + id + "/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"OUTDOOR\",\"minutes\":1440,\"date\":\"2026-10-12\"}"))
                .andExpect(status().isCreated());
    }
    @Test
    void missingMinutesIsReportedAsRequired() throws Exception {
        long id = createPerson("Ria", "SENIOR");

        mockMvc.perform(post("/api/persons/" + id + "/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SCREEN\",\"date\":\"2026-10-12\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.minutes").value("minutes is required"));
    }
}
