package com.bancoias.transfers.domain.policy;

import java.math.BigDecimal;

/**
 * Regla de negocio de RF02: una transferencia no puede hacer que la cuenta
 * origen supere su límite diario permitido. Es una función pura de dominio:
 * no conoce de dónde viene {@code consumedToday} (eso lo resuelve
 * {@code application}/{@code infrastructure} sumando las transferencias
 * autorizadas del día).
 */
public final class DailyLimitPolicy {

	private DailyLimitPolicy() {
	}

	public static boolean exceedsDailyLimit(BigDecimal consumedToday, BigDecimal amount, BigDecimal dailyLimit) {
		return consumedToday.add(amount).compareTo(dailyLimit) > 0;
	}
}
