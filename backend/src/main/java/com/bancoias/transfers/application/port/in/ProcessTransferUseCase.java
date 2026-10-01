package com.bancoias.transfers.application.port.in;

import com.bancoias.transfers.domain.model.Transfer;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada: procesar una solicitud de transferencia (RF01, RF02,
 * RF03, RF05). Siempre devuelve un {@link Transfer} — autorizado o
 * rechazado — nunca lanza una excepción de negocio.
 */
public interface ProcessTransferUseCase {

	Mono<Transfer> process(ProcessTransferCommand command);
}
