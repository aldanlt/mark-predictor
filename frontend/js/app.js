// =====================================================================
//  Student Performance Predictor - Frontend
//  Todas las operaciones se hacen llamando a la API REST del backend Java.
//  (Nginx redirige las rutas /api/... al backend.)
// =====================================================================

const API = '/api';

const DIFICULTADES = { FACIL: 'Fácil', MEDIA: 'Media', DIFICIL: 'Difícil' };

// Datos cargados del backend (se recargan después de cada cambio)
let asignaturas = [];
let cursos = [];
let estudiantes = [];

// ---------------------------------------------------------------------
//  Funciones de ayuda
// ---------------------------------------------------------------------

/** Llama a la API. Si el backend responde con error, lanza su mensaje. */
async function api(ruta, metodo = 'GET', datos = null) {
    const opciones = { method: metodo, headers: { 'Content-Type': 'application/json' } };
    if (datos !== null) {
        opciones.body = JSON.stringify(datos);
    }
    const respuesta = await fetch(API + ruta, opciones);
    if (!respuesta.ok) {
        const error = await respuesta.json().catch(() => ({}));
        throw new Error(error.error || 'Error ' + respuesta.status);
    }
    return respuesta.status === 204 ? null : respuesta.json();
}

/** Aviso en la esquina de la pantalla (verde si todo va bien, rojo si hay error). */
function aviso(texto, esError = false) {
    const caja = document.getElementById('toast');
    caja.textContent = texto;
    caja.className = esError ? 'toast error' : 'toast';
    caja.hidden = false;
    setTimeout(() => { caja.hidden = true; }, 4000);
}

/** Ejecuta una acción y muestra el error si falla. */
async function intentar(accion) {
    try {
        await accion();
    } catch (error) {
        aviso(error.message, true);
    }
}

/** Evita que un nombre con caracteres como < o > rompa el HTML. */
function esc(texto) {
    const div = document.createElement('div');
    div.textContent = texto ?? '';
    return div.innerHTML;
}

/** Etiqueta de color con la dificultad. */
function etiqueta(dificultad) {
    return `<span class="badge ${dificultad}">${DIFICULTADES[dificultad]}</span>`;
}

/** Etiqueta con el estado de una nota: aprobado, suspenso o sin calificar. */
function estado(nota) {
    if (nota === null || nota === undefined) return '<span class="badge">Sin calificar</span>';
    return nota >= 5 ? '<span class="badge FACIL">Aprobado</span>' : '<span class="badge DIFICIL">Suspenso</span>';
}

/** Barra horizontal que representa una puntuación de 0 a 100. */
function barra(puntos) {
    return `<div class="score-bar"><span style="width:${puntos}%"></span></div>`;
}

/** Fecha 2025-09-01 -> 1/9/2025 */
function fecha(iso) {
    return new Date(iso + 'T00:00:00').toLocaleDateString('es-ES');
}

function filaVacia(columnas, texto) {
    return `<tr><td colspan="${columnas}" class="empty">${texto}</td></tr>`;
}

// ---------------------------------------------------------------------
//  Pestañas
// ---------------------------------------------------------------------

document.querySelectorAll('.tabs button').forEach((boton) => {
    boton.addEventListener('click', () => {
        document.querySelectorAll('.tabs button').forEach((b) => b.classList.remove('active'));
        document.querySelectorAll('.tab-panel').forEach((p) => p.classList.remove('active'));
        boton.classList.add('active');
        document.getElementById(boton.dataset.tab).classList.add('active');
    });
});

// ---------------------------------------------------------------------
//  Cargar datos y pintar la página
// ---------------------------------------------------------------------

async function cargarTodo() {
    asignaturas = await api('/subjects');
    cursos = await api('/courses');
    estudiantes = await api('/students');

    pintarAsignaturas();
    pintarCursos();
    pintarEstudiantes();
    pintarDesplegables();
    pintarCasillasEstudiante();
    pintarAsignaturasMatricula();
    await pintarHistorial();
}

function pintarAsignaturas() {
    document.getElementById('tabla-asignaturas').innerHTML = asignaturas.length
        ? asignaturas.map((a) => `<tr><td>${esc(a.name)}</td><td>${etiqueta(a.difficulty)}</td></tr>`).join('')
        : filaVacia(2, 'Todavía no hay asignaturas registradas');

    // Casillas para elegir las asignaturas que se imparten en un curso
    document.getElementById('curso-asignaturas').innerHTML = asignaturas.length
        ? asignaturas.map((a) =>
            `<label class="inline"><input type="checkbox" value="${a.id}"> ${esc(a.name)}</label>`).join('')
        : '<p class="hint">Registra primero alguna asignatura.</p>';
}

