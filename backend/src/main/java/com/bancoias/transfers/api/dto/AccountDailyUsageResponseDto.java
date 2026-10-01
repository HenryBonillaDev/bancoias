package com.bancoias.transfers.api.dto;

import java.math.BigDecimal;

/** Respuesta de {@code GET /api/accounts/{accountId}/daily-usage}. */
public record AccountDailyUsageResponseDto(
		String accountId,
		BigDecimal dailyLimit,
		BigDecimal consumedToday,
		BigDecimal remaining) {
}
