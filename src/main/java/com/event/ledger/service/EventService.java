package com.event.ledger.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event.ledger.dto.EventRequest;
import com.event.ledger.model.Event;
import com.event.ledger.model.EventType;
import com.event.ledger.repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class EventService {

    private final EventRepository repository;
    private final ObjectMapper mapper;

    public EventService(EventRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Event createEvent(EventRequest request) {

        //Idempotency check
        Optional<Event> existing = repository.findByEventId(request.getEventId());
        if (existing.isPresent()) {
            return existing.get(); // return original
        }

        Event e = new Event();
        e.setEventId(request.getEventId());
        e.setAccountId(request.getAccountId());
        e.setType(request.getType());
        e.setAmount(request.getAmount());
        e.setCurrency(request.getCurrency());
        e.setEventTimestamp(request.getEventTimestamp());

        try {
            if (request.getMetadata() != null) {
                e.setMetadata(mapper.writeValueAsString(request.getMetadata()));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Metadata serialization failed");
        }

        return repository.save(e);
    }

    public Event getByEventId(String eventId) {
        return repository.findByEventId(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public List<Event> getByAccount(String accountId) {
        return repository.findByAccountIdOrderByEventTimestampAsc(accountId);
    }

    public BigDecimal computeBalance(String accountId) {

        List<Event> events = getByAccount(accountId);

        return events.stream()
                .map(e -> e.getType() == EventType.CREDIT
                        ? e.getAmount()
                        : e.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
