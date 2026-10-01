@echo off
REM ─────────────────────────────────────────────────────────────
REM Demarre le systeme legacy RH (serveur SOAP) en arriere-plan.
REM Le WSDL sera disponible a :
REM     http://localhost:8080/ws/hr-service.wsdl
REM ─────────────────────────────────────────────────────────────
setlocal
set JAR=D:\Projet\ApiSOAPProject\legacy-hr-system\target\legacy-hr-system-1.0.0.jar
if not exist "%JAR%" (
  echo [ERREUR] JAR introuvable : %JAR%
  echo Lancez d'abord :  mvn package -DskipTests
  pause
  exit /b 1
)

REM Arret d'une instance deja lancee sur le port 8080
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
  echo Arret du processus %%p sur le port 8080...
  taskkill /F /PID %%p >nul 2>&1
)

echo Demarrage de legacy-hr-system...
start "legacy-hr-system" /min cmd /c "java -jar "%JAR%" > "%~dp0legacy-hr-system.log" 2>&1"

REM Attente active que le WSDL reponde
set /a tries=0
:wait
timeout /t 2 /nobreak >nul
set /a tries+=1
curl -s -o NUL -w "" http://localhost:8080/ws/hr-service.wsdl 2>nul
if %tries% lss 45 (
  goto check
) else (
  echo   ... en attente (%tries%)
  goto wait
)

:check
curl -s -o NUL -w "%%{http_code}" http://localhost:8080/ws/hr-service.wsdl > "%~dp0wsdl_code.txt" 2>nul
set /p CODE=<"%~dp0wsdl_code.txt"
del "%~dp0wsdl_code.txt" >nul 2>&1

if "%CODE%"=="200" (
  echo.
  echo [OK] Serveur demarre - WSDL : http://localhost:8080/ws/hr-service.wsdl
  echo [OK] Journal : %~dp0legacy-hr-system.log
) else (
  echo.
  echo [KO] Le serveur n'a pas repondu ^(code HTTP %CODE%^).
  echo Consultez le journal : %~dp0legacy-hr-system.log
  pause
)
endlocal
