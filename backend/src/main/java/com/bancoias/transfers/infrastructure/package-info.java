/**
 * Capa de infraestructura: adaptadores que implementan los puertos de salida
 * definidos en {@code application.port.out} (persistencia R2DBC/H2, configuración,
 * datos semilla de cuentas).
 *
 * Regla de dependencia: depende de {@code application} y {@code domain}; nunca al
 * revés. Ver ADR-001 y ADR-004 en docs/ADR.md.
 */
package com.bancoias.transfers.infrastructure;
