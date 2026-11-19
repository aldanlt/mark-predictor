# Práctica 6 – Planificación Scrum

> Documento de trabajo del equipo. Rellenad los nombres y actualizad el estado de las tareas en el tablero
> (GitHub Projects o similar) a medida que avancéis.

## Roles

| Rol | Integrante |
|-----|------------|
| Scrum Master | _(por decidir)_ |
| Equipo de desarrollo | _(todos los integrantes, incluido el Scrum Master)_ |

## 1. Análisis del Product Backlog

| ID | Historia | Componentes afectados | Prioridad | Estimación (SP) |
|----|----------|-----------------------|-----------|-----------------|
| HU-05 | Persistencia de datos mediante MySQL | Docker Compose, MySQL, backend | Alta | 5 |
| HU-01 | Gestión de asignaturas | MySQL, backend | Alta | 3 |
| HU-04 | Matrícula de estudiantes en asignaturas | MySQL, backend, frontend | Alta | 5 |
| HU-02 | Predicción según la dificultad | Microservicio Python, backend | Media | 5 |
| HU-03 | Interfaz web de asignaturas | Frontend | Media | 2 |

### Dependencias

```
HU-05 (MySQL) ──► HU-01 (asignaturas API) ──► HU-03 (interfaz asignaturas)
                         │
                         └──► HU-04 (matrícula + horas) ──► HU-02 (predicción por asignatura)
```

- HU-01 y HU-04 necesitan el esquema de base de datos (HU-05) para persistir.
- HU-04 necesita que existan asignaturas (HU-01) para poder seleccionarlas.
- HU-02 necesita las horas por asignatura (HU-04) y la dificultad (HU-01).
- La parte Python de HU-02 (coeficientes) **no depende** de nada y puede empezar desde el primer momento.

## 2. Tareas técnicas por historia

### HU-05 · Persistencia con MySQL
- T-01 Añadir servicio `mysql` (imagen 8.4) a `docker-compose.yml` con volumen persistente y *healthcheck*.
- T-02 Añadir las dependencias de JPA y del driver de MySQL al backend.
- T-03 Diseñar las entidades `Student`, `Subject` y `Enrollment` (Hibernate crea las tablas).
- T-04 Sustituir el `HashMap` por repositorios Spring Data JPA.
- T-05 Configurar la conexión a MySQL mediante variables de entorno.
- T-06 Comprobar que los datos se conservan tras `docker compose down` + `up`.

### HU-01 · Gestión de asignaturas
- T-07 Entidad `Subject` y enum `Difficulty` (`FACIL`, `MEDIA`, `DIFICIL`).
- T-08 `SubjectRepository` y `SubjectService` (alta, consulta, nombre único).
- T-09 `SubjectController`: `GET /api/subjects`, `GET /api/subjects/{id}`, `POST /api/subjects`.
- T-10 Rechazar niveles de dificultad no admitidos con un error 400 claro.

### HU-03 · Interfaz web de asignaturas
- T-11 Pestaña "Asignaturas" con formulario (nombre + `select` de dificultad, campos obligatorios).
- T-12 Tabla de asignaturas registradas consumiendo la API REST.

### HU-04 · Matrícula de estudiantes
- T-13 Entidad `Enrollment` (estudiante, asignatura, horas) con restricción única estudiante-asignatura.
- T-14 Ampliar `POST /api/students` para recibir `subjects: [{subjectId, studyHours}]`.
- T-15 Validar horas no negativas y asignaturas repetidas/inexistentes.
- T-16 Incluir las asignaturas y horas en `GET /api/students` y `GET /api/students/{id}`.
- T-17 Formulario de estudiantes con *checkboxes* de asignaturas y un campo de horas por asignatura.

### HU-02 · Predicción por dificultad
- T-18 (Python) Tabla de coeficientes 1,2 / 1,0 / 0,8 en `POST /predict` (campo `difficulty`).
- T-19 (Python) Limitar el resultado al rango 0-100 del predictor y tests con pytest.
- T-20 (Java) Ampliar `PredictionClient` para enviar la dificultad de la asignatura.
- T-21 (Java) Endpoint `GET /api/students/{id}/predictions` (una predicción por asignatura cursada).
- T-22 (Frontend) Panel de predicción con la predicción general y la de cada asignatura.

## 3. Historias técnicas identificadas por el equipo

No estaban en el backlog inicial, pero son necesarias para completar las historias:

| ID | Historia técnica | Justificación |
|----|------------------|---------------|
| HT-01 | Gestor global de errores (`@RestControllerAdvice`) | Respuestas de error claras (400/404/409/503) que el frontend pueda mostrar. |
| HT-02 | Proxy Nginx `/api` → backend | El frontend y la API quedan en la misma dirección, así el navegador no bloquea las peticiones (CORS). |
| HT-03 | *Healthcheck* de MySQL en Compose | El backend no debe arrancar antes de que MySQL acepte conexiones. |
| HT-04 | Tests automáticos (pytest + JUnit) | Comprobar los coeficientes sin tener que levantar todo el sistema. |

## 4. Orden inicial y reparto propuesto

| Orden | Tarea(s) | Responsable inicial |
|-------|----------|---------------------|
| 1 | T-01, T-02, T-03, HT-03 | Dev A (infraestructura / BD) |
| 1 | T-18, T-19 | Dev B (Python) |
| 2 | T-04, T-05, T-07 … T-10, HT-01 | Dev C (backend) |
| 2 | T-11, T-12, HT-02 | Dev D (frontend) |
| 3 | T-13 … T-16 | Dev C + Dev A |
| 3 | T-17 | Dev D |
| 4 | T-20, T-21, T-22 | Dev B + Dev D |
| 5 | T-06, HT-04, pruebas de integración | Todo el equipo |

> El reparto no es definitivo: quien termine antes revisa el tablero y coge la siguiente tarea
> desbloqueada, ayuda en integración o revisa código de un compañero.

## 5. Revisión al final de la sesión (plantilla)

- **Completado:**
- **En desarrollo:**
- **Dificultades técnicas encontradas:**
- **Dependencias pendientes entre componentes:**
- **Tareas para continuar fuera del aula:**
