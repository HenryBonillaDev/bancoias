package com.bancoias.transfers.application.port.in;

import com.bancoias.transfers.domain.model.Transfer;
import reactor.core.publisher.Mono;

/** Puerto de entrada: consultar una transferencia por su referencia (RF06). */
public interface GetTransferUseCase {

	Mono<Transfer> getByRequestReference(String requestReference);
}
