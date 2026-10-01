package com.bancoias.transfers.domain.model;

import java.math.BigDecimal;

/**
 * Vista de solo lectura de cuánto se ha consumido del límite diario de una
 * cuenta (ver {@link com.bancoias.transfers.domain.policy.DailyLimitPolicy}).
 * No es una entidad persistida: se calcula en el momento combinando
 * {@link Account} con la suma de transferencias autorizadas del día.
 */
public record AccountDailyUsage(String accountId, BigDecimal dailyLimit, BigDecimal consumedToday) {

	public BigDecimal remaining() {
		return dailyLimit.subtract(consumedToday);
	}
}
