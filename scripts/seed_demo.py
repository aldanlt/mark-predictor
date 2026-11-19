#!/usr/bin/env python3
"""Carga datos de demostración a través de la API REST (sin dependencias externas).

Uso:
    python3 scripts/seed_demo.py                       # API en http://localhost:8080/api
    python3 scripts/seed_demo.py http://localhost:3000/api

Reproduce el ejemplo del enunciado de la Práctica 7: Ana cursa Algoritmos en 2024/2025 (3,5)
y lo repite en 2025/2026 (6,2), además de Ingeniería del Software (8,4).
"""
import json
import sys
import urllib.error
import urllib.request

API = (sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080/api").rstrip("/")


def call(method: str, path: str, body: dict | None = None):
    data = json.dumps(body).encode() if body is not None else None
    request = urllib.request.Request(API + path, data=data, method=method,
                                     headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(request) as response:
            raw = response.read()
            return json.loads(raw) if raw else None
    except urllib.error.HTTPError as error:
        message = json.loads(error.read() or b"{}").get("error", error.reason)
        raise SystemExit(f"{method} {path} -> {error.code}: {message}")


def get_or_create(path: str, key: str, body: dict) -> dict:
    existing = next((item for item in call("GET", path) if item[key] == body[key]), None)
    return existing or call("POST", path, body)


subjects = {
    name: get_or_create("/subjects", "name", {"name": name, "difficulty": difficulty})
    for name, difficulty in [
        ("Algoritmos", "DIFICIL"),
        ("Ingeniería del Software", "MEDIA"),
        ("Bases de Datos", "MEDIA"),
        ("Inglés Técnico", "FACIL"),
    ]
}
ids = {name: subject["id"] for name, subject in subjects.items()}

courses = {
    name: get_or_create("/courses", "name", {
        "name": name, "startDate": start, "endDate": end, "subjectIds": list(ids.values()),
    })
    for name, start, end in [
        ("2024/2025", "2024-09-01", "2025-07-31"),
        ("2025/2026", "2025-09-01", "2026-07-31"),
    ]
}

students = {s["name"]: s for s in call("GET", "/students")}

if "Ana Martínez" not in students:
    ana = call("POST", "/students", {
        "name": "Ana Martínez", "studyHours": 10, "attendance": 85, "previousGrade": 6.5,
        "courseId": courses["2024/2025"]["id"],
        "subjects": [{"subjectId": ids["Algoritmos"], "studyHours": 3}],
    })
    first = ana["enrollments"][0]
    call("PUT", f"/enrollments/{first['id']}", {"studyHours": 3, "grade": 3.5})
    for subject, hours, grade in [("Algoritmos", 6, 6.2), ("Ingeniería del Software", 5, 8.4)]:
        call("POST", "/enrollments", {
            "studentId": ana["id"], "subjectId": ids[subject], "courseId": courses["2025/2026"]["id"],
            "studyHours": hours, "grade": grade,
        })

if "Bruno López" not in students:
    call("POST", "/students", {
        "name": "Bruno López", "studyHours": 15, "attendance": 95, "previousGrade": 8,
        "courseId": courses["2025/2026"]["id"],
        "subjects": [
            {"subjectId": ids["Bases de Datos"], "studyHours": 8},
            {"subjectId": ids["Inglés Técnico"], "studyHours": 2},
        ],
    })

print(f"Datos de demostración cargados en {API}")
for student in call("GET", "/students"):
    print(f"  - {student['name']}: {len(student['enrollments'])} matrícula(s)")
