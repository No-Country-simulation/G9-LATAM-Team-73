# G9-LATAM-Team-73

<div align="center">
<h1>✨✨✨ TechMind ✨✨✨</h1>
<h2>✨✨✨ Organización Inteligente del Conocimiento Técnico ✨✨✨</h2>
</div>

![Badge en Desarrollo](https://img.shields.io/badge/ENTREGA-%2008/2026-pink)

---

## 👥 Integrantes

| Nombre | Rol |
|--------|-----|
| Jocelyn Gudiño | Project Manager |
| Miguel Venegas | Data Scientist |
| Jonathan Gutiérrez | Data Analyst |
| Manuel Jaliffe | Backend Developer |
| Camilo González | Backend Developer |
| Javier Lujan | Backend Developer |

---

## 📌 Información del Proyecto

Profesionales y estudiantes de tecnología consumen diariamente una gran cantidad de contenido técnico, lo que dificulta organizar, localizar y reutilizar esta información posteriormente.

TechMind es una solución que permite la **organización inteligente de contenido técnico**, facilitando su clasificación, consulta y reutilización. Recibe textos técnicos (descripciones de artículos, documentación, anotaciones de estudio, tutoriales, etc.) y utiliza modelos de Machine Learning para:

- 🏷️ **Clasificar** el contenido en categorías técnicas (Backend, Frontend, Full Stack, Data Science, DevOps, Mobile)
- 📊 **Estimar la probabilidad** de la clasificación
- 🔑 **Extraer palabras clave** relevantes del texto
- 🌐 **Detectar el lenguaje** de programación o tecnología mencionada

---

## 🏗️ Arquitectura del Proyecto

```
+---------------------------+
|       Data Science        |
|  EDA → Limpieza → Modelo  |
+-------------+-------------+
              |
              v
+---------------------------+
|      Modelos ONNX         |
|  modelo_categoria.onnx    |
|  modelo_lenguaje.onnx     |
+-------------+-------------+
              |
              v
+---------------------------+
|      Backend Java         |
|  Spring Boot + ONNX RT    |
|  Puerto 8080              |
+-------------+-------------+
              |
              v
+---------------------------+
|      Frontend Astro       |
|  (TypeScript + Astro)     |
|  Puerto 4321              |
+-------------+-------------+
              |
              v
+---------------------------+
|         Usuario           |
+---------------------------+
```

---

## 🛠️ Herramientas Utilizadas

- **Lenguajes**: Python, Java, TypeScript
- **Backend**: Spring Boot 3.3 (Java 21)
- **Frontend**: Astro 7, TypeScript
- **Machine Learning**: Scikit-Learn, TF-IDF, modelos exportados a ONNX
- **Base de datos**: PostgreSQL 15
- **Contenedores**: Docker
- **Librerías ML**: Pandas, Scikit-Learn, ONNX Runtime
- **Herramientas**: Google Colab, Railway Cloud, Docker, Postman, Trello

---

## 📁 Estructura de Carpetas

```
G9-LATAM-Team-73/
│
├── data/
│   ├── raw/               # Dataset original (CSV)
│   ├── processed/         # Dataset limpio
│   └── eda/               # Imágenes y reportes del EDA
│
├── notebook/
│   └── *.ipynb            # Notebooks de exploración, limpieza y entrenamiento
│
├── model/
│   ├── modelo_categoria.onnx   # Modelo de clasificación por categoría
│   ├── mapa_categoria.json     # Mapeo de labels del modelo de categoría
│   ├── modelo_lenguaje.onnx    # Modelo de clasificación por lenguaje/tecnología
│   └── mapa_lenguaje.json      # Mapeo de labels del modelo de lenguaje
│
├── backend/
│   ├── src/               # Código fuente Spring Boot
│   ├── docker-compose.yml # Configuración Docker del backend
│   ├── Dockerfile         # Imagen Docker del backend
│   └── pom.xml            # Dependencias Maven
│
├── ui/
│   └── frontend/          # Aplicación Astro (TypeScript)
│       ├── src/           # Componentes y páginas
│       ├── public/        # Recursos estáticos
│       └── package.json   # Dependencias npm
│
├── railway/
│   └── deployment.md      # Guía de despliegue Railway Cloud 
│
└── README.md
```

---

## ⚙️ Requisitos Previos

Antes de correr el proyecto, asegúrate de tener instalado:

- [Java 21](https://www.oracle.com/java/technologies/downloads/#java21)
- [Maven 3.9+](https://maven.apache.org/download.cgi)
- [Node.js 18+](https://nodejs.org/)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)

---

## 🚀 Ejecución del Proyecto (Local)

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-org/G9-LATAM-Team-73.git
cd G9-LATAM-Team-73
```

### 2. Levantar la base de datos PostgreSQL

```bash
docker run -d \
  --name techmind-postgres \
  -e POSTGRES_DB=techmind \
  -e POSTGRES_USER=techmind \
  -e POSTGRES_PASSWORD=techmind \
  -p 5432:5432 \
  postgres:15
```

### 3. Levantar el Backend

```bash
cd backend
mvn clean package -DskipTests
mvn spring-boot:run
```

El backend quedará disponible en: `http://localhost:8080`

### 4. Levantar el Frontend

Abre una nueva terminal:

```bash
cd ui/frontend
echo PUBLIC_API_URL=http://localhost:8080 > .env
npm install
npm run dev
```

El frontend quedará disponible en: `http://localhost:4321`

> **Nota Windows:** Si npm no ejecuta por políticas de PowerShell, corre primero:
> ```powershell
> Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned
> ```

---

## 📡 Endpoints del API

### `POST /api/classify`

Clasifica un texto técnico.

**Request:**
```json
{
  "title": "Configuración de Spring Boot con PostgreSQL",
  "text": "En este tutorial aprenderemos a conectar Spring Boot con una base de datos PostgreSQL usando JPA e Hibernate."
}
```

**Response:**
```json
{
  "category": "Backend",
  "probability": 0.92,
  "tags": ["spring", "java", "postgresql", "jpa"],
  "source": "onnx"
}
```

---

## 💡 Ejemplos de Uso

| Título | Descripción | Categoría esperada |
|--------|-------------|-------------------|
| React Hooks Tutorial | Uso de useState y useEffect en React | Frontend |
| Docker y Kubernetes | Despliegue de contenedores con CI/CD | DevOps |
| Flutter para Android | Desarrollo de apps móviles con Dart | Mobile |
| Modelo de clasificación con Scikit-Learn | Entrenamiento con TF-IDF | Data Science |

---

## 🌐 Despliegue en OCI / Railway

El proyecto está preparado para desplegarse en la nube. Ver [`railway/deployment.md`](railway/deployment.md) para instrucciones detalladas.

Variables de entorno requeridas en producción:

| Variable | Descripción |
|----------|-------------|
| `DB_URL` | URL JDBC de PostgreSQL |
| `DB_USERNAME` | Usuario de la base de datos |
| `DB_PASSWORD` | Contraseña de la base de datos |
| `TECHMIND_ML_FALLBACK_ENABLED` | Activar clasificador por palabras clave si no hay modelo |
| `TECHMIND_CORS_ALLOWED_ORIGINS` | Origen permitido del frontend |

---

## 📄 Licencia

Este proyecto fue desarrollado como parte del **Hackathon ONE - LATAM 2026**.  
Todos los derechos reservados al Equipo 73.

