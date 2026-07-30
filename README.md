# G9-LATAM-Team-73

<div align="center">
<h1> ✨✨✨ TechMind ✨✨✨ </h1>
<h2> ✨✨✨ Organización Inteligente del Conocimiento Técnico ✨✨✨ </h2>
</div>

![Badge en Desarollo](https://img.shields.io/badge/ENTREGA-%2008/2026-pink)

<h3> Integrantes </h3>

*	Jocelyn Gudiño - Project Manager
*	Miguel Venegas - Data Scientist
*	Jonathan Gutiérrez - Data Analyst
*	Luiggi Juarez - Data Analyst
*	Manuel Jaliffe - Backend Developer
*	Camilo González - Backend Developer
*	Javier Lujan - Backend Developer

<h1></h1>

<h3> Información de Proyecto </h3>

Profesionales y estudiantes de tecnología consumen diariamente una gran cantidad de contenido técnico, lo que dificulta organizar, localizar y reutilizar esta información posteriormente.

Se requiere crear una solución que permita la organización inteligente de contenido técnico, facilitando su clasificación, consulta y reutilización.
La solución debe recibir textos técnicos (por ejemplo: descripciones de artículos, documentación, anotaciones de estudio, contenidos de cursos, tutoriales o materiales de referencia) y utilizar técnicas de Ciencia de Datos para identificar información relevante sobre ese contenido.

<h1></h1>

<h3> Arquitectura del proyecto </h3>

La arquitectura del proyecto sigue un flujo descendente:
Data Science prepara y entrena el modelo, el modelo se almacena en OCI Object Storage, la API REST lo consume para generar predicciones, y la UI permite al usuario interactuar con el sistema.

```bash
+---------------------------+
|       Data Science        |
|  EDA → Limpieza → Modelo  |
+-------------+-------------+
              |
              v
+---------------------------+
|      Modelo ONNX/Pkl      |
|   (generado por DS)       |
+-------------+-------------+
              |
              v
+---------------------------+
|      OCI Object Storage   |
|   (modelo.onnx almacenado)|
+-------------+-------------+
              |
              v
+---------------------------+
|         API REST          |
|      (FastAPI/Flask)      |
+-------------+-------------+
              |
              v
+---------------------------+
|        Interfaz UI        |
|  (HTML/JS o Streamlit)    |
+-------------+-------------+
              |
              v
+---------------------------+
|         Usuario           |
+---------------------------+
```
<h1></h1>

<h3> Herramientas utilizadas </h3>

* **Lenguajes**: Python, Java. 
* **Frameworks/Backend**: Spring Boot (Framework de Java).
* **Librerías**: Pandas, Scikit-Learn.
* **Técnicas de Machine Learning**: TF-IDF(Vectorización de texto).
* **Herramientas**: Excel, Google Colab, OCI, Docker, Postman, Trello.

<h1></h1>

<h3> Estructura de carpetas </h3>

La estructura permite una organización por capas independientes, lo que facilita un flujo claro del proyecto.

```bash

G9-LATAM-Team-73/
│
├── data/
│   ├── raw/               # Dataset original (CSV)
│   ├── processed/         # Limpieza de Dataset
│   └── eda/               # Imágenes y reportes del EDA
│
├── notebooks/
│   ├── 01_eda.ipynb       # Exploración de datos (nombre por actualizar)
│   ├── 02_cleaning.ipynb  # Limpieza y preparación (nombre por actualizar)
│   ├── 03_training.ipynb  # Entrenamiento del modelo (nombre por actualizar)
│   └── 04_export_model.ipynb # Exportación a pkl/onnx (nombre por actualizar)
│
├── model/
│   ├── modelo.pkl         # Modelo para pruebas locales (nombre por actualizar)
│   └── modelo.onnx        # Modelo para backend en producción (nombre por actualizar)
│
├── api/
│   ├── main.py            # API REST (FastAPI) (nombre por actualizar)
│   ├── requirements.txt   # Dependencias del backend (nombre por actualizar)
│   └── utils/             # Funciones auxiliares
│
├── ui/
│   ├── index.html         # Interfaz web (nombre por actualizar)
│   ├── script.js          # Lógica del frontend (nombre por actualizar)
│   └── styles.css         # Estilos (nombre por actualizar)
│
├── oci/
│   ├── architecture.png   # Diagrama de arquitectura (nombre por actualizar)
│   └── deployment.md      # Guía de despliegue en OCI (nombre por actualizar)
│
└── README.md              # Documentación del proyecto.
```

<h1></h1>

<h3> Ejecución del proyecto </h3>

<h1></h1>

<h3> Ejemplos de uso </h3>

<h1></h1>

<h3> Despliegue en OCI </h3>

<h1></h1>

<h3> Licencias </h3>
