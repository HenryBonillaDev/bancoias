package com.bancoias.transfers.infrastructure.persistence.adapter;

import com.bancoias.transfers.application.port.out.TransferRepositoryPort;
import com.bancoias.transfers.domain.model.Transfer;
import com.bancoias.transfers.infrastructure.persistence.TransferEntityMapper;
import com.bancoias.transfers.infrastructure.persistence.TransferR2dbcRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador R2DBC del puerto {@link TransferRepositoryPort} (ver ADR-001,
 * ADR-004).
 *
 * <p>RF05 (idempotencia): {@code request_reference} tiene un constraint
 * {@code UNIQUE} en {@code schema.sql}. Si dos solicitudes con la misma
 * referencia llegan casi al mismo tiempo, la base de datos solo permite que
 * una se inserte; la otra recibe una violación de integridad que esta clase
 * traduce en "leer y devolver la transferencia ya persistida" en lugar de
 * fallar o duplicar.
 */
@Component
public class TransferPersistenceAdapter implements TransferRepositoryPort {

	private final TransferR2dbcRepository repository;

	public TransferPersistenceAdapter(TransferR2dbcRepository repository) {
		this.repository = repository;
	}

	@Override
	public Mono<Transfer> save(Transfer transfer) {
		return repository.save(TransferEntityMapper.toNewEntity(transfer))
				.map(TransferEntityMapper::toDomain)
				.onErrorResume(DataIntegrityViolationException.class,
						ex -> findByRequestReference(transfer.requestReference()));
	}

	@Override
	public Mono<Transfer> findByRequestReference(String requestReference) {
		return repository.findByRequestReference(requestReference)
				.map(TransferEntityMapper::toDomain);
	}

	@Override
	public Flux<Transfer> findRecent(int limit) {
		return repository.findAllByOrderByProcessedAtDesc(PageRequest.of(0, limit))
				.map(TransferEntityMapper::toDomain);
	}

	@Override
	public Mono<BigDecimal> sumAuthorizedAmount(String sourceAccountId, LocalDate day) {
		var startOfDay = day.atStartOfDay(ZoneOffset.UTC).toInstant();
		var startOfNextDay = day.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
		return repository.sumAuthorizedAmount(sourceAccountId, startOfDay, startOfNextDay);
	}
}
