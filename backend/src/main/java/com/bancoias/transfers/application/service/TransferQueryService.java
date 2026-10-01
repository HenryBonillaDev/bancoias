package com.bancoias.transfers.application.service;

import com.bancoias.transfers.application.port.in.GetTransferUseCase;
import com.bancoias.transfers.application.port.in.ListRecentTransfersUseCase;
import com.bancoias.transfers.application.port.out.TransferRepositoryPort;
import com.bancoias.transfers.domain.model.Transfer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Caso de uso de RF06: consultar transferencias ya procesadas. */
public class TransferQueryService implements GetTransferUseCase, ListRecentTransfersUseCase {

	private final TransferRepositoryPort transferRepository;

	public TransferQueryService(TransferRepositoryPort transferRepository) {
		this.transferRepository = transferRepository;
	}

	@Override
	public Mono<Transfer> getByRequestReference(String requestReference) {
		return transferRepository.findByRequestReference(requestReference);
	}

	@Override
	public Flux<Transfer> listRecent(int limit) {
		return transferRepository.findRecent(limit);
	}
}
