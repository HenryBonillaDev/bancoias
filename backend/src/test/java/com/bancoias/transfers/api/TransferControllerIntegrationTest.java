package com.bancoias.transfers.api;

import com.bancoias.transfers.api.dto.TransferRequestDto;
import com.bancoias.transfers.api.dto.TransferResponseDto;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Pruebas de integración de extremo a extremo: HTTP -> casos de uso ->
 * persistencia real en H2 (R2DBC). Cubren RF01, RF02, RF03, RF05 y RF06.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class TransferControllerIntegrationTest {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void authorizesAndPersistsAValidTransfer_RF01_RF02_RF03() {
		TransferRequestDto request = new TransferRequestDto(
				"IT-REF-001", "CTA-1001", "CTA-2001", BigDecimal.valueOf(600_000));

		TransferResponseDto response = postTransfer(request)
				.expectStatus().isOk()
				.expectBody(TransferResponseDto.class)
				.returnResult()
				.getResponseBody();

		org.assertj.core.api.Assertions.assertThat(response).isNotNull();
		org.assertj.core.api.Assertions.assertThat(response.status()).isEqualTo("AUTHORIZED");
		org.assertj.core.api.Assertions.assertThat(response.reason()).isNull();
		org.assertj.core.api.Assertions.assertThat(response.processedAt()).isNotNull();

		webTestClient.get().uri("/api/transfers/IT-REF-001")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.status").isEqualTo("AUTHORIZED");
	}

	@Test
	void repeatingTheSameRequestReferenceReturnsTheOriginalResult_RF05() {
		TransferRequestDto request = new TransferRequestDto(
				"IT-REF-002", "CTA-1002", "CTA-2001", BigDecimal.valueOf(100_000));

		TransferResponseDto first = postTransfer(request)
				.expectStatus().isOk()
				.expectBody(TransferResponseDto.class)
				.returnResult()
				.getResponseBody();

		TransferRequestDto sameReferenceDifferentAmount = new TransferRequestDto(
				"IT-REF-002", "CTA-1002", "CTA-2001", BigDecimal.valueOf(999_999));

		TransferResponseDto second = postTransfer(sameReferenceDifferentAmount)
				.expectStatus().isOk()
				.expectBody(TransferResponseDto.class)
				.returnResult()
				.getResponseBody();

		org.assertj.core.api.Assertions.assertThat(second).isNotNull();
		org.assertj.core.api.Assertions.assertThat(second.amount()).isEqualByComparingTo(first.amount());
		org.assertj.core.api.Assertions.assertThat(second.processedAt()).isEqualTo(first.processedAt());
	}

	@Test
	void rejectsAnAmountOfZero_RF02() {
		TransferRequestDto request = new TransferRequestDto(
				"IT-REF-003", "CTA-1001", "CTA-2001", BigDecimal.ZERO);

		postTransfer(request)
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.status").isEqualTo("REJECTED")
				.jsonPath("$.reason").isEqualTo("El valor de la transferencia debe ser mayor que cero");
	}

	@Test
	void rejectsWhenTheOperationExceedsTheDailyLimit_RF02() {
		postTransfer(new TransferRequestDto("IT-REF-LIMIT-1", "CTA-2001", "CTA-1001", BigDecimal.valueOf(4_800_000)))
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.status").isEqualTo("AUTHORIZED");

		postTransfer(new TransferRequestDto("IT-REF-LIMIT-2", "CTA-2001", "CTA-1001", BigDecimal.valueOf(300_000)))
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.status").isEqualTo("REJECTED")
				.jsonPath("$.reason").isEqualTo("La operación supera el límite diario permitido de la cuenta origen");
	}

	@Test
	void trimsWhitespaceAroundTextFieldsSoReferencesAreFoundWithoutTheStraySpace() {
		TransferRequestDto request = new TransferRequestDto(
				" IT-REF-TRIM ", " CTA-1001 ", " CTA-2001 ", BigDecimal.valueOf(50_000));

		postTransfer(request)
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.requestReference").isEqualTo("IT-REF-TRIM")
				.jsonPath("$.sourceAccountId").isEqualTo("CTA-1001")
				.jsonPath("$.destinationAccountId").isEqualTo("CTA-2001")
				.jsonPath("$.status").isEqualTo("AUTHORIZED");

		// Buscarla sin el espacio debe encontrarla (antes del fix quedaba guardada como " IT-REF-TRIM ").
		webTestClient.get().uri("/api/transfers/IT-REF-TRIM")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.requestReference").isEqualTo("IT-REF-TRIM");

		// Repetirla con espacios distintos sigue siendo la misma referencia (RF05).
		postTransfer(new TransferRequestDto("IT-REF-TRIM", "CTA-1001", "CTA-2001", BigDecimal.valueOf(999_999)))
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.amount").isEqualTo(50_000);
	}

	@Test
	void returns400WhenRequiredFieldsAreMissing() {
		webTestClient.post().uri("/api/transfers")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("""
						{"requestReference":"","sourceAccountId":"CTA-1001","destinationAccountId":"CTA-2001","amount":1000}
						""")
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void returns404ForAnUnknownRequestReference() {
		webTestClient.get().uri("/api/transfers/DOES-NOT-EXIST")
				.exchange()
				.expectStatus().isNotFound();
	}

	private WebTestClient.ResponseSpec postTransfer(TransferRequestDto request) {
		return webTestClient.post().uri("/api/transfers")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(request)
				.exchange();
	}
}
