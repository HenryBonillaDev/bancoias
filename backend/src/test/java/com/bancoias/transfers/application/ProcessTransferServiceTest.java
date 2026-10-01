package com.bancoias.transfers.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bancoias.transfers.application.port.in.ProcessTransferCommand;
import com.bancoias.transfers.application.port.out.AccountRepositoryPort;
import com.bancoias.transfers.application.port.out.TransactionalExecutionPort;
import com.bancoias.transfers.application.port.out.TransferRepositoryPort;
import com.bancoias.transfers.application.service.ProcessTransferService;
import com.bancoias.transfers.domain.model.Account;
import com.bancoias.transfers.domain.model.Transfer;
import com.bancoias.transfers.domain.model.TransferStatus;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ProcessTransferServiceTest {

	private static final Instant FIXED_INSTANT = Instant.parse("2026-09-30T15:00:00Z");
	private static final BigDecimal DAILY_LIMIT = BigDecimal.valueOf(5_000_000);

	@Mock
	private TransferRepositoryPort transferRepository;

	@Mock
	private AccountRepositoryPort accountRepository;

	@Mock
	private TransactionalExecutionPort transactionalExecution;

	private ProcessTransferService service;

	@BeforeEach
	void setUp() {
		Clock fixedClock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
		service = new ProcessTransferService(transferRepository, accountRepository, transactionalExecution, fixedClock);
		lenient().when(transferRepository.save(any(Transfer.class)))
				.thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
		// En la prueba unitaria no probamos la transacción real (eso lo cubre
		// la prueba de integración de concurrencia); aquí simplemente se deja
		// pasar la acción tal cual, como una transacción identidad.
		lenient().when(transactionalExecution.executeSerializable(any()))
				.thenAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	void returnsThePreviouslyProcessedTransferWhenReferenceWasAlreadyPersisted_RF05() {
		Transfer existing = Transfer.authorized("REF-001", "CTA-1001", "CTA-2001", BigDecimal.valueOf(1000), FIXED_INSTANT);
		when(transferRepository.findByRequestReference("REF-001")).thenReturn(Mono.just(existing));

		ProcessTransferCommand command = new ProcessTransferCommand(
				"REF-001", "CTA-1001", "CTA-2001", BigDecimal.valueOf(999_999_999));

		StepVerifier.create(service.process(command))
				.expectNext(existing)
				.verifyComplete();

		verify(transferRepository, never()).save(any());
		verify(accountRepository, never()).findById(anyString());
	}

	@Test
	void rejectsWhenAmountIsZeroOrNegative_RF02() {
		when(transferRepository.findByRequestReference("REF-002")).thenReturn(Mono.empty());

		ProcessTransferCommand command = new ProcessTransferCommand(
				"REF-002", "CTA-1001", "CTA-2001", BigDecimal.ZERO);

		StepVerifier.create(service.process(command))
				.assertNext(transfer -> {
					org.assertj.core.api.Assertions.assertThat(transfer.status()).isEqualTo(TransferStatus.REJECTED);
					org.assertj.core.api.Assertions.assertThat(transfer.reason())
							.isEqualTo("El valor de la transferencia debe ser mayor que cero");
				})
				.verifyComplete();

		verify(accountRepository, never()).findById(anyString());
	}

	@Test
	void rejectsWhenSourceAndDestinationAccountsAreTheSame_RF02() {
		when(transferRepository.findByRequestReference("REF-003")).thenReturn(Mono.empty());

		ProcessTransferCommand command = new ProcessTransferCommand(
				"REF-003", "CTA-1001", "CTA-1001", BigDecimal.valueOf(1000));

		StepVerifier.create(service.process(command))
				.assertNext(transfer -> org.assertj.core.api.Assertions.assertThat(transfer.reason())
						.isEqualTo("La cuenta origen y la cuenta destino deben ser diferentes"))
				.verifyComplete();
	}

	@Test
	void rejectsWhenSourceAccountDoesNotExist_RF02() {
		when(transferRepository.findByRequestReference("REF-004")).thenReturn(Mono.empty());
		when(accountRepository.findById("CTA-9999")).thenReturn(Mono.empty());

		ProcessTransferCommand command = new ProcessTransferCommand(
				"REF-004", "CTA-9999", "CTA-2001", BigDecimal.valueOf(1000));

		StepVerifier.create(service.process(command))
				.assertNext(transfer -> org.assertj.core.api.Assertions.assertThat(transfer.reason())
						.isEqualTo("La cuenta origen no es una cuenta válida"))
				.verifyComplete();
	}

	@Test
	void rejectsWhenDestinationAccountDoesNotExist_RF02() {
		when(transferRepository.findByRequestReference("REF-005")).thenReturn(Mono.empty());
		when(accountRepository.findById("CTA-1001")).thenReturn(Mono.just(new Account("CTA-1001", DAILY_LIMIT)));
		when(accountRepository.findById("CTA-9999")).thenReturn(Mono.empty());

		ProcessTransferCommand command = new ProcessTransferCommand(
				"REF-005", "CTA-1001", "CTA-9999", BigDecimal.valueOf(1000));

		StepVerifier.create(service.process(command))
				.assertNext(transfer -> org.assertj.core.api.Assertions.assertThat(transfer.reason())
						.isEqualTo("La cuenta destino no es una cuenta válida"))
				.verifyComplete();
	}

	@Test
	void rejectsWhenOperationExceedsSourceAccountDailyLimit_RF02() {
		when(transferRepository.findByRequestReference("REF-006")).thenReturn(Mono.empty());
		when(accountRepository.findById("CTA-1001")).thenReturn(Mono.just(new Account("CTA-1001", DAILY_LIMIT)));
		when(accountRepository.findById("CTA-2001")).thenReturn(Mono.just(new Account("CTA-2001", DAILY_LIMIT)));
		when(transferRepository.sumAuthorizedAmount(anyString(), any())).thenReturn(Mono.just(BigDecimal.valueOf(4_500_000)));

		ProcessTransferCommand command = new ProcessTransferCommand(
				"REF-006", "CTA-1001", "CTA-2001", BigDecimal.valueOf(600_000));

		StepVerifier.create(service.process(command))
				.assertNext(transfer -> {
					org.assertj.core.api.Assertions.assertThat(transfer.status()).isEqualTo(TransferStatus.REJECTED);
					org.assertj.core.api.Assertions.assertThat(transfer.reason())
							.isEqualTo("La operación supera el límite diario permitido de la cuenta origen");
				})
				.verifyComplete();
	}

	@Test
	void authorizesAValidTransferWithinTheDailyLimit() {
		when(transferRepository.findByRequestReference("REF-007")).thenReturn(Mono.empty());
		when(accountRepository.findById("CTA-1001")).thenReturn(Mono.just(new Account("CTA-1001", DAILY_LIMIT)));
		when(accountRepository.findById("CTA-2001")).thenReturn(Mono.just(new Account("CTA-2001", DAILY_LIMIT)));
		when(transferRepository.sumAuthorizedAmount(anyString(), any())).thenReturn(Mono.just(BigDecimal.ZERO));

		ProcessTransferCommand command = new ProcessTransferCommand(
				"REF-007", "CTA-1001", "CTA-2001", BigDecimal.valueOf(600_000));

		StepVerifier.create(service.process(command))
				.assertNext(transfer -> {
					org.assertj.core.api.Assertions.assertThat(transfer.status()).isEqualTo(TransferStatus.AUTHORIZED);
					org.assertj.core.api.Assertions.assertThat(transfer.reason()).isNull();
					org.assertj.core.api.Assertions.assertThat(transfer.processedAt()).isEqualTo(FIXED_INSTANT);
				})
				.verifyComplete();
	}
}
