package com.bancoias.transfers.application.port.in;

import java.math.BigDecimal;

/**
 * Comando de entrada del caso de uso de RF01. Representa la solicitud tal
 * como llega desde {@code api}, antes de ser validada contra las reglas de
 * negocio de RF02.
 */
public record ProcessTransferCommand(
		String requestReference,
		String sourceAccountId,
		String destinationAccountId,
		BigDecimal amount) {
}
