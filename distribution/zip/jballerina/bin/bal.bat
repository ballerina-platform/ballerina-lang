@echo off

REM ---------------------------------------------------------------------------
REM   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
REM
REM   Licensed under the Apache License, Version 2.0 (the "License");
REM   you may not use this file except in compliance with the License.
REM   You may obtain a copy of the License at
REM
REM   http://www.apache.org/licenses/LICENSE-2.0
REM
REM   Unless required by applicable law or agreed to in writing, software
REM   distributed under the License is distributed on an "AS IS" BASIS,
REM   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM   See the License for the specific language governing permissions and
REM   limitations under the License.

rem ---------------------------------------------------------------------------
rem Main Script for Ballerina
rem
rem Environment Variable Prerequisites
rem
rem   BALLERINA_HOME  Home of BALLERINA installation. If not set I will  try
rem                   to figure it out.
rem
rem   JAVA_HOME       Must point at your Java Development Kit installation.
rem
rem   JAVA_OPTS       (Optional) Java runtime options used when the commands
rem                   is executed.
rem ---------------------------------------------------------------------------

rem ----- if JAVA_HOME is not set we're not happy ------------------------------

:checkJava

set BALLERINA_HOME=%~sdp0..
if exist %BALLERINA_HOME%\..\..\dependencies\jdk-17.0.7+7-jre (
   set "JAVA_HOME=%BALLERINA_HOME%\..\..\dependencies\jdk-17.0.7+7-jre"
)

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto invalidJavaHome
goto checkServer

:noJavaHome
echo "You must set the JAVA_HOME variable before running Ballerina."
goto end

:invalidJavaHome
echo "Invalid JAVA_HOME. The system cannot find the specified path." 1>&2
goto end

rem ----- set BALLERINA_HOME ----------------------------
:checkServer
rem %~sdp0 is expanded pathname of the current script under NT with spaces in the path removed
set BALLERINA_HOME=%~sdp0..

goto updateClasspath

:noServerHome
echo BALLERINA_HOME is set incorrectly or BALLERINA could not be located. Please set BALLERINA_HOME.
goto end

rem ----- update classpath -----------------------------------------------------
:updateClasspath

setlocal EnableDelayedExpansion
set BALLERINA_CLASSPATH=
FOR %%C in ("%BALLERINA_HOME%\bre\lib\bootstrap\*.jar") DO set BALLERINA_CLASSPATH=!BALLERINA_CLASSPATH!;%BALLERINA_HOME%\bre\lib\bootstrap\%%~nC%%~xC

set BALLERINA_CLASSPATH=!BALLERINA_CLASSPATH!;%JAVA_HOME%\lib\tools.jar

set BALLERINA_CLASSPATH=!BALLERINA_CLASSPATH!;%BALLERINA_HOME%\bre\lib\*
set BALLERINA_CLASSPATH=!BALLERINA_CLASSPATH!;%BALLERINA_HOME%\lib\tools\lang-server\lib\*
set BALLERINA_CLASSPATH=!BALLERINA_CLASSPATH!;%BALLERINA_HOME%\lib\tools\debug-adapter\lib\*

rem ----- load bal tool jars to classpath ---------------------------------------
set "bal_version_file=%USERPROFILE%\.ballerina\ballerina-version"
if exist "!bal_version_file!" (
    for /F "usebackq delims=" %%A in ("%bal_version_file%") do (
        for /F "tokens=2 delims=-" %%B in ("%%A") do (
            set "bal_version=%%B"
        )
    )

    set "BAL_TOOLS_FILE=%USERPROFILE%\.ballerina\.config\dist-!bal_version!.toml"
    if exist "!BAL_TOOLS_FILE!" (
        set "org="
        set "name="
        set "version="
        for /F "usebackq tokens=*" %%B in ("!BAL_TOOLS_FILE!") do (
            set "line=%%B"
            echo !line! | findstr /C:"org =" > nul 2>&1
            if not errorlevel 1 (
                for /F "tokens=2 delims== " %%C in ("!line!") do set "org=%%C"
                set "org=!org:"=!"
            ) else (
                echo !line! | findstr /C:"name =" > nul 2>&1
                if not errorlevel 1 (
                    for /F "tokens=2 delims== " %%C in ("!line!") do set "name=%%C"
                    set "name=!name:"=!"
                ) else (
                    echo !line! | findstr /C:"version =" > nul 2>&1
                    if not errorlevel 1 (
                        for /F "tokens=2 delims== " %%C in ("!line!") do set "version=%%C"
                        set "version=!version:"=!"
                    )
                )
            )
            if defined org if defined name if defined version (
                set "libs=%USERPROFILE%\.ballerina\repositories\central.ballerina.io\bala\!org!\!name!\!version!\any\tool\libs\"
                for /f "delims=" %%F in ('dir /b /s /a-d "!libs!\*.jar"') do (
                    set "BALLERINA_CLASSPATH=!BALLERINA_CLASSPATH!;%%F"
                )
                set "org="
                set "name="
                set "version="
            )
        )
    )
)

