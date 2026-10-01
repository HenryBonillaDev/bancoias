package com.bancoias.transfers.infrastructure.persistence;

import com.bancoias.transfers.application.port.out.TransactionalExecutionPort;
import io.r2dbc.spi.R2dbcException;
import java.time.Duration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Implementación de {@link TransactionalExecutionPort} (RF04) con aislamiento
 * {@code SERIALIZABLE} + reintento acotado ante conflictos de serialización
 * (SQLState {@code 40001}, estándar SQL para esta condición).
 */
@Component
public class TransactionalExecutionAdapter implements TransactionalExecutionPort {

	private static final String SERIALIZATION_FAILURE_SQLSTATE = "40001";
	private static final int MAX_RETRIES = 30;

	private final TransactionalOperator serializableTransactionalOperator;

	public TransactionalExecutionAdapter(ReactiveTransactionManager transactionManager) {
		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		definition.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
		this.serializableTransactionalOperator = TransactionalOperator.create(transactionManager, definition);
	}

	@Override
	public <T> Mono<T> executeSerializable(Mono<T> action) {
		return serializableTransactionalOperator.transactional(action)
				.retryWhen(Retry.backoff(MAX_RETRIES, Duration.ofMillis(10))
						.maxBackoff(Duration.ofMillis(200))
						.filter(TransactionalExecutionAdapter::isSerializationConflict));
	}

	private static boolean isSerializationConflict(Throwable throwable) {
		for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
			if (cause instanceof R2dbcException r2dbcException
					&& SERIALIZATION_FAILURE_SQLSTATE.equals(r2dbcException.getSqlState())) {
				return true;
			}
		}
		return false;
	}
}
