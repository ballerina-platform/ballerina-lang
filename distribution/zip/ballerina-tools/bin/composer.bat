@echo off

REM ---------------------------------------------------------------------------
REM   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
rem Main Script for Ballerina Composer
rem
rem Environment Variable Prerequisites
rem
rem   BAL_COMPOSER_HOME   Home of Ballerina composer installation. If not set
rem                   I will try to figure it out.
rem
rem   JAVA_HOME       Must point at your Java Development Kit installation.
rem
rem   JAVA_OPTS       (Optional) Java runtime options used when the commands
rem                   is executed.
rem ---------------------------------------------------------------------------

rem ----- if JAVA_HOME is not set we're not happy ------------------------------

set BASE_DIR=%CD%
:checkJava

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
goto checkServer

:noJavaHome
echo "You must set the JAVA_HOME variable before running Ballerina."
goto end

rem ----- Only set BALLERINA_HOME if not already set ----------------------------
:checkServer
rem %~sdp0 is expanded pathname of the current script under NT with spaces in the path removed
if "%BALLERINA_HOME%"=="" set BALLERINA_HOME=%~sdp0..
SET curDrive=%cd:~0,1%
SET wsasDrive=%BALLERINA_HOME:~0,1%
if not "%curDrive%" == "%wsasDrive%" %wsasDrive%:

rem find BALLERINA_HOME if it does not exist due to either an invalid value passed
rem by the user or the %0 problem on Windows 9x
if not exist "%BALLERINA_HOME%\bin\version.txt" goto noServerHome

goto updateClasspath

:noServerHome
echo BALLERINA_HOME is set incorrectly or BALLERINA could not be located. Please set BALLERINA_HOME.
goto end

rem ----- update classpath -----------------------------------------------------
:updateClasspath

setlocal EnableDelayedExpansion
cd %BALLERINA_HOME%
set BALLERINA_CLASSPATH=
FOR %%C in ("%BALLERINA_HOME%\bin\bootstrap\*.jar") DO set BALLERINA_CLASSPATH=!BALLERINA_CLASSPATH!;".\bin\bootstrap\%%~nC%%~xC"

set BALLERINA_CLASSPATH="%JAVA_HOME%\lib\tools.jar";%BALLERINA_CLASSPATH%;

rem ----- Process the input command -------------------------------------------

rem Slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway).

:setupArgs
if ""%1""=="""" goto doneStart

if ""%1""==""debug""    goto commandDebug
if ""%1""==""-debug""   goto commandDebug
if ""%1""==""--debug""  goto commandDebug

if ""%1""==""help""  goto commandHelp

shift
goto setupArgs

rem ----- commandUnknownArg ----------------------------------------------------
:commandHelp
echo
echo Start Ballerina Composer using 'composer.bat'
goto end


rem ----- commandDebug ---------------------------------------------------------
:commandDebug
shift
set DEBUG_PORT=%1
if "%DEBUG_PORT%"=="" goto noDebugPort
if not "%JAVA_OPTS%"=="" echo Warning !!!. User specified JAVA_OPTS will be ignored, once you give the --debug option.
set JAVA_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=%DEBUG_PORT%
echo Please start the remote debugging client to continue...
goto findJdk

:noDebugPort
echo Please specify the debug port after the --debug option
goto end

:doneStart
if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal
goto findJdk

rem ---------- Handle the SSL Issue with proper JDK version --------------------
rem find the version of the jdk
:findJdk

set CMD=%*

:checkJdk16
"%JAVA_HOME%\bin\java" -version 2>&1 | findstr /r "1.[8]" >NUL
IF ERRORLEVEL 1 goto unknownJdk
goto jdk16

:unknownJdk
echo Starting Ballerina Composer (in unsupported JDK)
echo [ERROR] Ballerina Composer is supported only on JDK 1.8
goto jdk16

:jdk16
goto runServer

rem ----------------- Execute The Requested Command ----------------------------

:runServer
cd %BALLERINA_HOME%

FOR %%C in ("%BALLERINA_HOME%\resources\composer\services\*.jar") DO set BALLERINA_CLASSPATH=!BALLERINA_CLASSPATH!;".\resources\composer\services\%%~nC%%~xC"
set BALLERINA_CLASSPATH=!BALLERINA_CLASSPATH!;"%BALLERINA_HOME%\bre\lib\*"
rem ---------- Add jars to classpath ----------------

rem set BALLERINA_CLASSPATH=.\bin\bootstrap;%BALLERINA_CLASSPATH%

set JAVA_ENDORSED=".\bin\bootstrap\endorsed";"%JAVA_HOME%\jre\lib\endorsed";"%JAVA_HOME%\lib\endorsed"

set CMD_LINE_ARGS=-Xbootclasspath/a:%BALLERINA_XBOOTCLASSPATH% -Xms256m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath="%BALLERINA_HOME%\logs\heap-dump.hprof" -Djava.util.logging.config.file="%BALLERINA_HOME%\resources\composer\conf\composer-logging.properties" -Dcom.sun.management.jmxremote -classpath %BALLERINA_CLASSPATH% %JAVA_OPTS% -Djava.endorsed.dirs=%JAVA_ENDORSED%  -Dballerina.home="%BALLERINA_HOME%" -Dcomposer.config.path="%BALLERINA_HOME%\resources\composer\conf\composer-config.yml" -Dcomposer.public.path="%BALLERINA_HOME%\resources\composer\web\public" -Djava.command="%JAVA_HOME%\bin\java" -Djava.opts="%JAVA_OPTS%" -Dfile.encoding=UTF8 -Dcomposer.port=9091 -Dopen.browser=true -DenableCloud=false -Dworkspace.port=8289 -Dtransports.netty.conf=./resources/composer/services/netty-transports.yml -Dmsf4j.conf=./resources/composer/services/deployment.yaml

:runJava
"%JAVA_HOME%\bin\java" %CMD_LINE_ARGS% org.ballerinalang.composer.server.launcher.ServerLauncher %CMD%
if "%ERRORLEVEL%"=="121" goto runJava
:end
goto endlocal

:endlocal

:END
