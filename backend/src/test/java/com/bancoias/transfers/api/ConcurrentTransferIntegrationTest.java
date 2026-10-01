package com.bancoias.transfers.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.bancoias.transfers.api.dto.AccountDailyUsageResponseDto;
import com.bancoias.transfers.api.dto.TransferRequestDto;
import com.bancoias.transfers.api.dto.TransferResponseDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/**
 * RF04: valida que el límite diario se preserva bajo solicitudes
 * verdaderamente concurrentes sobre la misma cuenta origen.
 *
 * <p>Dispara 2 solicitudes simultáneas cuyo monto es, cada una, un poco más
 * de la mitad de lo que le queda disponible a la cuenta en este momento —
 * así, sin importar cuánto haya consumido otra prueba antes, nunca pueden
 * autorizarse las dos a la vez. Si la protección de RF04 fallara (lectura
 * del acumulado no serializada), ambas verían el mismo "espacio libre" y
 * ambas se autorizarían.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ConcurrentTransferIntegrationTest {

	private static final String SOURCE_ACCOUNT = "CTA-1002";
	private static final String DESTINATION_ACCOUNT = "CTA-2001";

	@LocalServerPort
	private int port;

	@Test
	void preservesTheDailyLimitUnderTwoConcurrentRequestsForTheSameSourceAccount() {
		WebClient webClient = WebClient.create("http://localhost:" + port);

		AccountDailyUsageResponseDto before = getDailyUsage(webClient, SOURCE_ACCOUNT);
		BigDecimal remaining = before.dailyLimit().subtract(before.consumedToday());
		BigDecimal amount = remaining.divide(BigDecimal.valueOf(2), 0, RoundingMode.FLOOR).add(BigDecimal.ONE);

		List<TransferResponseDto> results = Flux.just(
						"CONC-REF-" + UUID.randomUUID(), "CONC-REF-" + UUID.randomUUID())
				.flatMap(ref -> postTransfer(webClient, ref, amount), 2)
				.collectList()
				.block();

		assertThat(results).hasSize(2);
		long authorizedCount = results.stream().filter(r -> "AUTHORIZED".equals(r.status())).count();
		// Las dos juntas (2 * amount > remaining) nunca caben: a lo sumo una se autoriza.
		assertThat(authorizedCount).isLessThanOrEqualTo(1);

		AccountDailyUsageResponseDto after = getDailyUsage(webClient, SOURCE_ACCOUNT);
		assertThat(after.consumedToday()).isLessThanOrEqualTo(after.dailyLimit());
		assertThat(after.consumedToday()).isEqualByComparingTo(before.consumedToday().add(amount.multiply(BigDecimal.valueOf(authorizedCount))));
	}

	private Flux<TransferResponseDto> postTransfer(WebClient webClient, String requestReference, BigDecimal amount) {
		TransferRequestDto request = new TransferRequestDto(requestReference, SOURCE_ACCOUNT, DESTINATION_ACCOUNT, amount);
		return webClient.post().uri("/api/transfers")
				.bodyValue(request)
				.retrieve()
				.bodyToMono(TransferResponseDto.class)
				.flux();
	}

	private AccountDailyUsageResponseDto getDailyUsage(WebClient webClient, String accountId) {
		return webClient.get().uri("/api/accounts/{accountId}/daily-usage", accountId)
				.retrieve()
				.bodyToMono(AccountDailyUsageResponseDto.class)
				.block();
	}
}
