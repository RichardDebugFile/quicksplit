# QuickSplit · divide gastos en grupo sin dramas

QuickSplit es una aplicación full‑stack para **registrar gastos compartidos en un grupo** y
calcular automáticamente **el menor número de pagos necesarios para saldar todas las deudas**
(simplificación de deudas / *minimum cash flow*).

> Proyecto académico (Procesos de Software). El objetivo principal es montar un **pipeline de CI
> con una herramienta SAST** integrada: ver **[docs/SAST.md](docs/SAST.md)**.

---

## ✨ ¿Qué lo hace interesante?

El corazón de la app es un **algoritmo de grafos**: a partir del balance neto de cada miembro
(lo que pagó menos lo que le toca pagar), produce un plan de transferencias mínimo. En vez de
"cada quien le paga a cada quien", QuickSplit dice exactamente *quién le paga cuánto a quién* con
la menor cantidad de movimientos. Ej.: si A le debe a B y B le debe lo mismo a C, el resultado es
**una sola** transferencia A → C.

---

## 🧱 Arquitectura

```
┌────────────┐      /api      ┌──────────────┐      JDBC      ┌────────────┐
│  Frontend  │  ───────────▶  │   Backend    │  ───────────▶  │ PostgreSQL │
│ React + TS │   (proxy)      │ Spring Boot  │                │            │
│  (nginx)   │  ◀───────────  │  REST + JWT  │  ◀───────────  │            │
└────────────┘                └──────────────┘                └────────────┘
   :8088                          :8080                           :5432
```

- **Backend** — Java 17 + Spring Boot 3.2 (Web, Security/JWT, Data JPA, Validation, Actuator,
  OpenAPI). PostgreSQL en producción, H2 en memoria para tests.
- **Frontend** — React 18 + TypeScript + Vite, React Router, Axios. nginx sirve la SPA y hace
  proxy de `/api` al backend.
- **Infra** — Docker multi‑stage para cada servicio + `docker-compose` (db + backend + frontend).
- **Calidad** — JUnit 5 / Mockito / MockMvc (backend), Vitest / Testing Library (frontend),
  JaCoCo, Checkstyle, ESLint.
- **CI/CD** — GitHub Actions (build + test + docker) y **SonarQube (SAST)**.

---

## 🚀 Cómo ejecutarlo

### Opción 1 — Un solo clic (Windows + Docker Desktop)

```
doble clic en  start.bat
```

Construye e inicia todo, espera a que el backend esté sano y abre el navegador en
`http://localhost:8088`. Para detener: `stop.bat`.

### Opción 2 — Docker Compose

```bash
docker compose up --build      # levantar
docker compose down            # detener
```

| Servicio  | URL                                   |
|-----------|---------------------------------------|
| App web   | http://localhost:8088                 |
| API REST  | http://localhost:8080/api             |
| Swagger   | http://localhost:8080/swagger-ui.html |
| Health    | http://localhost:8080/actuator/health |

### Opción 3 — Desarrollo local (sin Docker)

```bash
# Backend (usa H2 en memoria por defecto)
cd backend && mvn spring-boot:run

# Frontend (en otra terminal)
cd frontend && npm install && npm run dev   # http://localhost:5173
```

---

## 📚 API principal

| Método | Endpoint                               | Descripción                              |
|--------|----------------------------------------|------------------------------------------|
| POST   | `/api/auth/register`                   | Crear cuenta (devuelve JWT)              |
| POST   | `/api/auth/login`                      | Iniciar sesión (devuelve JWT)            |
| GET    | `/api/auth/me`                         | Usuario autenticado                      |
| POST   | `/api/groups`                          | Crear grupo                              |
| GET    | `/api/groups`                          | Mis grupos                               |
| GET    | `/api/groups/{id}`                     | Detalle + miembros                       |
| POST   | `/api/groups/{id}/members`             | Agregar miembro por email                |
| POST   | `/api/groups/{id}/expenses`            | Registrar gasto (split EQUAL o EXACT)    |
| GET    | `/api/groups/{id}/expenses`            | Listar gastos                            |
| GET    | `/api/groups/{id}/settlement`          | **Balances + plan de pagos mínimo**      |

Todas excepto `register`/`login` requieren `Authorization: Bearer <token>`.

---

## 🧮 El algoritmo de simplificación de deudas

`SettlementCalculator` resuelve el *minimum cash flow problem* con una estrategia voraz:

1. Calcula el **balance neto** de cada miembro (pagó − le toca).
2. Separa **acreedores** (balance +) y **deudores** (balance −) en dos colas de prioridad.
3. En cada paso empareja al **mayor acreedor** con el **mayor deudor** y salda el mínimo de ambos.
4. Repite hasta saldar todo.

Garantiza **≤ n − 1 transferencias** y trabaja con `BigDecimal` para no perder centavos. Ver
[`SettlementCalculator.java`](backend/src/main/java/com/quicksplit/settlement/SettlementCalculator.java)
y sus pruebas
[`SettlementCalculatorTest.java`](backend/src/test/java/com/quicksplit/settlement/SettlementCalculatorTest.java).

---

## ✅ Tests

```bash
# Backend: 15 unitarios + 5 de integración, cobertura JaCoCo
cd backend && mvn verify
#  -> reporte en backend/target/site/jacoco/index.html

# Frontend
cd frontend && npm run coverage
```

---

## 🔁 CI/CD

Dos workflows en `.github/workflows/`:

- **`ci.yml`** — compila y testea backend (`mvn verify`) y frontend (`lint`, `coverage`, `build`),
  valida las imágenes Docker y publica artefactos (JAR, cobertura, `dist`).
- **`sonarcloud.yml`** — **análisis SAST con SonarQube** sobre Java + TypeScript, con cobertura, y
  publica la retroalimentación como artefacto.

La configuración del SAST y cómo reproducirlo está en **[docs/SAST.md](docs/SAST.md)**.

---

## 🔐 Variables de entorno (backend)

| Variable                     | Por defecto                  | Descripción                          |
|------------------------------|------------------------------|--------------------------------------|
| `SPRING_DATASOURCE_URL`      | `jdbc:postgresql://db:5432/quicksplit` | Cadena JDBC (perfil prod)  |
| `SPRING_DATASOURCE_USERNAME` | `quicksplit`                 | Usuario de la BD                     |
| `SPRING_DATASOURCE_PASSWORD` | `quicksplit`                 | Contraseña de la BD                  |
| `QUICKSPLIT_JWT_SECRET`      | *(obligatoria en prod)*      | Secreto JWT (≥ 32 caracteres)        |
| `CORS_ALLOWED_ORIGINS`       | `http://localhost:8088`      | Orígenes permitidos                  |

---

## 📂 Estructura

```
QuickSplit/
├── backend/            # API Spring Boot (Java 17)
├── frontend/           # SPA React + TypeScript (Vite)
├── .github/workflows/  # CI (ci.yml) y SAST (sonarcloud.yml)
├── docs/               # Documentación y SAST
├── docker-compose.yml  # db + backend + frontend
├── sonar-project.properties
├── start.bat / stop.bat
└── README.md
```

## 📄 Licencia

MIT.
