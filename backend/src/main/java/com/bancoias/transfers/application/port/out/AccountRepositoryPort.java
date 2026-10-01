package com.bancoias.transfers.application.port.out;

import com.bancoias.transfers.domain.model.Account;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida hacia el origen de cuentas válidas. Implementado en
 * {@code infrastructure} con los datos semilla del enunciado (CTA-1001,
 * CTA-1002, CTA-2001); no requiere un módulo de administración de cuentas.
 */
public interface AccountRepositoryPort {

	Mono<Account> findById(String accountId);
}
