# Student Performance Predictor (mark-predictor)

Proyecto de **Ingeniería del Software (Universidade de Vigo, 26/27)**. Aplicación web para registrar
estudiantes y asignaturas y predecir el rendimiento académico de cada estudiante en cada asignatura.

👉 **Explicación del proyecto paso a paso: [docs/explicacion.md](docs/explicacion.md)**

## Arquitectura

```
Navegador ──► frontend (Nginx) ──/api──► backend Java (Spring Boot) ──► prediction-service (Python)
                                               │
                                               ▼
                                             MySQL
```

| Servicio | Carpeta | Dirección |
|----------|---------|-----------|
| Frontend | `frontend/` | http://localhost:3000 |
| Backend Java (API REST) | `backend/` | http://localhost:8080/api |
| Microservicio de predicciones | `prediction-service/` | http://localhost:8000/docs |
| MySQL | (imagen oficial) | `localhost:3307` |

## Puesta en marcha

Requisito: Docker Desktop.

```bash
docker compose up --build
```

Abre http://localhost:3000.

Los datos se guardan en el volumen `mysql-data`, así que se conservan al reiniciar los contenedores.
Para borrarlos: `docker compose down -v`.

## API REST

| Método | Ruta | Para qué sirve |
|--------|------|----------------|
| GET / POST | `/api/subjects` | Listar / crear asignaturas `{"name", "difficulty": "FACIL" \| "MEDIA" \| "DIFICIL"}` |
| GET / POST | `/api/students` | Listar / registrar estudiantes `{"name", "studyHours", "attendance", "previousGrade", "subjects": [{"subjectId", "studyHours"}]}` |
| GET | `/api/students/{id}/prediction` | Predicción general |
| GET | `/api/students/{id}/predictions` | Predicción de cada asignatura cursada |

## Tests

```bash
cd prediction-service && pip install -r requirements-dev.txt && pytest
cd backend && mvn package     # compila el backend (también lo hace docker compose up --build)
```

## Documentación Scrum

- [Práctica 6: planificación](docs/scrum/practica-6.md)
