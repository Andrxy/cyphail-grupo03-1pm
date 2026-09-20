@echo off
setlocal
REM Lanzador de Cyphail para Windows.
REM Ejecuta el jar ejecutable generado por "mvnw.cmd clean package".

set "PROJECT_DIR=%~dp0"
if "%PROJECT_DIR:~-1%"=="\" set "PROJECT_DIR=%PROJECT_DIR:~0,-1%"

set "CYPHAIL_JAR=%~dp0cli\target\cyphail.jar"

if not exist "%CYPHAIL_JAR%" (
    echo No se encontro "%CYPHAIL_JAR%".
    echo Compila primero el proyecto con: mvnw.cmd clean package
    exit /b 1
)

java -Dcyphail.home="%PROJECT_DIR%" -jar "%CYPHAIL_JAR%" %*