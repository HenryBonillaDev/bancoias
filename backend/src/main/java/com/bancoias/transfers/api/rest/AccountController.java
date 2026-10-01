package com.bancoias.transfers.api.rest;

import com.bancoias.transfers.api.dto.AccountDailyUsageResponseDto;
import com.bancoias.transfers.api.dto.AccountDtoMapper;
import com.bancoias.transfers.application.port.in.GetAccountDailyUsageUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Endpoint de solo lectura, no exigido por el enunciado (que explícitamente
 * no requiere un módulo de administración de cuentas), agregado para poder
 * verificar visualmente el consumo del límite diario (RF02) mientras se
 * prueban transferencias desde Swagger UI.
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

	private final GetAccountDailyUsageUseCase getAccountDailyUsageUseCase;

	public AccountController(GetAccountDailyUsageUseCase getAccountDailyUsageUseCase) {
		this.getAccountDailyUsageUseCase = getAccountDailyUsageUseCase;
	}

	@GetMapping("/{accountId}/daily-usage")
	public Mono<ResponseEntity<AccountDailyUsageResponseDto>> getDailyUsage(@PathVariable String accountId) {
		return getAccountDailyUsageUseCase.getDailyUsage(accountId.trim())
				.map(AccountDtoMapper::toResponse)
				.map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}
}