function pintarCursos() {
    document.getElementById('tabla-cursos').innerHTML = cursos.length
        ? cursos.map((c) => `
            <tr>
                <td><strong>${esc(c.name)}</strong></td>
                <td>${fecha(c.startDate)} – ${fecha(c.endDate)}</td>
                <td><div class="chips">${c.subjects.map((a) => `<span class="chip">${esc(a.name)}</span>`).join('')}</div></td>
                <td class="actions">
                    <button type="button" class="secondary small" onclick="editarCurso(${c.id})">Editar</button>
                    <button type="button" class="danger small" onclick="eliminarCurso(${c.id})">Eliminar</button>
                </td>
            </tr>`).join('')
        : filaVacia(4, 'Todavía no hay cursos académicos');
}

function pintarEstudiantes() {
    document.getElementById('tabla-estudiantes').innerHTML = estudiantes.length
        ? estudiantes.map((e) => `
            <tr>
                <td>${esc(e.name)}</td>
                <td>${e.attendance} %</td>
                <td>${e.previousGrade}</td>
                <td><div class="chips">${e.enrollments.map((m) =>
                    `<span class="chip">${esc(m.subject.name)} · ${esc(m.course.name)} · ${m.studyHours} h</span>`).join('')}</div></td>
                <td class="actions">
                    <button type="button" class="secondary small" onclick="verPrediccion(${e.id})">Predicción</button>
                </td>
            </tr>`).join('')
        : filaVacia(5, 'Todavía no hay estudiantes registrados');
}

/** Rellena los desplegables de cursos y estudiantes, sin perder lo que estaba elegido. */
function pintarDesplegables() {
    const opcionesCursos = cursos.map((c) => `<option value="${c.id}">${esc(c.name)}</option>`).join('');
    rellenar(document.getElementById('estudiante-curso'), '<option value="">Sin matrícula</option>' + opcionesCursos);
    rellenar(document.getElementById('matricula-curso'), opcionesCursos);

    const opcionesEstudiantes = estudiantes.map((e) => `<option value="${e.id}">${esc(e.name)}</option>`).join('');
    document.querySelectorAll('.select-estudiante').forEach((select) => rellenar(select, opcionesEstudiantes));
}

function rellenar(select, opciones) {
    const elegido = select.value;
    select.innerHTML = opciones;
    if ([...select.options].some((o) => o.value === elegido)) select.value = elegido;
}

// ---------------------------------------------------------------------
//  ESTUDIANTES: registro con asignaturas (checkboxes) y predicciones
// ---------------------------------------------------------------------

/** Una casilla y un campo de horas por cada asignatura del curso elegido. */
function pintarCasillasEstudiante() {
    const cursoId = Number(document.getElementById('estudiante-curso').value);
    const curso = cursos.find((c) => c.id === cursoId);
    const contenedor = document.getElementById('estudiante-asignaturas');

    if (!curso) {
        contenedor.innerHTML = '<p class="hint">Elige un curso para matricular al estudiante.</p>';
        return;
    }
    contenedor.innerHTML = curso.subjects.map((a) => `
        <label class="subject-option">
            <input type="checkbox" value="${a.id}" onchange="activarHoras(this)">
            <span>${esc(a.name)}</span>
            ${etiqueta(a.difficulty)}
            <input type="number" class="horas" min="0" step="0.5" placeholder="horas" required disabled>
        </label>`).join('');
}

/** Al marcar una asignatura se activa su campo de horas (y es obligatorio). */
function activarHoras(casilla) {
    const horas = casilla.parentElement.querySelector('.horas');
    horas.disabled = !casilla.checked;
    if (!casilla.checked) horas.value = '';
}

document.getElementById('estudiante-curso').addEventListener('change', pintarCasillasEstudiante);

document.getElementById('form-estudiante').addEventListener('submit', (evento) => {
    evento.preventDefault();
    const form = evento.target;

    const seleccionadas = [];
    form.querySelectorAll('.subject-option').forEach((fila) => {
        const casilla = fila.querySelector('input[type="checkbox"]');
        if (casilla.checked) {
            seleccionadas.push({
                subjectId: Number(casilla.value),
                studyHours: Number(fila.querySelector('.horas').value),
            });
        }
    });

    const estudiante = {
        name: form.elements.name.value,
        studyHours: Number(form.elements.studyHours.value),
        attendance: Number(form.elements.attendance.value),
        previousGrade: Number(form.elements.previousGrade.value),
        courseId: form.elements.courseId.value ? Number(form.elements.courseId.value) : null,
        subjects: seleccionadas,
    };

    intentar(async () => {
        await api('/students', 'POST', estudiante);
        aviso(`Estudiante "${estudiante.name}" registrado`);
        form.reset();
        await cargarTodo();
    });
});

