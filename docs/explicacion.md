# Cómo funciona mark-predictor

Una explicación sencilla, pieza a pieza, de la aplicación *Student Performance Predictor*.

---

## 1. ¿Qué hace la aplicación?

- Registra **asignaturas** (con dificultad Fácil, Media o Difícil).
- Registra **cursos académicos** (2024/2025, 2025/2026…) y qué asignaturas se imparten en cada uno.
- Registra **estudiantes** y los **matricula** en asignaturas de un curso, con sus horas de estudio.
- Guarda la **nota** de cada matrícula, así que se conserva el historial aunque el alumno repita.
- **Predice** el rendimiento de cada estudiante en cada asignatura.
- Genera un **informe** del historial en CSV o JSON.

---

## 2. Las cinco piezas

```
Navegador ──► frontend (Nginx) ──/api──► backend Java ──► prediction-service (Python)
                                              │       └──► report-service (Python)
                                              ▼
                                            MySQL
```

| Pieza | Qué hace | Tecnología |
|-------|----------|------------|
| **frontend** | Las pantallas que ve el usuario | HTML + CSS + JavaScript, servido con Nginx |
| **backend** | Recibe las peticiones, aplica las reglas y guarda los datos | Java + Spring Boot |
| **prediction-service** | Hace los cálculos de predicción | Python + FastAPI |
| **report-service** | Construye los ficheros CSV y JSON | Python + FastAPI |
| **MySQL** | Base de datos donde se guarda todo | MySQL 8 |

Cada pieza tiene **una sola responsabilidad**: el frontend no sabe calcular, el backend no sabe construir un
CSV y los servicios Python no saben qué es una base de datos. Por eso, si cambia la fórmula de predicción,
solo hay que tocar `prediction-service`.

---

## 3. Ejemplo: ¿qué pasa al pulsar «Predicción»?

1. El **frontend** (`app.js`) hace `GET /api/students/1/predictions`.
2. **Nginx** reenvía la petición al backend (`nginx.conf`).
3. `StudentController` recibe la petición y llama a `PredictionService`.
4. `PredictionService` pide a la base de datos las matrículas del estudiante.
5. Para cada matrícula, `PredictionClient` envía al servicio Python:
   `{"study_hours": 6, "attendance": 85, "previous_grade": 6.5, "difficulty": "DIFICIL"}`
6. El **servicio Python** calcula y responde: `{"base": 51.5, "coefficient": 0.8, "score": 41.2}`.
7. El backend junta todos los resultados y los devuelve como JSON.
8. El frontend pinta la tabla.

---

## 4. El backend Java por capas

Todo está en `backend/src/main/java/es/uvigo/markpredictor/`:

| Carpeta | Qué contiene | Ejemplo |
|---------|--------------|---------|
| `model` | Las **entidades**: clases que se guardan en la base de datos (`@Entity`) | `Student`, `Subject`, `Course`, `Enrollment` |
| `repository` | Interfaces para leer y guardar en MySQL. Spring escribe las consultas solo, a partir del nombre del método | `findByStudentIdOrderByCourseStartDateAsc` |
| `service` | Las **reglas de negocio** (validaciones, comprobaciones) | «no se puede borrar un curso con matrículas» |
| `controller` | Las **rutas de la API REST** (`@GetMapping`, `@PostMapping`…) | `GET /api/students` |
| `dto` | Objetos con los datos que llegan de un formulario | `StudentRequest` |
| `client` | Clases que llaman a los microservicios Python | `PredictionClient` |
| `report` | Todo lo de los informes (los patrones de la P7) | `ReportFactory` |
| `exception` | Errores propios y `ErrorHandler`, que los convierte en respuestas HTTP | `NotFoundException` → 404 |
| `config` | Configuración (`AppConfig`, conexión a MySQL) | |

**Regla de oro:** el controlador solo recibe y responde; las decisiones se toman en el servicio, y el
repositorio solo habla con la base de datos.

### Las entidades y sus relaciones

