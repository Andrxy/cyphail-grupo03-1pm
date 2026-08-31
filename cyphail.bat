@echo off
setlocal
REM Lanzador de Cyphail para Windows.
REM Ejecuta el jar ejecutable generado por "mvnw.cmd clean package".

set "CYPHAIL_JAR=%~dp0cli\target\cyphail.jar"

if not exist "%CYPHAIL_JAR%" (
    echo No se encontro "%CYPHAIL_JAR%".
    echo Compila primero el proyecto con: mvnw.cmd clean package
    exit /b 1
)

java -jar "%CYPHAIL_JAR%" %*