function verPrediccion(estudianteId) {
    intentar(async () => {
        const general = await api(`/students/${estudianteId}/prediction`);
        const porMatricula = await api(`/students/${estudianteId}/predictions`);
        const estudiante = estudiantes.find((e) => e.id === estudianteId);

        const filas = porMatricula.map((p) => `
            <tr>
                <td>${esc(p.course)}</td>
                <td>${esc(p.subject)}</td>
                <td>${etiqueta(p.difficulty)}</td>
                <td>${p.studyHours}</td>
                <td>${p.base}</td>
                <td>× ${p.coefficient}</td>
                <td><strong>${p.score}</strong></td>
                <td>${barra(p.score)}</td>
                <td>${p.grade ?? '—'}</td>
            </tr>`).join('');

        const panel = document.getElementById('panel-prediccion');
        panel.innerHTML = `
            <h2>Predicción de ${esc(estudiante.name)}</h2>
            <p class="hint">Predicción general (escala 0-100)</p>
            <p class="score-big">${general.score}</p>
            <h3>Predicción por matrícula</h3>
            ${porMatricula.length ? `
            <table>
                <thead><tr><th>Curso</th><th>Asignatura</th><th>Dificultad</th><th>Horas</th><th>Base</th>
                <th>Coef.</th><th>Predicción</th><th></th><th>Nota real</th></tr></thead>
                <tbody>${filas}</tbody>
            </table>` : '<p class="empty">El estudiante no tiene matrículas.</p>'}`;
        panel.hidden = false;
        panel.scrollIntoView({ behavior: 'smooth' });
    });
}

// ---------------------------------------------------------------------
//  ASIGNATURAS
// ---------------------------------------------------------------------

document.getElementById('form-asignatura').addEventListener('submit', (evento) => {
    evento.preventDefault();
    const form = evento.target;
    const asignatura = { name: form.elements.name.value, difficulty: form.elements.difficulty.value };
    intentar(async () => {
        await api('/subjects', 'POST', asignatura);
        aviso(`Asignatura "${asignatura.name}" registrada`);
        form.reset();
        await cargarTodo();
    });
});

// ---------------------------------------------------------------------
//  CURSOS ACADÉMICOS: registrar, modificar y eliminar
// ---------------------------------------------------------------------

const formCurso = document.getElementById('form-curso');

formCurso.addEventListener('submit', (evento) => {
    evento.preventDefault();
    const curso = {
        name: formCurso.elements.name.value,
        startDate: formCurso.elements.startDate.value,
        endDate: formCurso.elements.endDate.value,
        subjectIds: [...formCurso.querySelectorAll('#curso-asignaturas input:checked')].map((c) => Number(c.value)),
    };
    const id = formCurso.elements.id.value;

    intentar(async () => {
        if (id) {
            await api(`/courses/${id}`, 'PUT', curso);
        } else {
            await api('/courses', 'POST', curso);
        }
        aviso(`Curso ${curso.name} guardado`);
        limpiarFormCurso();
        await cargarTodo();
    });
});

/** Rellena el formulario con los datos del curso para modificarlo. */
function editarCurso(id) {
    const curso = cursos.find((c) => c.id === id);
    formCurso.elements.id.value = curso.id;
    formCurso.elements.name.value = curso.name;
    formCurso.elements.startDate.value = curso.startDate;
    formCurso.elements.endDate.value = curso.endDate;
    formCurso.querySelectorAll('#curso-asignaturas input').forEach((casilla) => {
        casilla.checked = curso.subjects.some((a) => a.id === Number(casilla.value));
    });
    document.getElementById('titulo-curso').textContent = 'Editar curso ' + curso.name;
    document.getElementById('cancelar-curso').hidden = false;
    formCurso.scrollIntoView({ behavior: 'smooth' });
}

function limpiarFormCurso() {
    formCurso.reset();
    formCurso.elements.id.value = '';
    document.getElementById('titulo-curso').textContent = 'Registrar curso académico';
    document.getElementById('cancelar-curso').hidden = true;
}

document.getElementById('cancelar-curso').addEventListener('click', limpiarFormCurso);

function eliminarCurso(id) {
    const curso = cursos.find((c) => c.id === id);
    if (!confirm(`¿Eliminar el curso ${curso.name}?`)) return;
    intentar(async () => {
        await api(`/courses/${id}`, 'DELETE');
        aviso(`Curso ${curso.name} eliminado`);
        await cargarTodo();
    });
}

// ---------------------------------------------------------------------
//  MATRÍCULAS E HISTORIAL
// ---------------------------------------------------------------------

