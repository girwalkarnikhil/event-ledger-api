package com.event.ledger.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.event.ledger.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByEventId(String eventId);

    List<Event> findByAccountIdOrderByEventTimestampAsc(String accountId);
    
    @Query("""
            SELECT COALESCE(
                SUM(CASE WHEN e.type = 'CREDIT' THEN e.amount ELSE -e.amount END),
                0
            )
            FROM Event e
            WHERE e.accountId = :accountId
            """)
    BigDecimal calculateBalance(String accountId);
}