```
Student 1 ──── * Enrollment * ──── 1 Subject
                    │
                    * ──── 1 Course * ──── * Subject   (asignaturas que se imparten en el curso)
```

- **Enrollment** (matrícula) es la pieza central: une estudiante + asignatura + curso, y guarda las
  horas de estudio y la nota (`grade`, que puede estar vacía).
- La restricción única `(student, subject, course)` impide matricular dos veces en la misma asignatura
  y curso, pero **sí** permite repetir la asignatura en otro curso. Así se conserva el historial:

| Estudiante | Asignatura | Curso | Nota |
|------------|------------|-------|------|
| Ana | Algoritmos | 2024/2025 | 3,5 |
| Ana | Algoritmos | 2025/2026 | 6,2 |
| Ana | Ingeniería del Software | 2025/2026 | 8,4 |

### Base de datos

No escribimos SQL: con `spring.jpa.hibernate.ddl-auto=update`, **Hibernate crea las tablas** a partir de
las entidades al arrancar (`student`, `subject`, `course`, `course_subjects`, `enrollment`). Los datos
se guardan en un volumen de Docker, por eso no se pierden al reiniciar.

### Errores

Cuando algo falla, el servicio lanza una excepción, por ejemplo
`throw new ConflictException("Ya existe el curso 2025/2026")`. `ErrorHandler` la captura y responde
con el código adecuado y `{"error": "..."}`, que el frontend muestra en rojo.

| Excepción | Código HTTP | Cuándo |
|-----------|-------------|--------|
| `BadRequestException` | 400 | Datos no válidos (horas negativas, nota 11, fechas al revés…) |
| `NotFoundException` | 404 | El estudiante, curso… no existe |
| `ConflictException` | 409 | Duplicados, o borrar un curso con matrículas |
| `ServiceUnavailableException` | 503 | Un microservicio Python no responde |

---

## 5. Los microservicios Python

Cada uno es un único fichero `main.py`.

**prediction-service** (`POST /predict`):

```
base = 50 · min(horas, 20)/20  +  20 · asistencia/100  +  30 · notaPrevia/10      (de 0 a 100)
predicción = base × coeficiente      Fácil 1,2 · Media 1,0 · Difícil 0,8
```

El resultado se limita a 0-100. Ejemplo del enunciado: una base de 70 da 84 (fácil), 70 (media) y 56 (difícil).
Si no se envía dificultad, el coeficiente es 1 (predicción general).

**report-service** (`POST /reports/csv` o `/reports/json`): recibe el estudiante y sus matrículas y
devuelve el texto del fichero. Usa sus propios nombres de campos (`student`, `records`, `name`,
`academic_year`, `score`), distintos a los de Java. Por eso hace falta el patrón Adapter.

---

## 6. El frontend

- `index.html`: las cinco pestañas (Estudiantes, Asignaturas, Cursos, Matrículas, Informes). Solo se
  ve la que tiene la clase `active`.
- `js/app.js`: un único fichero, organizado por secciones. Todo funciona igual:
  1. La función `api()` llama al backend con `fetch`.
  2. `cargarTodo()` descarga asignaturas, cursos y estudiantes y vuelve a pintar las tablas.
  3. Cada formulario tiene un `submit` que envía los datos y, si todo va bien, llama a `cargarTodo()`.
- Los campos `required`, `min` y `max` del HTML impiden enviar un formulario incompleto, y el backend
  vuelve a comprobarlo todo por seguridad.

---

## 7. Los patrones de diseño (Práctica 7)

### Singleton: `AppConfig`

**Problema:** muchas clases necesitan la dirección de MySQL o de los servicios Python. Si cada una la
escribe a mano, habrá copias distintas.

**Solución:** una única clase de configuración, con una única instancia.

```java
private static AppConfig instance;
private AppConfig() { ... }                      // nadie puede hacer new AppConfig()

public static synchronized AppConfig getInstance() {
    if (instance == null) instance = new AppConfig();   // se crea la primera vez
    return instance;                                    // después, siempre la misma
}
```

