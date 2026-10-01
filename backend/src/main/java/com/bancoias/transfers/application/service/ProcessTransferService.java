package com.bancoias.transfers.application.service;

import com.bancoias.transfers.application.port.in.ProcessTransferCommand;
import com.bancoias.transfers.application.port.in.ProcessTransferUseCase;
import com.bancoias.transfers.application.port.out.AccountRepositoryPort;
import com.bancoias.transfers.application.port.out.TransferRepositoryPort;
import com.bancoias.transfers.domain.model.Account;
import com.bancoias.transfers.domain.model.Transfer;
import com.bancoias.transfers.domain.policy.DailyLimitPolicy;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import reactor.core.publisher.Mono;

/**
 * Caso de uso de RF01/RF02/RF03/RF05.
 *
 * <p>Nota de alcance (ver docs/REQUIREMENTS.md y docs/DECISIONS.md): esta
 * implementación cubre la validación de negocio y la idempotencia básica por
 * referencia repetida, pero la secuencia "leer acumulado del día + guardar"
 * no es todavía atómica frente a solicitudes concurrentes sobre la misma
 * cuenta origen (RF04). El endurecimiento de concurrencia es el siguiente
 * paso explícito del plan, no una garantía de esta clase.
 */
public class ProcessTransferService implements ProcessTransferUseCase {

	private final TransferRepositoryPort transferRepository;
	private final AccountRepositoryPort accountRepository;
	private final Clock clock;

	public ProcessTransferService(
			TransferRepositoryPort transferRepository,
			AccountRepositoryPort accountRepository,
			Clock clock) {
		this.transferRepository = transferRepository;
		this.accountRepository = accountRepository;
		this.clock = clock;
	}

	@Override
	public Mono<Transfer> process(ProcessTransferCommand command) {
		return transferRepository.findByRequestReference(command.requestReference())
				.switchIfEmpty(Mono.defer(() -> validateAndSave(command)));
	}

	private Mono<Transfer> validateAndSave(ProcessTransferCommand command) {
		Instant now = Instant.now(clock);

		Optional<String> basicRejection = validateBasicRules(command);
		if (basicRejection.isPresent()) {
			return transferRepository.save(rejected(command, basicRejection.get(), now));
		}

		return resolveRejectionReason(command, now)
				.map(reason -> reason.isPresent()
						? rejected(command, reason.get(), now)
						: authorized(command, now))
				.flatMap(transferRepository::save);
	}

	private Mono<Optional<String>> resolveRejectionReason(ProcessTransferCommand command, Instant now) {
		return accountRepository.findById(command.sourceAccountId())
				.flatMap(sourceAccount -> accountRepository.findById(command.destinationAccountId())
						.flatMap(destinationAccount -> dailyLimitRejection(command, sourceAccount, now))
						.switchIfEmpty(Mono.just(Optional.of("La cuenta destino no es una cuenta válida"))))
				.switchIfEmpty(Mono.just(Optional.of("La cuenta origen no es una cuenta válida")));
	}

	private Mono<Optional<String>> dailyLimitRejection(ProcessTransferCommand command, Account sourceAccount, Instant now) {
		LocalDate today = now.atZone(clock.getZone()).toLocalDate();
		return transferRepository.sumAuthorizedAmount(sourceAccount.accountId(), today)
				.defaultIfEmpty(BigDecimal.ZERO)
				.map(consumedToday -> DailyLimitPolicy.exceedsDailyLimit(consumedToday, command.amount(), sourceAccount.dailyLimit())
						? Optional.of("La operación supera el límite diario permitido de la cuenta origen")
						: Optional.<String>empty());
	}

	private static Optional<String> validateBasicRules(ProcessTransferCommand command) {
		if (command.amount() == null || command.amount().compareTo(BigDecimal.ZERO) <= 0) {
			return Optional.of("El valor de la transferencia debe ser mayor que cero");
		}
		if (Objects.equals(command.sourceAccountId(), command.destinationAccountId())) {
			return Optional.of("La cuenta origen y la cuenta destino deben ser diferentes");
		}
		return Optional.empty();
	}

	private static Transfer authorized(ProcessTransferCommand command, Instant now) {
		return Transfer.authorized(
				command.requestReference(), command.sourceAccountId(), command.destinationAccountId(),
				command.amount(), now);
	}

	private static Transfer rejected(ProcessTransferCommand command, String reason, Instant now) {
		return Transfer.rejected(
				command.requestReference(), command.sourceAccountId(), command.destinationAccountId(),
				command.amount(), reason, now);
	}
}
