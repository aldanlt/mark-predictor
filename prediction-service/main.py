"""Microservicio de predicciones (FastAPI).

Fórmula original (escala 0-100):
    50 % horas de estudio (a partir de 20 h/semana cuenta como el máximo)
    20 % asistencia
    30 % nota previa

Predicción por asignatura (P6 · HU-02): se multiplica por el coeficiente de dificultad
y el resultado se limita al rango 0-100.
"""
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

app = FastAPI(title="Servicio de predicciones")

COEFICIENTES = {"FACIL": 1.2, "MEDIA": 1.0, "DIFICIL": 0.8}


class DatosPrediccion(BaseModel):
    study_hours: float = Field(ge=0)
    attendance: float = Field(ge=0, le=100)
    previous_grade: float = Field(ge=0, le=10)
    difficulty: str | None = None  # si no se envía, es la predicción general


def formula_base(horas: float, asistencia: float, nota_previa: float) -> float:
    return min(horas, 20) / 20 * 50 + asistencia / 100 * 20 + nota_previa / 10 * 30


def limitar(valor: float) -> float:
    """Mantiene el resultado dentro de la escala 0-100."""
    return max(0, min(100, valor))


@app.post("/predict")
def predecir(datos: DatosPrediccion):
    base = round(formula_base(datos.study_hours, datos.attendance, datos.previous_grade), 2)

    if datos.difficulty is None:
        coeficiente = 1.0
    elif datos.difficulty in COEFICIENTES:
        coeficiente = COEFICIENTES[datos.difficulty]
    else:
        raise HTTPException(status_code=400, detail="Dificultad no válida: usa FACIL, MEDIA o DIFICIL")

    return {"base": base, "coefficient": coeficiente, "score": round(limitar(base * coeficiente), 2)}
