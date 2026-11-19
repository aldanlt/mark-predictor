// =====================================================================
//  Student Performance Predictor - Frontend
//  Todas las operaciones se hacen llamando a la API REST del backend Java.
//  (Nginx redirige las rutas /api/... al backend.)
// =====================================================================

const API = '/api';

const DIFICULTADES = { FACIL: 'Fácil', MEDIA: 'Media', DIFICIL: 'Difícil' };

// Datos cargados del backend (se recargan después de cada cambio)
let asignaturas = [];
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

/** Barra horizontal que representa una puntuación de 0 a 100. */
function barra(puntos) {
    return `<div class="score-bar"><span style="width:${puntos}%"></span></div>`;
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
    estudiantes = await api('/students');
    pintarAsignaturas();
    pintarCasillasAsignaturas();
    pintarEstudiantes();
}

function pintarAsignaturas() {
    document.getElementById('tabla-asignaturas').innerHTML = asignaturas.length
        ? asignaturas.map((a) => `<tr><td>${esc(a.name)}</td><td>${etiqueta(a.difficulty)}</td></tr>`).join('')
        : filaVacia(2, 'Todavía no hay asignaturas registradas');
}

function pintarEstudiantes() {
    document.getElementById('tabla-estudiantes').innerHTML = estudiantes.length
        ? estudiantes.map((e) => `
            <tr>
                <td>${esc(e.name)}</td>
                <td>${e.attendance} %</td>
                <td>${e.previousGrade}</td>
                <td><div class="chips">${e.enrollments.map((m) =>
                    `<span class="chip">${esc(m.subject.name)} · ${m.studyHours} h</span>`).join('')}</div></td>
                <td class="actions">
                    <button type="button" class="secondary small" onclick="verPrediccion(${e.id})">Predicción</button>
                </td>
            </tr>`).join('')
        : filaVacia(5, 'Todavía no hay estudiantes registrados');
}

// ---------------------------------------------------------------------
//  ESTUDIANTES: registro con asignaturas (checkboxes) y predicciones
// ---------------------------------------------------------------------

/** Una casilla y un campo de horas por cada asignatura. */
function pintarCasillasAsignaturas() {
    const contenedor = document.getElementById('estudiante-asignaturas');
    if (!asignaturas.length) {
        contenedor.innerHTML = '<p class="hint">Registra primero alguna asignatura.</p>';
        return;
    }
    contenedor.innerHTML = asignaturas.map((a) => `
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
        const porAsignatura = await api(`/students/${estudianteId}/predictions`);
        const estudiante = estudiantes.find((e) => e.id === estudianteId);

        const filas = porAsignatura.map((p) => `
            <tr>
                <td>${esc(p.subject)}</td>
                <td>${etiqueta(p.difficulty)}</td>
                <td>${p.studyHours}</td>
                <td>${p.base}</td>
                <td>× ${p.coefficient}</td>
                <td><strong>${p.score}</strong></td>
                <td>${barra(p.score)}</td>
            </tr>`).join('');

        const panel = document.getElementById('panel-prediccion');
        panel.innerHTML = `
            <h2>Predicción de ${esc(estudiante.name)}</h2>
            <p class="hint">Predicción general (escala 0-100)</p>
            <p class="score-big">${general.score}</p>
            <h3>Predicción por asignatura</h3>
            ${porAsignatura.length ? `
            <table>
                <thead><tr><th>Asignatura</th><th>Dificultad</th><th>Horas</th><th>Base</th>
                <th>Coef.</th><th>Predicción</th><th></th></tr></thead>
                <tbody>${filas}</tbody>
            </table>` : '<p class="empty">No está matriculado en ninguna asignatura.</p>'}`;
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

// Al abrir la página se cargan los datos
intentar(cargarTodo);
