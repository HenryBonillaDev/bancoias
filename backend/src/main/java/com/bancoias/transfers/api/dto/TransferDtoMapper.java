package com.bancoias.transfers.api.dto;

import com.bancoias.transfers.application.port.in.ProcessTransferCommand;
import com.bancoias.transfers.domain.model.Transfer;

public final class TransferDtoMapper {

	private TransferDtoMapper() {
	}

	/**
	 * Recorta espacios en los campos de texto antes de construir el comando.
	 * Evita que, p. ej., {@code " REF-001"} y {@code "REF-001"} se traten
	 * como referencias distintas (rompería la idempotencia de RF05) o que
	 * cuentas válidas sean rechazadas por un espacio accidental en el input.
	 */
	public static ProcessTransferCommand toCommand(TransferRequestDto dto) {
		return new ProcessTransferCommand(
				trim(dto.requestReference()), trim(dto.sourceAccountId()), trim(dto.destinationAccountId()), dto.amount());
	}

	private static String trim(String value) {
		return value == null ? null : value.trim();
	}

	public static TransferResponseDto toResponse(Transfer transfer) {
		return new TransferResponseDto(
				transfer.requestReference(),
				transfer.sourceAccountId(),
				transfer.destinationAccountId(),
				transfer.amount(),
				transfer.status().name(),
				transfer.reason(),
				transfer.processedAt());
	}
}
