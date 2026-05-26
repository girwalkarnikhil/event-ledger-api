package com.event.ledger.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event.ledger.dto.BalanceResponse;
import com.event.ledger.dto.EventRequest;
import com.event.ledger.model.Event;
import com.event.ledger.repository.EventRepository;

@Service
@Transactional
public class EventService {

    private final EventRepository repository;

    public EventService(EventRepository repository) {
        this.repository = repository;
    }
    
    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    public Event createEvent(EventRequest request) {
    	
    	log.info("Received event creation request: eventId={}, accountId={}", request.getEventId(), request.getAccountId());

        //Idempotency check
        Optional<Event> existing = repository.findByEventId(request.getEventId());
        if (existing.isPresent()) {
        	log.warn("Duplicate event detected (idempotency hit): eventId={}", request.getEventId());
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
    	log.debug("Fetching event by eventId={}", eventId);
        return repository.findByEventId(eventId)
                .orElseThrow(() -> {
                    log.error("Event not found: eventId={}", eventId);
                    return new RuntimeException("Event not found");
                });

    }

    public List<Event> getEventsByAccountId(String accountId) {
    	log.debug("Fetching events for accountId={}", accountId);
        List<Event> events = repository.findByAccountIdOrderByEventTimestampAsc(accountId);
        log.info("Fetched {} events for accountId={}", events.size(), accountId);
        return events;
    }

    public BalanceResponse getBalance(String accountId) {
    	log.info("Computing balance for accountId={}", accountId);
        BigDecimal balance = repository.calculateBalance(accountId);
        log.info("Computed balance={} for accountId={}", balance, accountId);
        return new BalanceResponse(accountId, balance);
    }
}
