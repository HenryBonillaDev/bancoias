package com.bancoias.transfers.infrastructure.persistence;

import com.bancoias.transfers.domain.model.Transfer;
import com.bancoias.transfers.domain.model.TransferStatus;
import com.bancoias.transfers.infrastructure.persistence.entity.TransferEntity;

public final class TransferEntityMapper {

	private TransferEntityMapper() {
	}

	public static TransferEntity toNewEntity(Transfer transfer) {
		return TransferEntity.forInsert(
				transfer.requestReference(),
				transfer.sourceAccountId(),
				transfer.destinationAccountId(),
				transfer.amount(),
				transfer.status().name(),
				transfer.reason(),
				transfer.processedAt());
	}

	public static Transfer toDomain(TransferEntity entity) {
		return new Transfer(
				entity.requestReference(),
				entity.sourceAccountId(),
				entity.destinationAccountId(),
				entity.amount(),
				TransferStatus.valueOf(entity.status()),
				entity.reason(),
				entity.processedAt());
	}
}
