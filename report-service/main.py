"""Microservicio de informes académicos (FastAPI) - P7 · HU-05.

Recibe los datos con SU formato (student, records, name, academic_year, score)
y devuelve el contenido final del informe en CSV o JSON.
"""
import csv
import io
import json

from fastapi import FastAPI, HTTPException, Response
from pydantic import BaseModel

app = FastAPI(title="Servicio de informes")


class Estudiante(BaseModel):
    id: int
    name: str


class Registro(BaseModel):
    name: str            # asignatura
    academic_year: str   # curso académico
    study_hours: float
    score: float | None = None  # calificación (puede no existir)


class DatosInforme(BaseModel):
    student: Estudiante
    records: list[Registro]


def generar_csv(datos: DatosInforme) -> str:
    salida = io.StringIO()
    writer = csv.writer(salida)
    writer.writerow(["id_estudiante", "estudiante", "asignatura", "curso", "horas_estudio", "calificacion"])
    for r in sorted(datos.records, key=lambda r: r.academic_year):
        writer.writerow([datos.student.id, datos.student.name, r.name, r.academic_year,
                         r.study_hours, "" if r.score is None else r.score])
    return salida.getvalue()


def generar_json(datos: DatosInforme) -> str:
    registros = sorted(datos.records, key=lambda r: r.academic_year)
    informe = {
        "student": datos.student.model_dump(),
        "records": [r.model_dump() for r in registros],
    }
    return json.dumps(informe, ensure_ascii=False, indent=2)


@app.post("/reports/{formato}")
def generar_informe(formato: str, datos: DatosInforme):
    if formato == "csv":
        return Response(generar_csv(datos), media_type="text/csv; charset=utf-8")
    if formato == "json":
        return Response(generar_json(datos), media_type="application/json; charset=utf-8")
    raise HTTPException(status_code=400, detail="Formato no admitido: usa csv o json")
