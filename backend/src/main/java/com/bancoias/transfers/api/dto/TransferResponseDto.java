package com.bancoias.transfers.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** Representación HTTP de una transferencia procesada (RF03, RF06). */
public record TransferResponseDto(
		String requestReference,
		String sourceAccountId,
		String destinationAccountId,
		BigDecimal amount,
		String status,
		String reason,
		Instant processedAt) {
}
