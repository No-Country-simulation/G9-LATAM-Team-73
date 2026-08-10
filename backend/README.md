# backend

Proyecto Spring Boot con la infraestructura, la base de datos, el empaquetado en Docker y la capa de IA/ONNX del backend de TechMind (G9-LATAM-Team-73).

## Stack

- Java 21
- Spring Boot 3.3.4 (`spring-boot-starter-data-jpa`, `spring-boot-starter-web`, `spring-boot-starter-validation`)
- PostgreSQL (driver `org.postgresql:postgresql`)
- ONNX Runtime (`com.microsoft.onnxruntime:onnxruntime`)
- OCI Language SDK (`oci-java-sdk-ailanguage`) para traducción EN → ES
- Lombok

## Estructura del proyecto

```
src/main/java/com/techmind/backend/
├── BackendApplication.java
├── config/
│   ├── MlConfig.java
│   ├── MlProperties.java
│   └── OciLanguageProperties.java
├── model/
│   └── ContentEntity.java
├── repository/
│   └── ContentRepository.java
├── ml/                          # Dev 2 – IA / ONNX
│   ├── TextPreprocessor.java
│   ├── KeywordExtractor.java
│   ├── OnnxModelService.java
│   ├── PredictionResult.java
│   └── ContentAnalysisService.java
└── translation/                 # Dev 2 – EN → ES
    ├── TranslationService.java
    ├── TranslationResult.java
    ├── LanguageDetector.java
    ├── LocalTranslationService.java
    └── OciLanguageTranslationService.java
```

### Qué hace cada pieza

- **`ContentEntity`**: entidad JPA. Campos: `id`, `title`, `originalText`, `translatedText`, `category`, `probability`, `sourceLanguage`, `processedAt`.
- **`ContentRepository`**: consultas por categoría e histórico.
- **`TextPreprocessor`**: limpia texto igual que el notebook (`limpiar_texto`): minúsculas, sin puntuación/números, espacios colapsados.
- **`OnnxModelService`**: carga `model/techmind_classifier.onnx` y predice categoría + probabilidad; si el `.onnx` aún no está, usa clasificador heurístico (`techmind.ml.fallback-enabled=true`).
- **`KeywordExtractor`**: genera tags técnicos (`informacion_adicional`).
- **`ContentAnalysisService`**: orquesta traducción + inferencia (listo para que Dev 3 lo conecte al controller).
- **`TranslationService`**: detecta idioma; con OCI habilitado traduce EN → ES; en local deja el texto original.

## Modelo ONNX

Colocar el archivo exportado por Ciencia de Datos en:

```
src/main/resources/model/techmind_classifier.onnx
```

Expectativa del notebook: `Pipeline(TfidfVectorizer + LogisticRegression)` convertido con `skl2onnx` (`StringTensorType`), de modo que Java envíe el texto limpio directamente.

Metadatos de clases: `src/main/resources/model/labels.json`.

## Base de datos

PostgreSQL

## Configuración

- **`application.properties`**: perfil por defecto + flags ML/OCI.
- **`application-docker.properties`**: perfil `docker`, lee `DB_*` y `TECHMIND_*`.
- **`.env.example`**: variables para `docker-compose.yml`.

### Traducción OCI Language

```properties
techmind.oci.language.enabled=true
techmind.oci.language.compartment-id=ocid1.compartment.oc1..xxx
techmind.oci.language.config-file=~/.oci/config
techmind.oci.language.profile=DEFAULT
```

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

## Pruebas (Dev 2)

```bash
./mvnw test
```

Cubre preprocesamiento, extracción de tags, inferencia (fallback), detección de idioma y el orquestador con mocks.

## Docker

- **`Dockerfile`**: build multi-stage. Primera etapa compila con Maven sobre `eclipse-temurin-21`; segunda etapa corre el jar sobre `eclipse-temurin:21-jre (migrado desde alpine por incompatibilidad con librerias nativas de ONNX Runtime)`.
- **`docker-compose.yml`**: servicios `db` (Postgres 16) y `api` (perfil `docker`).

## API REST (Dev 3)

### `POST /contenido`

Recibe un contenido tecnico (titulo + texto), lo analiza (traduccion + clasificacion ONNX via `ContentAnalysisService`) y devuelve la categoria, probabilidad y tags detectados. Tambien guarda el resultado en la base de datos.

**Request:**

```json
{
  "titulo": "Introduccion a Spring Boot",
  "texto": "En este contenido se presentan los conceptos basicos para crear APIs REST utilizando Java y Spring Boot con arquitectura limpia."
}
```

- `titulo`: obligatorio, maximo 255 caracteres.
- `texto`: obligatorio, entre 10 y 5000 caracteres.

**Response (200 OK):**

```json
{
  "categoria": "Backend",
  "probabilidad": 0.86,
  "informacionAdicional": ["Spring Boot", "Java", "API REST"]
}
```

**Ejemplo con curl:**

```bash
curl -X POST http://localhost:8080/contenido \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Introduccion a Spring Boot","texto":"En este contenido se presentan los conceptos basicos para crear APIs REST utilizando Java y Spring Boot con arquitectura limpia."}'
```

### `GET /contenido/health`

Verifica que la API esta activa. Responde `OK` (200).

### Manejo de errores

Todos los errores devuelven el mismo formato:

```json
{
  "timestamp": "2026-08-07T04:20:00Z",
  "status": 400,
  "error": "Bad Request",
  "mensaje": "Errores de validacion en la solicitud",
  "detalles": ["El campo 'titulo' es obligatorio y no puede estar vacio"]
}
```

| Situacion | Codigo |
|---|---|
| Campos faltantes o invalidos (titulo/texto) | 400 |
| JSON mal formado | 400 |
| Fallo en el analisis (traduccion/modelo ONNX) | 500 |
| Error inesperado | 500 |

### Arquitectura interna

`ContenidoController` depende de la interfaz `ContenidoProcessingService`, no de una implementacion concreta:

- `MockContenidoService`: implementacion simulada usada en Fase 1 para probar el Controller sin depender del modelo ONNX ni de la base de datos.
- `ContenidoService` (`@Primary`, activa por defecto): implementacion real. Llama a `ContentAnalysisService` (Dev 2) para traducir y clasificar, y guarda el historial con `ContentRepository` (Dev 1). Si falla el guardado en base de datos, la respuesta al usuario no se ve afectada (solo se pierde ese registro del historial, queda en logs).
