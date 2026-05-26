package com.event.ledger.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.event.ledger.dto.BalanceResponse;
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

        boolean exists = false;

        try {
            service.getEventByEventId(request.getEventId());
            exists = true;
        } catch (Exception ignored) {
        }

        Event response = service.createEvent(request);

        // If duplicate -> 200 OK
        return ResponseEntity
                .status(exists ? HttpStatus.OK : HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/events/{id}")
    public Event getEvent(@PathVariable String id) {
        return service.getEventByEventId(id);
    }

    @GetMapping("/events")
    public List<Event> getEventsByAccount(@RequestParam String account) {
        return service.getEventsByAccountId(account);
    }

    @GetMapping("/accounts/{accountId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String accountId) {
        return ResponseEntity.ok(service.getBalance(accountId));
    }
}