/** El desplegable de asignaturas muestra solo las del curso elegido. */
function pintarAsignaturasMatricula() {
    const cursoId = Number(document.getElementById('matricula-curso').value);
    const curso = cursos.find((c) => c.id === cursoId);
    const lista = curso ? curso.subjects : [];
    document.getElementById('matricula-asignatura').innerHTML = lista.length
        ? lista.map((a) => `<option value="${a.id}">${esc(a.name)} (${DIFICULTADES[a.difficulty]})</option>`).join('')
        : '<option value="">El curso no tiene asignaturas</option>';
}

document.getElementById('matricula-curso').addEventListener('change', pintarAsignaturasMatricula);

document.getElementById('form-matricula').addEventListener('submit', (evento) => {
    evento.preventDefault();
    const form = evento.target;
    const matricula = {
        studentId: Number(form.elements.studentId.value),
        courseId: Number(form.elements.courseId.value),
        subjectId: Number(form.elements.subjectId.value),
        studyHours: Number(form.elements.studyHours.value),
        grade: form.elements.grade.value === '' ? null : Number(form.elements.grade.value),
    };
    intentar(async () => {
        await api('/enrollments', 'POST', matricula);
        aviso('Matrícula registrada');
        form.elements.studyHours.value = '';
        form.elements.grade.value = '';
        document.getElementById('historial-estudiante').value = form.elements.studentId.value;
        await cargarTodo();
    });
});

async function pintarHistorial() {
    const estudianteId = document.getElementById('historial-estudiante').value;
    const tabla = document.getElementById('tabla-historial');
    if (!estudianteId) {
        tabla.innerHTML = filaVacia(6, 'Selecciona un estudiante');
        return;
    }
    const historial = await api(`/students/${estudianteId}/enrollments`);
    tabla.innerHTML = historial.length
        ? historial.map((m) => `
            <tr>
                <td>${esc(m.course.name)}</td>
                <td>${esc(m.subject.name)} ${etiqueta(m.subject.difficulty)}</td>
                <td><input type="number" id="horas-${m.id}" class="compact" value="${m.studyHours}" min="0" step="0.5"></td>
                <td><input type="number" id="nota-${m.id}" class="compact" value="${m.grade ?? ''}" min="0" max="10" step="0.1" placeholder="—"></td>
                <td>${estado(m.grade)}</td>
                <td class="actions stack">
                    <button type="button" class="secondary small" onclick="guardarMatricula(${m.id})">Guardar</button>
                    <button type="button" class="danger small" onclick="eliminarMatricula(${m.id})">Eliminar</button>
                </td>
            </tr>`).join('')
        : filaVacia(6, 'Este estudiante no tiene matrículas');
}

document.getElementById('historial-estudiante').addEventListener('change', () => intentar(pintarHistorial));

/** Guarda las horas y la nota de una matrícula (la nota vacía significa "sin calificar"). */
function guardarMatricula(id) {
    const nota = document.getElementById('nota-' + id).value;
    const datos = {
        studyHours: Number(document.getElementById('horas-' + id).value),
        grade: nota === '' ? null : Number(nota),
    };
    intentar(async () => {
        await api(`/enrollments/${id}`, 'PUT', datos);
        aviso('Matrícula actualizada');
        await cargarTodo();
    });
}

function eliminarMatricula(id) {
    if (!confirm('¿Eliminar esta matrícula? Se perderá su nota.')) return;
    intentar(async () => {
        await api(`/enrollments/${id}`, 'DELETE');
        aviso('Matrícula eliminada');
        await cargarTodo();
    });
}

// ---------------------------------------------------------------------
//  INFORMES: descarga del fichero CSV o JSON
// ---------------------------------------------------------------------

document.getElementById('form-informe').addEventListener('submit', (evento) => {
    evento.preventDefault();
    const form = evento.target;
    const estudianteId = form.elements.studentId.value;
    const formato = form.elements.format.value;

    intentar(async () => {
        const respuesta = await fetch(`${API}/reports/students/${estudianteId}?format=${formato}`);
        if (!respuesta.ok) {
            const error = await respuesta.json().catch(() => ({}));
            throw new Error(error.error || 'No se pudo generar el informe');
        }
        // Se crea un enlace temporal para que el navegador descargue el fichero
        const fichero = await respuesta.blob();
        const enlace = document.createElement('a');
        enlace.href = URL.createObjectURL(fichero);
        enlace.download = `informe_estudiante_${estudianteId}.${formato}`;
        enlace.click();
        URL.revokeObjectURL(enlace.href);
        aviso('Informe descargado');
    });
});

// Al abrir la página se cargan los datos
intentar(cargarTodo);
