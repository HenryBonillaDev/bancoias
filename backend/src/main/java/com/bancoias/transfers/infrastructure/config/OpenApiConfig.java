package com.bancoias.transfers.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Expone Swagger UI en /swagger-ui.html para probar la API manualmente (RF01/RF02/RF06). */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI transfersCoreOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("BancoIAS — Core de Transferencias")
						.description("API para procesar, persistir y consultar transferencias (RF01-RF06).")
						.version("v0.1.0"));
	}
}
