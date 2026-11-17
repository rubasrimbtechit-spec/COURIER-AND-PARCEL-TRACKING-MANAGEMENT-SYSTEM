@echo off
setlocal
if not exist "target\courier-parcel-tracking-1.0.0.jar" (
  echo Building executable JAR...
  call mvn clean package
  if errorlevel 1 (
    echo Build failed. Install JDK 17+ and Maven, then retry.
    exit /b 1
  )
)
java -jar "target\courier-parcel-tracking-1.0.0.jar"
endlocal
