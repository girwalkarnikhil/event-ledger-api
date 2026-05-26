package com.event.ledger.dto;

import java.math.BigDecimal;

public record BalanceResponse(
        String accountId,
        BigDecimal balance
) {
}
