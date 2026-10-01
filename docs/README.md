# BancoIAS — Core de Transferencias

Solución Full Stack (Java Spring Boot WebFlux + Angular) para procesar, persistir y consultar
transferencias bancarias, desarrollada como prueba técnica.

## Mapa de documentación

| Documento | Contenido |
|-----------|-----------|
| [REQUIREMENTS.md](./REQUIREMENTS.md) | Requisitos funcionales (RF01-RF07), datos iniciales y criterios de completitud, tomados del enunciado. |
| [ARCHITECTURE.md](./ARCHITECTURE.md) | Diagrama de arquitectura (Clean Architecture) y explicación de capas. |
| [ADR.md](./ADR.md) | Decisiones de arquitectura formales (ADR), con contexto, alternativas, trade-offs y validación. |
| [DECISIONS.md](./DECISIONS.md) | Bitácora de decisiones de negocio puntuales y trade-offs menores durante la implementación. |
| [API.md](./API.md) | Endpoints del backend documentados. |

## Estado del proyecto

En construcción — ver tabla de estado de requisitos en
[REQUIREMENTS.md §12](./REQUIREMENTS.md#12-estado-de-cobertura-de-requisitos-se-irá-actualizando).

## Estructura del repositorio

```
bancoias/
├── backend/     Java 21 + Spring Boot WebFlux + Gradle (Kotlin DSL), Clean Architecture
├── frontend/    Angular (standalone components)
└── docs/        Este directorio
```

## Uso de inteligencia artificial

Se utilizó un asistente de IA (Claude Code) como apoyo durante el desarrollo. El registro
detallado de en qué actividades se usó, qué se aprovechó, cómo se validó y qué consideraciones de
seguridad de la información se aplicaron se mantiene en **[AI_USAGE.md](./AI_USAGE.md)**.
