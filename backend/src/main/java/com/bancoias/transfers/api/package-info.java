/**
 * Capa de entrada HTTP (WebFlux): controllers, DTOs de request/response y manejo
 * de errores. Traduce HTTP hacia/desde los casos de uso de {@code application}.
 *
 * Regla de dependencia: depende de {@code application}; no accede directamente a
 * {@code infrastructure} ni a {@code domain} salvo tipos expuestos explícitamente
 * por los puertos de entrada. Ver ADR-001 en docs/ADR.md.
 */
package com.bancoias.transfers.api;
