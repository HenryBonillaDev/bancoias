package com.bancoias.transfers.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Permite que el frontend Angular consuma la API (RF07).
 *
 * <p>Se listan explícitamente {@code localhost} y {@code 127.0.0.1}: el
 * navegador los trata como orígenes distintos para CORS aunque apunten al
 * mismo sitio, y "ng serve" puede arrancar en 4200 o, si ese puerto está
 * ocupado, en el siguiente libre (4201, 4202, ...).
 */
@Configuration
public class CorsConfig implements WebFluxConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
				.allowedMethods("GET", "POST")
				.allowedHeaders("*");
	}
}
