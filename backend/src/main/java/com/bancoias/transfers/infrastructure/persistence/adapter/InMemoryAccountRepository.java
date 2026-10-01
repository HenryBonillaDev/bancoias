package com.bancoias.transfers.infrastructure.persistence.adapter;

import com.bancoias.transfers.application.port.out.AccountRepositoryPort;
import com.bancoias.transfers.domain.model.Account;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Datos semilla de cuentas válidas (ver docs/REQUIREMENTS.md §3). El
 * enunciado indica explícitamente que no se requiere un módulo de
 * administración de cuentas ni integración con un sistema externo, por lo
 * que un mapa fijo en memoria es suficiente para este ejercicio.
 */
@Component
public class InMemoryAccountRepository implements AccountRepositoryPort {

	private static final BigDecimal DEFAULT_DAILY_LIMIT = new BigDecimal("5000000");

	private final Map<String, Account> accounts = Map.of(
			"CTA-1001", new Account("CTA-1001", DEFAULT_DAILY_LIMIT),
			"CTA-1002", new Account("CTA-1002", DEFAULT_DAILY_LIMIT),
			"CTA-2001", new Account("CTA-2001", DEFAULT_DAILY_LIMIT));

	@Override
	public Mono<Account> findById(String accountId) {
		return Mono.justOrEmpty(accounts.get(accountId));
	}
}
