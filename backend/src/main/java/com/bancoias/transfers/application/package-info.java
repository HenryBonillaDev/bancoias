/**
 * Capa de aplicación: casos de uso que orquestan el dominio (p. ej. procesar y
 * consultar transferencias) y puertos (interfaces) que {@code infrastructure}
 * debe implementar.
 *
 * Subpaquetes:
 * <ul>
 *   <li>{@code port.in} — puertos de entrada (casos de uso invocados por {@code api}).</li>
 *   <li>{@code port.out} — puertos de salida (dependencias externas, implementadas
 *       por {@code infrastructure}, p. ej. repositorio de transferencias).</li>
 *   <li>{@code service} — implementación de los casos de uso.</li>
 * </ul>
 *
 * Regla de dependencia: depende de {@code domain}, nunca de {@code infrastructure}
 * ni de {@code api}. Ver ADR-001 en docs/ADR.md.
 */
package com.bancoias.transfers.application;
