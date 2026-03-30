@echo off
SETLOCAL ENABLEDELAYEDEXPANSION
title Social Media API - Setup and Run
color 0A

echo.
echo ============================================================
echo     SOCIAL MEDIA API - AUTO SETUP ^& LAUNCHER
echo ============================================================
echo.

REM ============================================================
REM STEP 1: FIND MYSQL ON THIS PC
REM ============================================================
echo [1/5] Searching for MySQL installation...

SET MYSQL_BIN=

REM Check common MySQL install locations
FOR %%V IN (9.2 9.1 9.0 8.4 8.3 8.2 8.1 8.0 5.7) DO (
    IF EXIST "C:\Program Files\MySQL\MySQL Server %%V\bin\mysql.exe" (
        SET MYSQL_BIN=C:\Program Files\MySQL\MySQL Server %%V\bin
        echo      Found MySQL %%V at !MYSQL_BIN!
        GOTO :MYSQL_FOUND
    )
    IF EXIST "C:\Program Files (x86)\MySQL\MySQL Server %%V\bin\mysql.exe" (
        SET MYSQL_BIN=C:\Program Files (x86)\MySQL\MySQL Server %%V\bin
        echo      Found MySQL %%V at !MYSQL_BIN!
        GOTO :MYSQL_FOUND
    )
)

REM Check if mysql is already on PATH
WHERE mysql >nul 2>&1
IF %ERRORLEVEL% == 0 (
    echo      Found MySQL on system PATH
    SET MYSQL_BIN=
    GOTO :MYSQL_FOUND
)

REM MySQL not found
echo.
echo  [ERROR] MySQL was NOT found on this computer.
echo.
echo  Please do ONE of the following:
echo.
echo  Option A - Download and install MySQL:
echo    https://dev.mysql.com/downloads/installer/
echo    Choose "Developer Default" during setup.
echo.
echo  Option B - If MySQL IS installed but not found,
echo    edit this script and set MYSQL_BIN manually:
echo    e.g.  SET MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.0\bin
echo.
pause
EXIT /B 1

:MYSQL_FOUND

REM ============================================================
REM STEP 2: ASK FOR MYSQL ROOT PASSWORD
REM ============================================================
echo.
echo [2/5] MySQL credentials needed...
echo.
SET /P DB_PASSWORD=     Enter your MySQL root password (press Enter if none): 

REM ============================================================
REM STEP 3: CREATE DATABASE
REM ============================================================
echo.
echo [3/5] Creating database "socialmedia_db"...
echo.

IF "%MYSQL_BIN%"=="" (
    SET MYSQL_CMD=mysql
) ELSE (
    SET MYSQL_CMD="!MYSQL_BIN!\mysql.exe"
)

IF "%DB_PASSWORD%"=="" (
    !MYSQL_CMD! -u root -e "CREATE DATABASE IF NOT EXISTS socialmedia_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>nul
) ELSE (
    !MYSQL_CMD! -u root -p%DB_PASSWORD% -e "CREATE DATABASE IF NOT EXISTS socialmedia_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>nul
)

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo  [ERROR] Could not connect to MySQL.
    echo.
    echo  Possible reasons:
    echo    - Wrong password entered
    echo    - MySQL service is not running
    echo    - MySQL port 3306 is blocked
    echo.
    echo  To start MySQL service, open a NEW Command Prompt as
    echo  Administrator and run:
    echo    net start MySQL80
    echo  (replace MySQL80 with your version if different)
    echo.
    pause
    EXIT /B 1
)

echo      Database "socialmedia_db" is ready!

REM ============================================================
REM STEP 4: UPDATE application.properties WITH PASSWORD
REM ============================================================
echo.
echo [4/5] Updating application.properties with your DB password...

SET PROPS_FILE=src\main\resources\application.properties

IF NOT EXIST "%PROPS_FILE%" (
    echo  [WARNING] Could not find application.properties
    echo  Make sure you run this script from inside the project folder.
    echo  Expected: %CD%\%PROPS_FILE%
    echo.
    pause
    EXIT /B 1
)

REM Write a clean application.properties with the entered password
(
echo # =============================================
echo # Application Config
echo # =============================================
echo spring.application.name=social-media-api
echo server.port=8080
echo server.servlet.context-path=/api
echo.
echo # =============================================
echo # Database Config
echo # =============================================
echo spring.datasource.url=jdbc:mysql://localhost:3306/socialmedia_db?useSSL=false^&serverTimezone=UTC^&allowPublicKeyRetrieval=true
echo spring.datasource.username=root
echo spring.datasource.password=%DB_PASSWORD%
echo spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
echo.
echo # =============================================
echo # JPA / Hibernate Config
echo # =============================================
echo spring.jpa.hibernate.ddl-auto=update
echo spring.jpa.show-sql=true
echo spring.jpa.properties.hibernate.format_sql=true
echo spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
echo spring.jpa.open-in-view=false
echo.
echo # =============================================
echo # JWT Config
echo # =============================================
echo app.jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
echo app.jwt.expiration-ms=86400000
echo.
echo # =============================================
echo # Pagination Defaults
echo # =============================================
echo app.pagination.default-page=0
echo app.pagination.default-size=10
echo app.pagination.max-size=50
echo.
echo # =============================================
echo # Swagger / OpenAPI
echo # =============================================
echo springdoc.api-docs.path=/v3/api-docs
echo springdoc.swagger-ui.path=/swagger-ui.html
echo springdoc.swagger-ui.operationsSorter=method
echo.
echo # =============================================
echo # Logging
echo # =============================================
echo logging.level.com.socialmedia=DEBUG
echo logging.level.org.springframework.security=INFO
echo logging.level.org.hibernate.SQL=DEBUG
) > "%PROPS_FILE%"

echo      application.properties updated successfully!

REM ============================================================
REM STEP 5: CHECK MAVEN AND RUN THE APP
REM ============================================================
echo.
echo [5/5] Starting the Spring Boot application...
echo.

REM Check if mvn is available
WHERE mvn >nul 2>&1
IF %ERRORLEVEL% == 0 (
    echo      Maven found on PATH. Running: mvn spring-boot:run
    echo.
    echo ============================================================
    echo  App will start at: http://localhost:8080/api
    echo  Swagger UI at:     http://localhost:8080/api/swagger-ui.html
    echo  Press Ctrl+C to stop the server
echo ============================================================
    echo.
    mvn spring-boot:run
    GOTO :END
)

REM Check for mvnw (Maven wrapper)
IF EXIST "mvnw.cmd" (
    echo      Using Maven Wrapper (mvnw)...
    echo.
    echo ============================================================
    echo  App will start at: http://localhost:8080/api
    echo  Swagger UI at:     http://localhost:8080/api/swagger-ui.html
    echo  Press Ctrl+C to stop the server
    echo ============================================================
    echo.
    mvnw.cmd spring-boot:run
    GOTO :END
)

REM Maven not found
echo  [WARNING] Maven (mvn) was not found on your PATH.
echo.
echo  You have two options:
echo.
echo  Option A - Run from STS (recommended):
echo    1. Open STS
echo    2. Import this project (File - Import - Maven - Existing Maven Projects)
echo    3. Right-click SocialMediaApplication.java
echo    4. Run As - Spring Boot App
echo.
echo  Option B - Install Maven:
echo    https://maven.apache.org/download.cgi
echo    Download "Binary zip archive", extract it,
echo    add the \bin folder to your system PATH,
echo    then re-run this script.
echo.

:END
echo.
pause
ENDLOCAL
