package com.bancoias.transfers.domain.model;

import java.math.BigDecimal;

/**
 * Cuenta válida del core de transferencias, con su límite diario permitido.
 * No incluye saldo: el enunciado solo requiere validar el límite diario de
 * transferencias autorizadas, no un balance disponible.
 */
public record Account(String accountId, BigDecimal dailyLimit) {
}
