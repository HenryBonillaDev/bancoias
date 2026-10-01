package com.bancoias.transfers.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.bancoias.transfers.domain.model.Transfer;
import com.bancoias.transfers.domain.model.TransferStatus;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TransferTest {

	private static final Instant NOW = Instant.parse("2026-09-30T12:00:00Z");

	@Test
	void authorizedFactoryBuildsAnAuthorizedTransferWithNoReason() {
		Transfer transfer = Transfer.authorized("REF-001", "CTA-1001", "CTA-2001", BigDecimal.valueOf(1000), NOW);

		assertThat(transfer.status()).isEqualTo(TransferStatus.AUTHORIZED);
		assertThat(transfer.reason()).isNull();
		assertThat(transfer.isAuthorized()).isTrue();
		assertThat(transfer.processedAt()).isEqualTo(NOW);
	}

	@Test
	void rejectedFactoryBuildsARejectedTransferWithReason() {
		Transfer transfer = Transfer.rejected(
				"REF-002", "CTA-1001", "CTA-1001", BigDecimal.valueOf(1000), "cuentas iguales", NOW);

		assertThat(transfer.status()).isEqualTo(TransferStatus.REJECTED);
		assertThat(transfer.reason()).isEqualTo("cuentas iguales");
		assertThat(transfer.isAuthorized()).isFalse();
	}
}
