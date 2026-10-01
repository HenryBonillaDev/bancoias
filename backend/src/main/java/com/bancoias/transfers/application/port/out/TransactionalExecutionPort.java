package com.bancoias.transfers.application.port.out;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida de RF04: ejecuta una acción dentro de una transacción con
 * aislamiento serializable, reintentando automáticamente si la base de datos
 * detecta un conflicto de serialización entre transacciones concurrentes.
 *
 * <p>Esto es lo que hace que la secuencia "leer acumulado del día + decidir +
 * guardar" (ver {@code ProcessTransferService}) sea segura bajo solicitudes
 * verdaderamente concurrentes sobre la misma cuenta origen (RF04): si dos
 * transacciones leen el mismo acumulado "antes" de que la otra confirme su
 * escritura, el motor de base de datos aborta una de las dos con un error de
 * serialización en vez de permitir que ambas se autoricen.
 */
public interface TransactionalExecutionPort {

	<T> Mono<T> executeSerializable(Mono<T> action);
}
