package com.bancoias.transfers.application.service;

import com.bancoias.transfers.application.port.in.ProcessTransferCommand;
import com.bancoias.transfers.application.port.in.ProcessTransferUseCase;
import com.bancoias.transfers.application.port.out.AccountRepositoryPort;
import com.bancoias.transfers.application.port.out.TransactionalExecutionPort;
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
 * Caso de uso de RF01/RF02/RF03/RF04/RF05.
 *
 * <p>RF04: la secuencia "leer el acumulado autorizado del día + decidir +
 * guardar" ({@link #decideAndSave}) se ejecuta dentro de
 * {@link TransactionalExecutionPort#executeSerializable}, una transacción con
 * aislamiento serializable con reintento. Si dos solicitudes concurrentes
 * sobre la misma cuenta origen leen el mismo acumulado antes de que la otra
 * confirme, la base de datos aborta una de las dos con un conflicto de
 * serialización; el adaptador la reintenta automáticamente, relee el
 * acumulado ya actualizado y vuelve a decidir — preservando el límite diario
 * aun bajo concurrencia real.
 */
public class ProcessTransferService implements ProcessTransferUseCase {

	private final TransferRepositoryPort transferRepository;
	private final AccountRepositoryPort accountRepository;
	private final TransactionalExecutionPort transactionalExecution;
	private final Clock clock;

	public ProcessTransferService(
			TransferRepositoryPort transferRepository,
			AccountRepositoryPort accountRepository,
			TransactionalExecutionPort transactionalExecution,
			Clock clock) {
		this.transferRepository = transferRepository;
		this.accountRepository = accountRepository;
		this.transactionalExecution = transactionalExecution;
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

		return accountRepository.findById(command.sourceAccountId())
				.flatMap(sourceAccount -> accountRepository.findById(command.destinationAccountId())
						.flatMap(destinationAccount -> transactionalExecution.executeSerializable(
								decideAndSave(command, sourceAccount, now)))
						.switchIfEmpty(Mono.defer(() -> transferRepository.save(
								rejected(command, "La cuenta destino no es una cuenta válida", now)))))
				.switchIfEmpty(Mono.defer(() -> transferRepository.save(
						rejected(command, "La cuenta origen no es una cuenta válida", now))));
	}

	/**
	 * Sección crítica de RF04: debe ejecutarse completa dentro de una única
	 * transacción serializable (leer el acumulado, decidir, guardar), nunca
	 * en pasos separados, o la protección contra concurrencia no aplicaría.
	 */
	private Mono<Transfer> decideAndSave(ProcessTransferCommand command, Account sourceAccount, Instant now) {
		LocalDate today = now.atZone(clock.getZone()).toLocalDate();
		return transferRepository.sumAuthorizedAmount(sourceAccount.accountId(), today)
				.defaultIfEmpty(BigDecimal.ZERO)
				.flatMap(consumedToday -> {
					Transfer transfer = DailyLimitPolicy.exceedsDailyLimit(consumedToday, command.amount(), sourceAccount.dailyLimit())
							? rejected(command, "La operación supera el límite diario permitido de la cuenta origen", now)
							: authorized(command, now);
					return transferRepository.save(transfer);
				});
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
