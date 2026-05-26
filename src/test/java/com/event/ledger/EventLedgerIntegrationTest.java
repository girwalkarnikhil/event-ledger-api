package com.event.ledger;

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
class EventLedgerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldHandleDuplicateEventsIdempotently() throws Exception {

        String payload = """
                {
                  "eventId": "evt-001",
                  "accountId": "acct-100",
                  "type": "CREDIT",
                  "amount": 150.00,
                  "currency": "USD",
                  "eventTimestamp": "2026-05-15T14:02:11Z"
                }
                """;

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/accounts/acct-100/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(150.00));
    }

    @Test
    void shouldReturnEventsOrderedByTimestamp() throws Exception {

        String laterEvent = """
                {
                  "eventId": "evt-002",
                  "accountId": "acct-200",
                  "type": "CREDIT",
                  "amount": 200.00,
                  "currency": "USD",
                  "eventTimestamp": "2026-05-15T15:00:00Z"
                }
                """;

        String earlierEvent = """
                {
                  "eventId": "evt-003",
                  "accountId": "acct-200",
                  "type": "DEBIT",
                  "amount": 50.00,
                  "currency": "USD",
                  "eventTimestamp": "2026-05-15T13:00:00Z"
                }
                """;

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(laterEvent))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(earlierEvent))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/events?account=acct-200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId").value("evt-003"))
                .andExpect(jsonPath("$[1].eventId").value("evt-002"));
    }

    @Test
    void shouldComputeBalanceCorrectly() throws Exception {

        String credit = """
                {
                  "eventId": "evt-004",
                  "accountId": "acct-300",
                  "type": "CREDIT",
                  "amount": 500.00,
                  "currency": "USD",
                  "eventTimestamp": "2026-05-15T10:00:00Z"
                }
                """;

        String debit = """
                {
                  "eventId": "evt-005",
                  "accountId": "acct-300",
                  "type": "DEBIT",
                  "amount": 120.00,
                  "currency": "USD",
                  "eventTimestamp": "2026-05-15T11:00:00Z"
                }
                """;

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credit))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(debit))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/accounts/acct-300/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(380.00));
    }
    
    @Test
    void shouldRejectInvalidAmount() throws Exception {

        String invalidPayload = """
                {
                  "eventId": "evt-006",
                  "accountId": "acct-400",
                  "type": "CREDIT",
                  "amount": 0,
                  "currency": "USD",
                  "eventTimestamp": "2026-05-15T14:02:11Z"
                }
                """;

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void shouldRejectInvalidEventType() throws Exception {

        String invalidPayload = """
                {
                  "eventId": "evt-007",
                  "accountId": "acct-500",
                  "type": "INVALID",
                  "amount": 100,
                  "currency": "USD",
                  "eventTimestamp": "2026-05-15T14:02:11Z"
                }
                """;

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }
}
