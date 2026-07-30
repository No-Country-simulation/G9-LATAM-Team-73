# backend

Proyecto Spring Boot con la infraestructura, la base de datos y el empaquetado en Docker del backend de TechMind (G9-LATAM-Team-73).

## Stack

- Java 21
- Spring Boot 3.3.4 (`spring-boot-starter-data-jpa`, `spring-boot-starter-web`, `spring-boot-starter-validation`)
- PostgreSQL (driver `org.postgresql:postgresql`)
- Lombok

## Estructura del proyecto

```
src/main/java/com/techmind/backend/
├── BackendApplication.java   # punto de entrada de Spring Boot
├── model/
│   └── ContentEntity.java    # entidad JPA mapeada a la tabla "content"
└── repository/
    └── ContentRepository.java # consultas por categoría e histórico
```

### Qué hace cada pieza

- **`ContentEntity`**: entidad JPA. Campos: `id`, `title`, `originalText`, `translatedText`, `category`, `probability`, `sourceLanguage`, `processedAt`.
- **`ContentRepository`**: extiende `JpaRepository<ContentEntity, Long>`. Agrega `findByCategory`, `findAllByOrderByProcessedAtDesc` y `findByProcessedAtBetweenOrderByProcessedAtDesc` para las consultas por categoría e histórico.

## Base de datos

PostgreSQL

## Configuración

- **`application.properties`**: perfil por defecto, apunta a `jdbc:postgresql://localhost:5432/techmind` (para correr contra un Postgres local).
- **`application-docker.properties`**: perfil `docker`, lee la conexión de las variables de entorno `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (las inyecta `docker-compose.yml`). El mismo esquema de variables sirve como base para el despliegue en OCI Compute.
- **`.env.example`**: variables que usa `docker-compose.yml` (`POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `SERVER_PORT`). Copiarlo a `.env` y ajustar valores antes de levantar los contenedores.

## Cómo correr el proyecto

Local (con un Postgres propio corriendo en `localhost:5432`):

```bash
./mvnw spring-boot:run
```

Con Docker (levanta Postgres y la API juntos):

```bash
docker compose up --build
```

La API queda en `http://localhost:8080`.

## Docker

- **`Dockerfile`**: build multi-stage. Primera etapa compila con Maven sobre `eclipse-temurin-21`; segunda etapa corre el jar sobre `eclipse-temurin:21-jre-alpine` (imagen final liviana).
- **`docker-compose.yml`**: dos servicios.
  - `db`: Postgres 16, con volumen nombrado `techmind_pgdata` para persistir los datos entre reinicios.
  - `api`: construye la imagen del `Dockerfile`, espera a que `db` esté saludable (`healthcheck`) y se conecta con el perfil `docker`.