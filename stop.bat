@echo off
chcp 65001 >nul
title QuickSplit - Detener
cd /d "%~dp0"

echo Deteniendo QuickSplit...
docker compose down

echo.
echo QuickSplit detenido. Los datos se conservan en el volumen 'quicksplit-data'.
echo (Para borrar tambien los datos: docker compose down -v)
echo.
pause
