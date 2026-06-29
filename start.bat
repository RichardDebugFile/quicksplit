@echo off
chcp 65001 >nul
title QuickSplit - Inicio
cd /d "%~dp0"

echo ===============================================
echo    QuickSplit - Levantando la aplicacion
echo ===============================================
echo.

REM 1) Verificar que Docker este corriendo
docker info >nul 2>&1
if errorlevel 1 (
  echo [ERROR] Docker no esta corriendo.
  echo         Abre Docker Desktop, espera a que inicie y vuelve a ejecutar este archivo.
  echo.
  pause
  exit /b 1
)

REM 2) Construir e iniciar todos los servicios (db + backend + frontend)
echo Construyendo e iniciando contenedores...
echo (La primera vez puede tardar varios minutos mientras se descargan imagenes.)
echo.
docker compose up --build -d
if errorlevel 1 (
  echo.
  echo [ERROR] No se pudieron iniciar los contenedores.
  pause
  exit /b 1
)

REM 3) Esperar a que el backend responda en su healthcheck
echo.
echo Esperando a que el backend este listo...
set /a tries=0
:waitloop
timeout /t 3 /nobreak >nul
for /f %%i in ('curl -s -o nul -w "%%{http_code}" http://localhost:8080/actuator/health 2^>nul') do set code=%%i
if "%code%"=="200" goto ready
set /a tries+=1
if %tries% lss 40 (
  goto waitloop
)
echo [AVISO] El backend tardo mas de lo esperado. Revisa 'docker compose logs backend'.

:ready
echo.
echo ===============================================
echo    QuickSplit esta arriba!
echo.
echo    Aplicacion : http://localhost:8088
echo    API REST   : http://localhost:8080
echo    Swagger UI : http://localhost:8080/swagger-ui.html
echo ===============================================
echo.

REM 4) Abrir la app en el navegador
start "" http://localhost:8088

echo Para detener todo, ejecuta stop.bat
echo.
pause
