from fastapi.testclient import TestClient

from main import app, formula_base, limitar

client = TestClient(app)


def test_formula_base():
    assert formula_base(10, 80, 6) == 59  # 25 + 16 + 18


def test_limitar_a_la_escala():
    assert limitar(120) == 100
    assert limitar(-5) == 0


def test_ejemplo_del_enunciado_coeficientes():
    # 8 h, 100 % de asistencia y un 10 de nota previa dan una base de 70 puntos
    datos = {"study_hours": 8, "attendance": 100, "previous_grade": 10}
    assert client.post("/predict", json={**datos, "difficulty": "FACIL"}).json()["score"] == 84
    assert client.post("/predict", json={**datos, "difficulty": "MEDIA"}).json()["score"] == 70
    assert client.post("/predict", json={**datos, "difficulty": "DIFICIL"}).json()["score"] == 56


def test_prediccion_general_sin_dificultad():
    respuesta = client.post("/predict", json={"study_hours": 10, "attendance": 80, "previous_grade": 6})
    assert respuesta.json() == {"base": 59, "coefficient": 1.0, "score": 59}


def test_nunca_supera_100():
    datos = {"study_hours": 40, "attendance": 100, "previous_grade": 10, "difficulty": "FACIL"}
    assert client.post("/predict", json=datos).json()["score"] == 100


def test_dificultad_no_valida():
    datos = {"study_hours": 1, "attendance": 1, "previous_grade": 1, "difficulty": "IMPOSIBLE"}
    assert client.post("/predict", json=datos).status_code == 400
