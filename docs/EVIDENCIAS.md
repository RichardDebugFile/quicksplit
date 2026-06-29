# Checklist de evidencias (para el documento final)

Marca cada captura cuando la tengas. Estas cubren los 4 entregables de la práctica.

## Entregable 1 — Repo público
- [ ] URL del repositorio público en GitHub.
- [ ] Captura de la página principal del repo (con README visible).

## Entregable 2 — Qué herramienta usó y por qué
- [ ] Sección 2 de [SAST.md](SAST.md) (ya redactada: SonarQube y la justificación).

## Entregable 3 — Evidencias de la ejecución del pipeline
- [ ] **Actions → CI** en verde (jobs *Backend*, *Frontend*, *Docker*).
- [ ] **Actions → SAST - SonarQube** en verde, mostrando los pasos:
      build backend/frontend → *SonarQube Scan* → *Publicar artefacto*.
- [ ] **Dashboard de SonarCloud** del proyecto (Quality Gate + métricas).
- [ ] Captura del **secret `SONAR_TOKEN`** configurado (Settings → Secrets, sin mostrar el valor).

## Entregable 4 — Retroalimentación de la herramienta SAST (artefacto)
- [ ] Descargar el artefacto **`sast-sonarqube-report`** del run.
- [ ] Captura de la lista de *Issues* / *Security Hotspots* en SonarCloud.
- [ ] Completar la tabla de hallazgos en la sección 6 de [SAST.md](SAST.md).

---

### Comandos útiles para evidencias locales

```bash
# Cobertura backend (HTML)
cd backend && mvn verify
# -> abrir backend/target/site/jacoco/index.html

# Cobertura frontend
cd frontend && npm run coverage

# Levantar todo y probar la app
start.bat   # (Windows) o: docker compose up --build
```
