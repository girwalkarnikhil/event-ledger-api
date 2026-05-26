package com.event.ledger.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.event.ledger.dto.EventRequest;
import com.event.ledger.model.Event;
import com.event.ledger.service.EventService;

import jakarta.validation.Valid;

@RestController
@RequestMapping
public class EventController {

    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    @PostMapping("/events")
    public ResponseEntity<Event> create(@Valid @RequestBody EventRequest request) {

        Event event = service.createEvent(request);

        // If duplicate -> 200 OK
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @GetMapping("/events/{id}")
    public Event getOne(@PathVariable String id) {
        return service.getByEventId(id);
    }

    @GetMapping("/events")
    public List<Event> getByAccount(@RequestParam String account) {
        return service.getByAccount(account);
    }

    @GetMapping("/accounts/{accountId}/balance")
    public Map<String, Object> balance(@PathVariable String accountId) {

        BigDecimal balance = service.computeBalance(accountId);

        return Map.of(
                "accountId", accountId,
                "balance", balance
        );
    }
}

