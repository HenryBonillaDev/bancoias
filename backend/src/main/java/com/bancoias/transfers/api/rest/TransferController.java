package com.bancoias.transfers.api.rest;

import com.bancoias.transfers.api.dto.TransferDtoMapper;
import com.bancoias.transfers.api.dto.TransferRequestDto;
import com.bancoias.transfers.api.dto.TransferResponseDto;
import com.bancoias.transfers.application.port.in.GetTransferUseCase;
import com.bancoias.transfers.application.port.in.ListRecentTransfersUseCase;
import com.bancoias.transfers.application.port.in.ProcessTransferUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

	private static final int DEFAULT_RECENT_LIMIT = 20;

	private final ProcessTransferUseCase processTransferUseCase;
	private final GetTransferUseCase getTransferUseCase;
	private final ListRecentTransfersUseCase listRecentTransfersUseCase;

	public TransferController(
			ProcessTransferUseCase processTransferUseCase,
			GetTransferUseCase getTransferUseCase,
			ListRecentTransfersUseCase listRecentTransfersUseCase) {
		this.processTransferUseCase = processTransferUseCase;
		this.getTransferUseCase = getTransferUseCase;
		this.listRecentTransfersUseCase = listRecentTransfersUseCase;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.OK)
	public Mono<TransferResponseDto> process(@Valid @RequestBody TransferRequestDto request) {
		return processTransferUseCase.process(TransferDtoMapper.toCommand(request))
				.map(TransferDtoMapper::toResponse);
	}

	@GetMapping("/{requestReference}")
	public Mono<ResponseEntity<TransferResponseDto>> getByRequestReference(@PathVariable String requestReference) {
		return getTransferUseCase.getByRequestReference(requestReference.trim())
				.map(TransferDtoMapper::toResponse)
				.map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping
	public Flux<TransferResponseDto> listRecent(
			@RequestParam(name = "limit", defaultValue = "" + DEFAULT_RECENT_LIMIT) int limit) {
		return listRecentTransfersUseCase.listRecent(limit)
				.map(TransferDtoMapper::toResponse);
	}
}
