import json

from fastapi.testclient import TestClient

from main import app

client = TestClient(app)

DATOS = {
    "student": {"id": 1, "name": "Ana"},
    "records": [
        {"name": "Algoritmos", "academic_year": "2025/2026", "study_hours": 6, "score": 6.2},
        {"name": "Algoritmos", "academic_year": "2024/2025", "study_hours": 3, "score": 3.5},
        {"name": "Ingeniería del Software", "academic_year": "2025/2026", "study_hours": 5},
    ],
}


def test_csv_con_historial_ordenado():
    lineas = client.post("/reports/csv", json=DATOS).text.strip().splitlines()
    assert lineas[0] == "id_estudiante,estudiante,asignatura,curso,horas_estudio,calificacion"
    assert lineas[1] == "1,Ana,Algoritmos,2024/2025,3.0,3.5"
    assert lineas[3] == "1,Ana,Ingeniería del Software,2025/2026,5.0,"


def test_json():
    informe = json.loads(client.post("/reports/json", json=DATOS).text)
    assert informe["student"]["name"] == "Ana"
    assert len(informe["records"]) == 3


def test_formato_no_admitido():
    assert client.post("/reports/pdf", json=DATOS).status_code == 400
