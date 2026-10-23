@echo off
setlocal
if not exist gradlew.bat (
  echo ERROR: gradlew.bat was not found.
  echo Copy build.gradle, gradle.properties, and the src folder into a Forge 1.12.2 MDK folder first.
  pause
  exit /b 1
)
java -version 2>&1 | findstr /C:"1.8." >nul
if errorlevel 1 (
  echo WARNING: This terminal may not be using Java 8.
  echo Forge 1.12.2 build tooling expects JDK 8. Set JAVA_HOME and PATH to JDK 8 first.
)
call gradlew.bat build
if errorlevel 1 (
  echo.
  echo Build failed. Check Java 8 and ForgeGradle dependency downloads.
  pause
  exit /b 1
)
echo.
echo Build complete. Look in build\libs for DeathEvolution-1.1.0.jar
pause
