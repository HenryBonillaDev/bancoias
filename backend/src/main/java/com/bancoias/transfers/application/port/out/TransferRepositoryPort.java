package com.bancoias.transfers.application.port.out;

import com.bancoias.transfers.domain.model.Transfer;
import java.math.BigDecimal;
import java.time.LocalDate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida hacia la persistencia de transferencias. Implementado por
 * {@code infrastructure.persistence.adapter} (R2DBC + H2, ver ADR-004).
 */
public interface TransferRepositoryPort {

	/**
	 * Persiste una transferencia nueva. Si ya existe una fila con el mismo
	 * {@code requestReference} (constraint único, RF05), el adaptador debe
	 * devolver la transferencia ya persistida en lugar de fallar o duplicar.
	 */
	Mono<Transfer> save(Transfer transfer);

	Mono<Transfer> findByRequestReference(String requestReference);

	Flux<Transfer> findRecent(int limit);

	/**
	 * Suma el valor de las transferencias AUTORIZADAS de la cuenta origen
	 * durante el día indicado (RF02). Usada para validar el límite diario.
	 */
	Mono<BigDecimal> sumAuthorizedAmount(String sourceAccountId, LocalDate day);
}
