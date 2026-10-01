package com.bancoias.transfers.api.exception;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

/**
 * Traduce errores de validación estructural (campos faltantes/mal formados
 * del request) a 400. Los rechazos de negocio (RF02) no pasan por aquí: se
 * devuelven como 200 con {@code status = REJECTED}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(WebExchangeBindException.class)
	public ResponseEntity<ApiErrorResponse> handleValidationError(WebExchangeBindException ex) {
		List<String> details = ex.getFieldErrors().stream()
				.map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
				.toList();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ApiErrorResponse("Solicitud inválida", details));
	}
}
