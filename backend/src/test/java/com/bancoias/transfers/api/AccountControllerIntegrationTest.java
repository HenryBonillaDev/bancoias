package com.bancoias.transfers.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.bancoias.transfers.api.dto.AccountDailyUsageResponseDto;
import com.bancoias.transfers.api.dto.TransferRequestDto;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Pruebas del endpoint de solo lectura {@code GET /api/accounts/{id}/daily-usage},
 * agregado para poder verificar visualmente el consumo del límite diario (RF02)
 * mientras se prueban transferencias (p. ej. desde Swagger UI).
 *
 * <p>Comparan deltas (antes/después) en vez de valores absolutos porque la
 * suite completa comparte el mismo contexto de Spring (y por tanto la misma
 * base H2 en memoria) entre clases de prueba.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AccountControllerIntegrationTest {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void reflectsAnAuthorizedTransferInTheSourceAccountsDailyUsage() {
		AccountDailyUsageResponseDto before = getDailyUsage("CTA-1002");

		webTestClient.post().uri("/api/transfers")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(new TransferRequestDto("ACC-IT-REF-001", "CTA-1002", "CTA-2001", BigDecimal.valueOf(250_000)))
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.status").isEqualTo("AUTHORIZED");

		AccountDailyUsageResponseDto after = getDailyUsage("CTA-1002");

		assertThat(after.consumedToday()).isEqualByComparingTo(before.consumedToday().add(BigDecimal.valueOf(250_000)));
		assertThat(after.dailyLimit()).isEqualByComparingTo(before.dailyLimit());
		assertThat(after.remaining()).isEqualByComparingTo(after.dailyLimit().subtract(after.consumedToday()));
	}

	@Test
	void doesNotCountARejectedTransferTowardsTheDailyUsage() {
		AccountDailyUsageResponseDto before = getDailyUsage("CTA-1001");

		webTestClient.post().uri("/api/transfers")
				.contentType(MediaType.APPLICATION_JSON)
				// cuenta origen == destino -> rechazada
				.bodyValue(new TransferRequestDto("ACC-IT-REF-002", "CTA-1001", "CTA-1001", BigDecimal.valueOf(999_999)))
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.status").isEqualTo("REJECTED");

		AccountDailyUsageResponseDto after = getDailyUsage("CTA-1001");

		assertThat(after.consumedToday()).isEqualByComparingTo(before.consumedToday());
	}

	@Test
	void returns404ForAnUnknownAccount() {
		webTestClient.get().uri("/api/accounts/CTA-DOES-NOT-EXIST/daily-usage")
				.exchange()
				.expectStatus().isNotFound();
	}

	private AccountDailyUsageResponseDto getDailyUsage(String accountId) {
		return webTestClient.get().uri("/api/accounts/{accountId}/daily-usage", accountId)
				.exchange()
				.expectStatus().isOk()
				.expectBody(AccountDailyUsageResponseDto.class)
				.returnResult()
				.getResponseBody();
	}
}
