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
if exist %BALLERINA_HOME%\..\..\dependencies\jdk8u265-b01-jre (
   set "JAVA_HOME=%BALLERINA_HOME%\..\..\dependencies\jdk8u265-b01-jre"
)

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
goto checkServer

:noJavaHome
echo "You must set the JAVA_HOME variable before running Ballerina."
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
FOR %%C in ("%BALLERINA_HOME%\bre\lib\bootstrap\*.jar") DO set BALLERINA_CLASSPATH=!BALLERINA_CLASSPATH!;"%BALLERINA_HOME%\bre\lib\bootstrap\%%~nC%%~xC"

set BALLERINA_CLASSPATH="%JAVA_HOME%\lib\tools.jar";%BALLERINA_CLASSPATH%;

set BALLERINA_CLASSPATH=!BALLERINA_CLASSPATH!;"%BALLERINA_HOME%\bre\lib\*"

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

set /a counter=1
for /l %%i in (1, 1, %argCount%) do (
   set /a counter=!counter!+1
   if "!argValue[%%i]!"=="--debug" call set BAL_JAVA_DEBUG=%%!counter!
)

if defined BAL_JAVA_DEBUG goto commandDebug

rem ----- Process the input command -------------------------------------------
goto doneStart

rem ----- commandDebug ---------------------------------------------------------
:commandDebug
if "%BAL_JAVA_DEBUG%"=="" goto noDebugPort
if not "%JAVA_OPTS%"=="" echo Warning !!!. User specified JAVA_OPTS will be ignored, once you give the BAL_JAVA_DEBUG variable.
set JAVA_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=%BAL_JAVA_DEBUG%
echo Please start the remote debugging client to continue...
goto runServer

:noDebugPort
echo Please specify the debug port for the BAL_JAVA_DEBUG variable
goto end

:doneStart
if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

goto runServer

rem ----------------- Execute The Requested Command ----------------------------

:runServer

set CMD=%*

rem ---------- Add jars to classpath ----------------

set BALLERINA_CLASSPATH=.\bre\lib\bootstrap;%BALLERINA_CLASSPATH%

rem BALLERINA_CLASSPATH_EXT is for outsiders to additionally add
rem classpath locations, e.g. AWS Lambda function libraries.
set BALLERINA_CLASSPATH=%BALLERINA_CLASSPATH%;%BALLERINA_CLASSPATH_EXT%

set CMD_LINE_ARGS=-Xbootclasspath/a:%BALLERINA_XBOOTCLASSPATH% -Xms256m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath="%BALLERINA_HOME%\heap-dump.hprof"  -Dcom.sun.management.jmxremote -classpath %BALLERINA_CLASSPATH% %JAVA_OPTS% -Dballerina.home="%BALLERINA_HOME%" -Dballerina.target="jvm" -Djava.command="%JAVA_HOME%\bin\java" -Djava.opts="%JAVA_OPTS%" -Denable.nonblocking=false -Dfile.encoding=UTF8 -Dballerina.version=1.0.3 -Djava.util.logging.config.class="org.ballerinalang.logging.util.LogConfigReader" -Djava.util.logging.manager="org.ballerinalang.logging.BLogManager" -DBALLERINA_DEV_COMPILE_BALLERINA_ORG=true

set jar=%2
if "%1" == "run" if not "%2" == "" if "%jar:~-4%" == ".jar" goto runJarFile
:runJava
"%JAVA_HOME%\bin\java" %CMD_LINE_ARGS% org.ballerinalang.tool.Main %CMD%
goto end

:runJarFile
for /f "tokens=1,*" %%a in ("%*") do set ARGS=%%b
"%JAVA_HOME%\bin\java" %CMD_LINE_ARGS% -jar %ARGS%
goto end

:end
goto endlocal

:endlocal

:END