set BALLERINA_CLI_HEIGHT=
set BALLERINA_CLI_WIDTH=
for /F "tokens=2 delims=:" %%a in ('mode con') do for %%b in (%%a) do (
  if not defined BALLERINA_CLI_HEIGHT (
     set "BALLERINA_CLI_HEIGHT=%%b"
  ) else if not defined BALLERINA_CLI_WIDTH (
     set "BALLERINA_CLI_WIDTH=%%b"
  )
)

set argCount=0
for %%x in (%*) do (
   set /A argCount+=1
   set "argValue[!argCount!]=%%~x"
)

if defined BAL_JAVA_DEBUG goto commandDebug
if defined BAL_DEBUG_OPTS goto commandDebugOpts

rem ----- Process the input command -------------------------------------------
goto doneStart

rem ----- commandDebug ---------------------------------------------------------
:commandDebug
if "%BAL_JAVA_DEBUG%"=="" goto noDebugPort
:commandDebugOpts
if not "%JAVA_OPTS%"=="" echo Warning !!!. User specified JAVA_OPTS will be ignored, once you give the BAL_JAVA_DEBUG variable.
if defined BAL_DEBUG_OPTS (
    set JAVA_OPTS=%BAL_DEBUG_OPTS%
) else (
    set JAVA_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=%BAL_JAVA_DEBUG%
)
goto runServer

:noDebugPort
echo Please specify the debug port for the BAL_JAVA_DEBUG variable
goto end

:doneStart
if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal
rem find the version of the jdk
:findJdk

set CMD=RUN %*

:checkJdk8AndHigher
set JVER=
for /f tokens^=2-5^ delims^=.-_^" %%j in ('"%JAVA_HOME%\bin\java" -fullversion 2^>^&1') do set "JVER=%%j%%k"
if %JVER% EQU 18 goto jdk8
goto unknownJdk

:unknownJdk
goto runServer

:jdk8
goto runServer

rem ----------------- Execute The Requested Command ----------------------------

:runServer

set CMD=%*

rem ---------- Add jars to classpath ----------------

set BALLERINA_CLASSPATH=.\bre\lib\bootstrap;!BALLERINA_CLASSPATH!

rem BALLERINA_CLASSPATH_EXT is for outsiders to additionally add
rem classpath locations, e.g. AWS Lambda function libraries.
if not "%BALLERINA_CLASSPATH_EXT%" == "" set BALLERINA_CLASSPATH=!BALLERINA_CLASSPATH!;%BALLERINA_CLASSPATH_EXT%

set CMD_LINE_ARGS=-Xbootclasspath/a:%BALLERINA_XBOOTCLASSPATH% -Xms256m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -Dcom.sun.management.jmxremote -classpath "%BALLERINA_CLASSPATH%" %JAVA_OPTS% -Dballerina.home="%BALLERINA_HOME%" -Dballerina.target="jvm" -Djava.command="%JAVA_HOME%\bin\java" -Djava.opts="%JAVA_OPTS%" -Denable.nonblocking=false -Dfile.encoding=UTF8 -Dballerina.version=${project.version}

set jar=%~2
if "%1" == "run" if "%jar:~-4%" == ".jar" goto runJar

set jar=%~4
if "%1" == "run" if "%~2" == "--debug" if "%jar:~-4%" == ".jar" goto debugJar

:runJava
"%JAVA_HOME%\bin\java" %CMD_LINE_ARGS% io.ballerina.cli.launcher.Main %CMD%
goto end

:runJar
for /f "tokens=1,*" %%a in ("%*") do set ARGS=%%b
"%JAVA_HOME%\bin\java" %CMD_LINE_ARGS% -jar %ARGS%
goto end

:debugJar
setlocal ENABLEDELAYEDEXPANSION
set JAR_ARG_FOUND=false
set t=%*
:loop
for /f "tokens=1,* " %%a in ("%t%") do (
   set ARG=%%a
   set t=%%b
   if "!ARG:~-4!" == ".jar" (
      set JAR_ARG_FOUND=true
   )
   if !JAR_ARG_FOUND! == true (
      set ARGS=!ARG!
   )
)
if defined t goto :loop
"%JAVA_HOME%\bin\java" %CMD_LINE_ARGS% -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=%3 -jar %ARGS%
goto end

:end
goto endlocal

:endlocal

:END
