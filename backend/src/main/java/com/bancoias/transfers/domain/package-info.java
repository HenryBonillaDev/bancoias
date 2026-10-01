/**
 * Capa de dominio: entidades y reglas de negocio puras del core de transferencias
 * (Transfer, Account, políticas de límite diario).
 *
 * Regla de dependencia: este paquete no debe depender de Spring, R2DBC, ni de los
 * paquetes {@code application}, {@code infrastructure} o {@code api}. Ver ADR-001
 * en docs/ADR.md.
 */
package com.bancoias.transfers.domain;
