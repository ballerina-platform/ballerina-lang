#!/bin/bash
# ---------------------------------------------------------------------------
#  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

# ----------------------------------------------------------------------------
# --------------- Startup Script for Ballerina Language Server ---------------
# ----------------------------------------------------------------------------

# ------------------ Environment Variable Prerequisites ----------------------
#
#   BALLERINA_HOME      (Optional) Home of Ballerina installation.
#
#   JAVA_HOME           Must point at your Java Development Kit installation.
#
# NOTE: Borrowed generously from Apache Tomcat startup scripts.
# ----------------------------------------------------------------------------

# ---------------------- Override JAVA_HOME for Installers -------------------
DIR="$(cd "$(dirname "$0")" && pwd)"
JAVA_PATH="$DIR/../../../../../../dependencies/jdk-11.0.8+10-jre"
if test -d "$JAVA_PATH"; then
  JAVA_HOME=$JAVA_PATH
fi

# ---------------------- Developer Configurations ---------------------------
BALLERINA_DEBUG_LOG=false;
DEBUG_MODE=false;
ALLOW_EXPERIMETAL=false;
DEBUG_PORT=5005;
CUSTOM_CLASSPATH="";
# ----------------------------------------------------------------------------

# ---------------------- Command Line Args ---------------------------
#while [ "$1" != "" ]; do
#    if [ "$1" = "--debug" ];
#    then
#       DEBUG_MODE=true;
#    elif [ "$1" = "--classpath" ];
#    then
#       shift
#       CUSTOM_CLASSPATH="$1";
#    elif [ "$1" = "--experimental" ];
#    then
#       ALLOW_EXPERIMETAL=true;
#    fi
#    # Add more if elseif clauses or use a switch case to check $1
#    # if parsing more arguments is required in future.
#    # Shift all the parameters down by one
#    shift
#done
# ---------------------- Command Line Args ---------------------------

# OS specific support.  $var _must_ be set to either true or false.
#ulimit -n 100000
BASE_DIR=$PWD
darwin=false;
os400=false;
case "`uname`" in
OS400*) os400=true;;
Darwin*) darwin=true
        if [ -z "$JAVA_HOME" ] ; then
		   if [ -z "$JAVA_VERSION" ] ; then
			 JAVA_HOME=$(/usr/libexec/java_home)
           else
             >&2 echo "Using Java version: $JAVA_VERSION"
			 JAVA_HOME=$(/usr/libexec/java_home -v $JAVA_VERSION)
		   fi
	    fi
        ;;
esac

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# set BALLERINA_HOME
BALLERINA_HOME=`cd "$PRGDIR/../../../.." ; pwd`

# For OS400
if $os400; then
  # Set job priority to standard for interactive (interactive - 6) by using
  # the interactive priority - 6, the helper threads that respond to requests
  # will be running at the same priority as interactive jobs.
  COMMAND='chgjob job('$JOBNAME') runpty(6)'
  system $COMMAND

  # Enable multi threading
  QIBM_MULTI_THREADED=Y
  export QIBM_MULTI_THREADED
fi

if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=java
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  >&2 echo "Error: JAVA_HOME is not defined correctly."
  exit 1
fi

# if JAVA_HOME is not set we're not happy
if [ -z "$JAVA_HOME" ]; then
  >&2 echo "You must set the JAVA_HOME variable before running Ballerina."
  exit 1
fi

if [ $DEBUG_MODE = true ]; then
  JAVA_DEBUG="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:$DEBUG_PORT"
else
  JAVA_DEBUG=""
fi

JDK_11=`$JAVA_HOME/bin/java -version 2>&1 | grep -e "11."`
if [ "$JDK_11" = "" ]; then
    >&2 echo "Error: Ballerina is supported only on JDK 11"
    exit 1
fi

CLASSPATHS="$CLASSPATHS":"$CUSTOM_CLASSPATH"

CLASSPATHS="$CLASSPATHS":"$BALLERINA_HOME"/bre/lib/*

CLASSPATHS="$CLASSPATHS":"$BALLERINA_HOME"/lib/tools/debug-adapter/lib/*

echo $BALLERINA_HOME

# ------------------------- Execute Command ---------------------------------

# echo JAVA_HOME environment variable is set to $JAVA_HOME
# echo BALLERINA_HOME environment variable is set to $BALLERINA_HOME

$JAVACMD \
	-XX:+HeapDumpOnOutOfMemoryError \
	-XX:HeapDumpPath="$BALLERINA_HOME"/debug-adapter-heap-dump.hprof \
	$JAVA_DEBUG \
	-Dballerina.home=$BALLERINA_HOME \
	-Dballerina.debugLog=$DEBUG_LOG \
	-Dexperimental=$ALLOW_EXPERIMETAL \
	-cp "$CLASSPATHS" \
	 org.ballerinalang.debugadapter.launcher.Launcher "$@"
