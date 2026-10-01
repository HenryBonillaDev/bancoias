package com.bancoias.transfers.infrastructure.persistence;

import com.bancoias.transfers.infrastructure.persistence.entity.TransferEntity;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransferR2dbcRepository extends ReactiveCrudRepository<TransferEntity, Long> {

	Mono<TransferEntity> findByRequestReference(String requestReference);

	Flux<TransferEntity> findAllByOrderByProcessedAtDesc(org.springframework.data.domain.Pageable pageable);

	@Query("""
			SELECT COALESCE(SUM(amount), 0) FROM transfers
			WHERE source_account_id = :sourceAccountId
			  AND status = 'AUTHORIZED'
			  AND processed_at >= :startOfDay
			  AND processed_at < :startOfNextDay
			""")
	Mono<BigDecimal> sumAuthorizedAmount(String sourceAccountId, Instant startOfDay, Instant startOfNextDay);
}
