# Student Performance Predictor (mark-predictor)

Proyecto de **Ingeniería del Software (Universidade de Vigo, 26/27)**. Aplicación web para gestionar
estudiantes, asignaturas, cursos académicos y matrículas, predecir el rendimiento académico y
descargar informes con el historial de cada estudiante.

👉 **Explicación del proyecto paso a paso: [docs/explicacion.md](docs/explicacion.md)**

## Arquitectura

```
Navegador ──► frontend (Nginx) ──/api──► backend Java (Spring Boot) ──► prediction-service (Python)
                                               │                    └──► report-service (Python)
                                               ▼
                                             MySQL
```

| Servicio | Carpeta | Dirección |
|----------|---------|-----------|
| Frontend | `frontend/` | http://localhost:3000 |
| Backend Java (API REST) | `backend/` | http://localhost:8080/api |
| Microservicio de predicciones | `prediction-service/` | http://localhost:8000/docs |
| Microservicio de informes | `report-service/` | http://localhost:8001/docs |
| MySQL | (imagen oficial) | `localhost:3307` |

## Puesta en marcha

Requisito: Docker Desktop.

```bash
docker compose up --build
```

Abre http://localhost:3000. Para cargar datos de ejemplo (con la tabla de Ana del enunciado):

```bash
python3 scripts/seed_demo.py
```

Los datos se guardan en el volumen `mysql-data`, así que se conservan al reiniciar los contenedores.
Para borrarlos: `docker compose down -v`.

## API REST

| Método | Ruta | Para qué sirve |
|--------|------|----------------|
| GET / POST | `/api/subjects` | Listar / crear asignaturas `{"name", "difficulty": "FACIL" \| "MEDIA" \| "DIFICIL"}` |
| GET / POST | `/api/courses` | Listar / crear cursos `{"name": "2025/2026", "startDate", "endDate", "subjectIds": [1, 2]}` |
| PUT / DELETE | `/api/courses/{id}` | Modificar / eliminar un curso (no se puede si tiene matrículas) |
| GET / POST | `/api/students` | Listar / registrar estudiantes `{"name", "studyHours", "attendance", "previousGrade", "courseId", "subjects": [{"subjectId", "studyHours"}]}` |
| GET | `/api/students/{id}/enrollments` | Historial académico |
| GET | `/api/students/{id}/prediction` | Predicción general |
| GET | `/api/students/{id}/predictions` | Predicción de cada matrícula |
| POST | `/api/enrollments` | Matricular `{"studentId", "subjectId", "courseId", "studyHours", "grade"}` |
| PUT / DELETE | `/api/enrollments/{id}` | Cambiar horas y nota / eliminar una matrícula |
| GET | `/api/reports/students/{id}?format=csv` | Descargar el informe (`csv` o `json`) |

## Patrones de diseño (Práctica 7)

| Patrón | Clase |
|--------|-------|
| Singleton | [`config/AppConfig`](backend/src/main/java/es/uvigo/markpredictor/config/AppConfig.java) |
| Clase abstracta | [`report/AcademicReport`](backend/src/main/java/es/uvigo/markpredictor/report/AcademicReport.java) → `CsvReport`, `JsonReport` |
| Factory | [`report/ReportFactory`](backend/src/main/java/es/uvigo/markpredictor/report/ReportFactory.java) |
| Adapter | [`report/ReportServiceAdapter`](backend/src/main/java/es/uvigo/markpredictor/report/ReportServiceAdapter.java) (implementa `ReportGenerator` y usa `ReportServiceClient`) |
| Facade | [`report/AcademicReportFacade`](backend/src/main/java/es/uvigo/markpredictor/report/AcademicReportFacade.java) |

## Tests

```bash
cd prediction-service && pip install -r requirements-dev.txt && pytest
cd report-service && pip install -r requirements-dev.txt && pytest
cd backend && mvn test        # también se ejecutan al hacer docker compose up --build
```

## Documentación Scrum

- [Práctica 6: planificación](docs/scrum/practica-6.md)
- [Práctica 7: planificación y diseño](docs/scrum/practica-7.md)
