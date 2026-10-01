package com.bancoias.transfers.application.port.in;

import com.bancoias.transfers.domain.model.Transfer;
import reactor.core.publisher.Flux;

/** Puerto de entrada: consultar las transferencias recientes procesadas (RF06). */
public interface ListRecentTransfersUseCase {

	Flux<Transfer> listRecent(int limit);
}
