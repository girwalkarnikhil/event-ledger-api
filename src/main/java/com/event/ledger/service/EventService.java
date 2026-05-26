package com.event.ledger.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event.ledger.dto.BalanceResponse;
import com.event.ledger.dto.EventRequest;
import com.event.ledger.exception.ResourceNotFoundException;
import com.event.ledger.model.Event;
import com.event.ledger.repository.EventRepository;

@Service
@Transactional
public class EventService {

    private final EventRepository repository;

    public EventService(EventRepository repository) {
        this.repository = repository;
    }

    public Event createEvent(EventRequest request) {

        //Idempotency check
        Optional<Event> existing = repository.findByEventId(request.getEventId());
        if (existing.isPresent()) {
            return existing.get(); // return original
        }

        Event event = new Event();
        event.setEventId(request.getEventId());
        event.setAccountId(request.getAccountId());
        event.setType(request.getType());
        event.setAmount(request.getAmount());
        event.setCurrency(request.getCurrency());
        event.setEventTimestamp(request.getEventTimestamp());
        event.setMetadata(request.getMetadata());

        return repository.save(event);
    }

    public Event getEventByEventId(String eventId) {
        return repository.findByEventId(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Event not found: " + eventId
                ));
    }

    public List<Event> getEventsByAccountId(String accountId) {
        return repository.findByAccountIdOrderByEventTimestampAsc(accountId);
    }

    public BalanceResponse getBalance(String accountId) {
        BigDecimal balance = repository.calculateBalance(accountId);
        return new BalanceResponse(accountId, balance);
    }
}
