@echo off
chcp 65001 > nul
cd /d "%~dp0"
title Сборка мода Bedrock World Restorer для Fabric 1.21.3

echo ========================================================
echo   Сборка мода Bedrock World Restorer (Fabric 1.21.3)
echo ========================================================
echo.

echo [1/3] Проверка установленной Java...
java -version
if %errorlevel% neq 0 (
    echo.
    echo [ОШИБКА] Java не найдена! Установите JDK 21: https://adoptium.net/temurin/releases/?version=21
    pause
    exit /b 1
)

echo.
echo [2/3] Подготовка Gradle 8.10.2...
set "GRADLE_BAT=%~dp0temp_gradle\gradle-8.10.2\bin\gradle.bat"

if not exist "%GRADLE_BAT%" (
    echo Загрузка Gradle с зеркала Tencent Cloud...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $null = New-Item -ItemType Directory -Force -Path 'temp_gradle'; Write-Host 'Скачивание архива Gradle...'; Invoke-WebRequest -Uri 'https://mirrors.cloud.tencent.com/gradle/gradle-8.10.2-bin.zip' -OutFile 'temp_gradle\gradle.zip'; Write-Host 'Распаковка архива...'; Expand-Archive -Path 'temp_gradle\gradle.zip' -DestinationPath 'temp_gradle' -Force; Remove-Item 'temp_gradle\gradle.zip' -Force; Write-Host 'Gradle готов!' -ForegroundColor Green"
) else (
    echo Gradle 8.10.2 уже найден в папке temp_gradle.
)

if not exist "%GRADLE_BAT%" (
    echo.
    echo [ОШИБКА] Не удалось подготовить Gradle. Проверьте интернет.
    pause
    exit /b 1
)

:: Очистка старых кэшей сбоев
if exist ".gradle\loom-cache" (
    rmdir /s /q ".gradle\loom-cache" 2>nul
)

echo.
echo [3/3] Компиляция Fabric-мода 1.21.3...
call "%GRADLE_BAT%" build

echo.
echo ========================================================
if exist "%~dp0build\libs\bedrock_restorer-1.0.0.jar" (
    echo   [УСПЕХ] Мод успешно скомпилирован!
    echo   Файл: build\libs\bedrock_restorer-1.0.0.jar
    echo   Скопируйте его в папку .minecraft\mods
) else (
    echo   [ПРЕДУПРЕЖДЕНИЕ] Сборка не завершена. Проверьте вывод выше.
)
echo ========================================================
echo.
pause
