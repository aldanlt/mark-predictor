# Cómo funciona mark-predictor

Una explicación sencilla, pieza a pieza, de la aplicación *Student Performance Predictor*.

---

## 1. ¿Qué hace la aplicación?

- Registra **asignaturas** (con dificultad Fácil, Media o Difícil).
- Registra **estudiantes** y los **matricula** en asignaturas, con las horas de estudio de cada una.
- **Predice** el rendimiento de cada estudiante en general y en cada asignatura.
- Guarda todo en **MySQL**, así que no se pierde al reiniciar.

---

## 2. Las cuatro piezas

```
Navegador ──► frontend (Nginx) ──/api──► backend Java ──► prediction-service (Python)
                                              │
                                              ▼
                                            MySQL
```

| Pieza | Qué hace | Tecnología |
|-------|----------|------------|
| **frontend** | Las pantallas que ve el usuario | HTML + CSS + JavaScript, servido con Nginx |
| **backend** | Recibe las peticiones, aplica las reglas y guarda los datos | Java + Spring Boot |
| **prediction-service** | Hace los cálculos de predicción | Python + FastAPI |
| **MySQL** | Base de datos donde se guarda todo | MySQL 8 |

Cada pieza tiene **una sola responsabilidad**: el frontend no sabe calcular, el backend no hace los
cálculos de predicción y el servicio Python no sabe qué es una base de datos. Por eso, si cambia la fórmula de predicción,
solo hay que tocar `prediction-service`.

---

## 3. Ejemplo: ¿qué pasa al pulsar «Predicción»?

1. El **frontend** (`app.js`) hace `GET /api/students/1/predictions`.
2. **Nginx** reenvía la petición al backend (`nginx.conf`).
3. `StudentController` recibe la petición y llama a `PredictionService`.
4. `PredictionService` obtiene el estudiante y sus asignaturas de la base de datos.
5. Para cada asignatura, `PredictionClient` envía al servicio Python:
   `{"study_hours": 6, "attendance": 85, "previous_grade": 6.5, "difficulty": "DIFICIL"}`
6. El **servicio Python** calcula y responde: `{"base": 51.5, "coefficient": 0.8, "score": 41.2}`.
7. El backend junta todos los resultados y los devuelve como JSON.
8. El frontend pinta la tabla.

---

## 4. El backend Java por capas

Todo está en `backend/src/main/java/es/uvigo/markpredictor/`:

| Carpeta | Qué contiene | Ejemplo |
|---------|--------------|---------|
| `model` | Las **entidades**: clases que se guardan en la base de datos (`@Entity`) | `Student`, `Subject`, `Enrollment` |
| `repository` | Interfaces para leer y guardar en MySQL. Spring escribe las consultas solo | `existsByName` |
| `service` | Las **reglas de negocio** (validaciones, comprobaciones) | «las horas no pueden ser negativas» |
| `controller` | Las **rutas de la API REST** (`@GetMapping`, `@PostMapping`…) | `GET /api/students` |
| `dto` | Objetos con los datos que llegan de un formulario | `StudentRequest` |
| `client` | Clases que llaman a los microservicios Python | `PredictionClient` |
| `exception` | Errores propios y `ErrorHandler`, que los convierte en respuestas HTTP | `NotFoundException` → 404 |

**Regla de oro:** el controlador solo recibe y responde; las decisiones se toman en el servicio, y el
repositorio solo habla con la base de datos.

### Las entidades y sus relaciones

```
Student 1 ──── * Enrollment * ──── 1 Subject
```

- **Enrollment** (matrícula) une un estudiante con una asignatura y guarda las horas de estudio que le dedica.
- La restricción única `(student, subject)` impide matricular dos veces al mismo estudiante en la misma asignatura.
- Al guardar un estudiante se guardan también sus matrículas (`cascade = CascadeType.ALL`).

### Base de datos

No escribimos SQL: con `spring.jpa.hibernate.ddl-auto=update`, **Hibernate crea las tablas** a partir de
las entidades al arrancar (`student`, `subject`, `enrollment`). Los datos
se guardan en un volumen de Docker, por eso no se pierden al reiniciar.

### Errores

Cuando algo falla, el servicio lanza una excepción, por ejemplo
`throw new ConflictException("Ya existe una asignatura llamada Algoritmos")`. `ErrorHandler` la captura y responde
con el código adecuado y `{"error": "..."}`, que el frontend muestra en rojo.

| Excepción | Código HTTP | Cuándo |
|-----------|-------------|--------|
| `BadRequestException` | 400 | Datos no válidos (horas negativas, asistencia de 120 %…) |
| `NotFoundException` | 404 | El estudiante o la asignatura no existe |
| `ConflictException` | 409 | Asignatura con un nombre que ya existe |
| `ServiceUnavailableException` | 503 | El microservicio Python no responde |

---

## 5. El microservicio Python

Es un único fichero, `prediction-service/main.py`.

**prediction-service** (`POST /predict`):

```
base = 50 · min(horas, 20)/20  +  20 · asistencia/100  +  30 · notaPrevia/10      (de 0 a 100)
predicción = base × coeficiente      Fácil 1,2 · Media 1,0 · Difícil 0,8
```

El resultado se limita a 0-100. Ejemplo del enunciado: una base de 70 da 84 (fácil), 70 (media) y 56 (difícil).
Si no se envía dificultad, el coeficiente es 1 (predicción general).

## 6. El frontend

- `index.html`: las dos pestañas (Estudiantes y Asignaturas). Solo se ve la que tiene la clase `active`.
- `js/app.js`: un único fichero, organizado por secciones. Todo funciona igual:
  1. La función `api()` llama al backend con `fetch`.
  2. `cargarTodo()` descarga asignaturas y estudiantes y vuelve a pintar las tablas.
  3. Cada formulario tiene un `submit` que envía los datos y, si todo va bien, llama a `cargarTodo()`.
- Los campos `required`, `min` y `max` del HTML impiden enviar un formulario incompleto, y el backend
  vuelve a comprobarlo todo por seguridad.

---

## 7. ¿Dónde está cada historia de usuario?

### Práctica 6

| Historia | Dónde |
|----------|-------|
| HU-01 Asignaturas | `Subject`, `Difficulty`, `SubjectService`, `SubjectController` |
| HU-02 Predicción por dificultad | `prediction-service/main.py`, `PredictionClient`, `PredictionService` |
| HU-03 Interfaz de asignaturas | Pestaña «Asignaturas» (`index.html`, `app.js`) |
| HU-04 Matrícula con horas | `StudentRequest`, `StudentService.create`, casillas del formulario de estudiantes |
| HU-05 MySQL | `docker-compose.yml` (servicio `mysql` y volumen), entidades JPA, `application.properties` |

---

## 8. Docker Compose

`docker-compose.yml` describe los cuatro contenedores, sus puertos y cómo se conectan. Dentro de Docker,
cada servicio se llama por su nombre (`mysql`, `prediction-service`…), y esos nombres se pasan al
backend como variables de entorno que se leen en `application.properties`.

```bash
docker compose up --build      # construir y arrancar todo
docker compose down            # parar (los datos se conservan)
docker compose down -v         # parar y borrar los datos
```
