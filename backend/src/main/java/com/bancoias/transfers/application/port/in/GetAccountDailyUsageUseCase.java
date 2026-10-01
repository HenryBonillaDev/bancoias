package com.bancoias.transfers.application.port.in;

import com.bancoias.transfers.domain.model.AccountDailyUsage;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada de solo lectura: consultar cuánto del límite diario ha
 * consumido una cuenta. No forma parte de los RF obligatorios del
 * enunciado (que explícitamente no pide un módulo de administración de
 * cuentas); se agrega como visibilidad para verificar manualmente RF02/RF04.
 */
public interface GetAccountDailyUsageUseCase {

	Mono<AccountDailyUsage> getDailyUsage(String accountId);
}
