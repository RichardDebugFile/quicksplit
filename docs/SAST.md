# SAST en el pipeline de CI — QuickSplit

> Entregable de la práctica: *"Seleccione una herramienta SAST y añádala a su pipeline de CI"*.

---

## 1. ¿Qué es SAST?

**SAST** (*Static Application Security Testing*) es el análisis **estático** del código fuente:
revisa el código **sin ejecutarlo** para detectar vulnerabilidades y malas prácticas
(inyección SQL, XSS, credenciales en duro, deserialización insegura, manejo débil de
contraseñas, etc.), además de *bugs* y *code smells*. Se contrapone a:

- **SCA** (*Software Composition Analysis*) — analiza **dependencias** de terceros (ej. OWASP
  Dependency‑Check).
- **DAST** (*Dynamic*) — analiza la aplicación **en ejecución** (ej. OWASP ZAP).

---

## 2. Herramienta elegida: **SonarQube (SonarQube Cloud / SonarCloud)**

**SonarQube** es una plataforma SAST que analiza el código y reporta:

| Categoría            | Qué detecta                                                |
|----------------------|------------------------------------------------------------|
| **Vulnerabilities**  | Fallos de seguridad explotables en el código               |
| **Security Hotspots**| Código sensible que requiere revisión manual de seguridad  |
| **Bugs**             | Errores de lógica que pueden fallar en ejecución           |
| **Code Smells**      | Mantenibilidad / deuda técnica                             |
| **Coverage**         | Cobertura de pruebas (integra JaCoCo + lcov)               |
| **Duplications**     | Código duplicado                                           |

Usamos **SonarCloud** (la versión SaaS), **gratuita para proyectos open source públicos**, que es
justo el caso de este repositorio.

### ¿Por qué SonarQube y no las otras herramientas asignadas?

| Herramienta              | Tipo  | Decisión |
|--------------------------|-------|----------|
| **SonarQube** ✅          | SAST  | **Elegida.** SAST real, analiza **Java y TypeScript** en un solo proyecto, gratis para OSS público, integración nativa con GitHub Actions, *quality gate* + dashboard web claro como evidencia. |
| Checkmarx                | SAST  | Comercial, requiere licencia de pago → inviable "con nuestros recursos". |
| Snyk                     | SAST/SCA | Buena opción gratis, pero su fuerte es SCA; menos completo como SAST puro que SonarQube. |
| OWASP Dependency‑Check   | **SCA** | Escanea *dependencias*, **no** el código propio → no cumple la definición de SAST. |
| OWASP ZAP                | **DAST** | Escanea la app *corriendo* → es análisis dinámico, **no** estático. |

En resumen: entre las herramientas asignadas, **SonarQube es la que mejor cumple la definición de
SAST**, es gratuita para nuestro caso y produce evidencia/retroalimentación clara.

---

## 3. Cómo está integrado en el pipeline

Workflow: [`.github/workflows/sonarcloud.yml`](../.github/workflows/sonarcloud.yml)
Configuración: [`sonar-project.properties`](../sonar-project.properties)

El job hace, en orden:

1. **Checkout** del repo (con historial completo).
2. **Compila el backend** con `mvn verify` → genera los `.class` (que SonarQube necesita para Java)
   y el reporte de cobertura **JaCoCo** (`jacoco.xml`).
3. **Instala el frontend** y corre `npm run coverage` → genera **lcov** para la cobertura de TS.
4. **SonarQube Scan** (`SonarSource/sonarqube-scan-action`) — analiza `backend/src` (Java) y
   `frontend/src` (TypeScript) y sube los resultados a SonarCloud.
5. **Exporta la retroalimentación** vía la API de SonarCloud a `sonar-report/`
   (issues, *security hotspots* y estado del *quality gate*) y la **publica como artefacto**
   `sast-sonarqube-report` (este es el "artefacto de retroalimentación" del entregable).

```
push/PR ─▶ [build backend+frontend con cobertura] ─▶ [SonarQube Scan] ─▶ [dashboard + artefacto SAST]
```

---

## 4. Cómo configurarlo (reproducible)

> Solo hay que hacerlo una vez. El repositorio ya trae el workflow y la config listos.

1. Entra a **https://sonarcloud.io** e inicia sesión **con tu cuenta de GitHub**.
2. **+ → Analyze new project** e importa el repositorio `quicksplit`.
3. Anota tu **Organization Key** y tu **Project Key**.
4. En **`sonar-project.properties`** reemplaza:
   ```properties
   sonar.organization=TU_ORG_SONARCLOUD
   sonar.projectKey=TU_ORG_SONARCLOUD_quicksplit
   ```
5. En SonarCloud, en el proyecto → **Administration → Analysis Method**: **desactiva
   "Automatic Analysis"** (usaremos el análisis desde CI).
6. Genera un token: **My Account → Security → Generate Token**.
7. En GitHub: **Settings → Secrets and variables → Actions → New repository secret**:
   - Nombre: `SONAR_TOKEN`
   - Valor: el token generado.
8. Haz `push` a `main`. El workflow **SAST - SonarQube** correrá automáticamente.

---

## 5. Evidencias del pipeline (para el documento)

Capturar / adjuntar:

1. **Ejecución del pipeline** — pestaña **Actions** del repo → workflow *SAST - SonarQube* en verde,
   con sus pasos (build, scan, artefacto).
2. **Dashboard de SonarCloud** — `https://sonarcloud.io/project/overview?id=<projectKey>`
   mostrando Vulnerabilities / Hotspots / Bugs / Coverage / Quality Gate.
3. **Artefacto de retroalimentación** — en la página del run, sección *Artifacts* →
   `sast-sonarqube-report` (contiene `sonar-issues.json`, `security-hotspots.json`,
   `quality-gate.json`, `RESUMEN.txt`).

En [docs/EVIDENCIAS.md](EVIDENCIAS.md) hay una checklist con los pantallazos exactos a tomar.

---

## 6. Retroalimentación de la herramienta (resultados)

> Esta sección se completa **después** de la primera corrida con los hallazgos reales.

- **Quality Gate:** _(Passed / Failed)_
- **Vulnerabilities:** _N_
- **Security Hotspots:** _N_
- **Bugs:** _N_
- **Code Smells:** _N_
- **Coverage:** _XX %_

### Hallazgos principales y acciones

| # | Tipo | Archivo | Descripción | Acción tomada |
|---|------|---------|-------------|---------------|
| 1 |      |         |             |               |
| 2 |      |         |             |               |

> El detalle completo y exportable está en el artefacto `sast-sonarqube-report` y en el dashboard.

---

## 7. Alternativa sin cuenta: SonarQube self‑hosted (opcional)

Si no se puede usar SonarCloud, se puede levantar SonarQube Community en local con Docker y
apuntar el scanner a `http://localhost:9000`:

```bash
docker run -d --name sonarqube -p 9000:9000 sonarqube:community
# luego: mvn sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=<token>
```
