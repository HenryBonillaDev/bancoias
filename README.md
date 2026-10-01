# BancoIAS — Core de Transferencias

Prueba técnica Full Stack (Java Spring Boot WebFlux + Angular). Ver documentación completa en
[docs/README.md](./docs/README.md), incluyendo requisitos, arquitectura, decisiones técnicas
(ADR) y uso de IA.

## Estructura

```
bancoias/
├── backend/   Java 21 + Spring Boot WebFlux + Gradle (Kotlin DSL)
├── frontend/  Angular (standalone components)
└── docs/      Documentación del proyecto
```

## Ejecución rápida

- Backend: `cd backend && ./gradlew bootRun` (arranca en `http://localhost:8080`)
  - Swagger UI: `http://localhost:8080/swagger-ui.html`
  - OpenAPI JSON: `http://localhost:8080/v3/api-docs`
  - Tests: `cd backend && ./gradlew test`
- Frontend: `cd frontend && npm install && npm start`
