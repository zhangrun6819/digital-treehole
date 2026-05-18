package com.compe.treehole;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class TreeholeFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void visitorCanCreateSessionAndSendMessage() throws Exception {
        String visitorBody = mockMvc.perform(post("/api/v1/visitors/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String visitorToken = JsonPath.read(visitorBody, "$.data.token");

        String sessionBody = mockMvc.perform(post("/api/v1/chat/sessions")
                        .header("Authorization", "Bearer " + visitorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"companionStyle":"PEER","title":"考试压力"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Integer sessionId = JsonPath.read(sessionBody, "$.data.id");

        mockMvc.perform(post("/api/v1/chat/sessions/{sessionId}/messages", sessionId)
                        .header("Authorization", "Bearer " + visitorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"inputType":"TEXT","content":"我最近因为考试很焦虑，但还是想试着说出来"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assistantMessage.content", notNullValue()))
                .andExpect(jsonPath("$.data.assistantMessage.emotionTag").value("ANXIOUS"));

        mockMvc.perform(get("/api/v1/chat/stats/star-map")
                        .header("Authorization", "Bearer " + visitorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.points[0].emotionTag").value("ANXIOUS"));
    }

    @Test
    void adminCanLogin() throws Exception {
        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"admin123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token", notNullValue()));
    }
}
