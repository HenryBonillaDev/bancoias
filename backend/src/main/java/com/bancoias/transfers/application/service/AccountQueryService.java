package com.bancoias.transfers.application.service;

import com.bancoias.transfers.application.port.in.GetAccountDailyUsageUseCase;
import com.bancoias.transfers.application.port.out.AccountRepositoryPort;
import com.bancoias.transfers.application.port.out.TransferRepositoryPort;
import com.bancoias.transfers.domain.model.AccountDailyUsage;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import reactor.core.publisher.Mono;

/** Caso de uso de solo lectura para ver el consumo del límite diario de una cuenta. */
public class AccountQueryService implements GetAccountDailyUsageUseCase {

	private final AccountRepositoryPort accountRepository;
	private final TransferRepositoryPort transferRepository;
	private final Clock clock;

	public AccountQueryService(
			AccountRepositoryPort accountRepository, TransferRepositoryPort transferRepository, Clock clock) {
		this.accountRepository = accountRepository;
		this.transferRepository = transferRepository;
		this.clock = clock;
	}

	@Override
	public Mono<AccountDailyUsage> getDailyUsage(String accountId) {
		return accountRepository.findById(accountId)
				.flatMap(account -> {
					LocalDate today = LocalDate.now(clock);
					return transferRepository.sumAuthorizedAmount(account.accountId(), today)
							.defaultIfEmpty(BigDecimal.ZERO)
							.map(consumedToday -> new AccountDailyUsage(account.accountId(), account.dailyLimit(), consumedToday));
				});
	}
}
