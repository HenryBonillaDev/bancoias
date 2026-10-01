package com.bancoias.transfers.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad de persistencia R2DBC, separada del modelo de dominio
 * {@link com.bancoias.transfers.domain.model.Transfer} (ver ADR-001). El
 * mapeo entre ambas la hace {@code TransferEntityMapper}.
 */
@Table("transfers")
public record TransferEntity(
		@Id Long id,
		String requestReference,
		String sourceAccountId,
		String destinationAccountId,
		BigDecimal amount,
		String status,
		String reason,
		Instant processedAt) {

	public static TransferEntity forInsert(
			String requestReference,
			String sourceAccountId,
			String destinationAccountId,
			BigDecimal amount,
			String status,
			String reason,
			Instant processedAt) {
		return new TransferEntity(
				null, requestReference, sourceAccountId, destinationAccountId, amount, status, reason, processedAt);
	}
}