La usan `DatabaseConfig`, `PredictionClient` y `ReportServiceClient`. Lee las variables de entorno de
`docker-compose.yml`.

### Clase abstracta: `AcademicReport`

Todo informe tiene un estudiante, sus matrículas y un nombre de fichero: eso va en la clase abstracta.
Lo que cambia según el formato (`getFormat()`, `getContentType()`) son métodos abstractos que
implementan `CsvReport` y `JsonReport`.

### Factory: `ReportFactory`

Quien pide un informe solo dice el formato como texto. La factoría decide qué clase crear:

```java
switch (format.toLowerCase()) {
    case "csv":  return new CsvReport(...);
    case "json": return new JsonReport(...);
    default:     throw new BadRequestException("Formato no admitido");
}
```

Si mañana hay un informe PDF, solo se toca la factoría.

### Adapter: `ReportServiceAdapter`

Es como un adaptador de enchufe. La aplicación espera un `ReportGenerator` (la interfaz), pero lo que
tenemos es `ReportServiceClient`, que habla «en Python». El adaptador implementa la interfaz y traduce
los datos por dentro:

| Java | Python |
|------|--------|
| `studentId`, `studentName` | `student: {id, name}` |
| `enrollments` | `records` |
| `subject` | `name` |
| `course` | `academic_year` |
| `grade` | `score` |

### Facade: `AcademicReportFacade`

Generar un informe requiere: buscar el estudiante, buscar sus matrículas, preparar los datos, pedir el
objeto a la factoría y llamar al adaptador. La fachada esconde todo eso tras **un solo método**:

```java
GeneratedReport informe = reportFacade.generateReport(studentId, "csv");
```

Gracias a ella, `ReportController` tiene dos líneas y no conoce ningún detalle interno.

```
ReportController ──► AcademicReportFacade ──► StudentService / EnrollmentService
                                          ├──► ReportFactory ──► CsvReport / JsonReport
                                          └──► ReportGenerator (ReportServiceAdapter) ──► ReportServiceClient ──► Python
```

---

## 8. ¿Dónde está cada historia de usuario?

### Práctica 6

| Historia | Dónde |
|----------|-------|
| HU-01 Asignaturas | `Subject`, `Difficulty`, `SubjectService`, `SubjectController` |
| HU-02 Predicción por dificultad | `prediction-service/main.py`, `PredictionClient`, `PredictionService` |
| HU-03 Interfaz de asignaturas | Pestaña «Asignaturas» (`index.html`, `app.js`) |
| HU-04 Matrícula con horas | `StudentRequest`, `StudentService.create`, casillas del formulario de estudiantes |
| HU-05 MySQL | `docker-compose.yml` (servicio `mysql` y volumen), entidades JPA, `DatabaseConfig` |

### Práctica 7

| Historia | Dónde |
|----------|-------|
| HU-01 Cursos académicos | `Course`, `CourseService`, `CourseController`, pestaña «Cursos» |
| HU-02 Matrículas y notas históricas | `Enrollment` (curso + nota), `EnrollmentService`, pestaña «Matrículas» |
| HU-03 Singleton | `config/AppConfig` |
| HU-04 Clase abstracta + Factory | `report/AcademicReport`, `CsvReport`, `JsonReport`, `ReportFactory` |
| HU-05 Microservicio + Adapter | `report-service/main.py`, `ReportGenerator`, `ReportServiceAdapter`, `ReportServiceClient` |
| HU-06 Facade | `report/AcademicReportFacade`, `ReportController`, pestaña «Informes» |

---

## 9. Docker Compose

`docker-compose.yml` describe los cinco contenedores, sus puertos y cómo se conectan. Dentro de Docker,
cada servicio se llama por su nombre (`mysql`, `prediction-service`…), y esos nombres se pasan al
backend como variables de entorno que lee `AppConfig`.

```bash
docker compose up --build      # construir y arrancar todo
docker compose down            # parar (los datos se conservan)
docker compose down -v         # parar y borrar los datos
python3 scripts/seed_demo.py   # cargar datos de ejemplo
```
