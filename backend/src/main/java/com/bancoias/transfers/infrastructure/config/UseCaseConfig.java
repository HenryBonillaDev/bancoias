package com.bancoias.transfers.infrastructure.config;

import com.bancoias.transfers.application.port.out.AccountRepositoryPort;
import com.bancoias.transfers.application.port.out.TransferRepositoryPort;
import com.bancoias.transfers.application.service.AccountQueryService;
import com.bancoias.transfers.application.service.ProcessTransferService;
import com.bancoias.transfers.application.service.TransferQueryService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensambla los casos de uso de {@code application} con sus adaptadores de
 * {@code infrastructure}. Mantiene las clases de {@code application} libres
 * de anotaciones de Spring (ver ADR-001).
 */
@Configuration
public class UseCaseConfig {

	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}

	@Bean
	public ProcessTransferService processTransferService(
			TransferRepositoryPort transferRepository, AccountRepositoryPort accountRepository, Clock clock) {
		return new ProcessTransferService(transferRepository, accountRepository, clock);
	}

	@Bean
	public TransferQueryService transferQueryService(TransferRepositoryPort transferRepository) {
		return new TransferQueryService(transferRepository);
	}

	@Bean
	public AccountQueryService accountQueryService(
			AccountRepositoryPort accountRepository, TransferRepositoryPort transferRepository, Clock clock) {
		return new AccountQueryService(accountRepository, transferRepository, clock);
	}
}
