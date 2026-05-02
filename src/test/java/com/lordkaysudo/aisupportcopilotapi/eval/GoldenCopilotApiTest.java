package com.lordkaysudo.aisupportcopilotapi.eval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lordkaysudo.aisupportcopilotapi.support.TestJwtTokens;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GoldenCopilotApiTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    static Stream<Arguments> cases() throws Exception {
        JsonNode root = OBJECT_MAPPER.readTree(
                GoldenCopilotApiTest.class.getResourceAsStream("/golden/copilot-cases.json")
        );
        List<Arguments> out = new ArrayList<>();
        for (JsonNode node : root) {
            String id = node.path("id").asText("case");
            out.add(Arguments.of(id, node));
        }
        return out.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void copilotCasesMatchGoldenExpectations(String caseId, JsonNode c) throws Exception {
        int expectedStatus = c.path("expectHttpStatus").asInt();
        JsonNode request = c.get("request");
        String body = OBJECT_MAPPER.writeValueAsString(request);

        ResultActions actions = mockMvc.perform(post("/api/copilot/answer")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestJwtTokens.agentToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        actions.andExpect(status().is(expectedStatus));

        if (expectedStatus == 200 && c.has("answerMustContainAny") && c.get("answerMustContainAny").isArray()) {
            List<Matcher<? super String>> matchers = new ArrayList<>();
            for (JsonNode frag : c.get("answerMustContainAny")) {
                matchers.add(containsString(frag.asText()));
            }
            @SuppressWarnings("unchecked")
            Matcher<String>[] arr = matchers.toArray(Matcher[]::new);
            actions.andExpect(jsonPath("$.answer").value(Matchers.anyOf(arr)));
            actions.andExpect(jsonPath("$.sources").isArray());
            actions.andExpect(jsonPath("$.confidence").isNumber());
        }
    }
}
