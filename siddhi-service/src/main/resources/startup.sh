#! /bin/sh


# add the libraries to the SIDDHI_CLASSPATH.
# EXEDIR is the directory where this executable is.
SCRIPT_PATH=${0%/*}

if [ "$0" != "$SCRIPT_PATH" ] && [ "$SCRIPT_PATH" != "" ]; then
    cd $SCRIPT_PATH
fi

DIRLIBS=dependencies/*.jar

for i in ${DIRLIBS}
do
  if [ -z "$SIDDHI_CLASSPATH" ] ; then
    SIDDHI_CLASSPATH=$i
  else
    SIDDHI_CLASSPATH="$i":$SIDDHI_CLASSPATH
  fi
done

echo $SIDDHI_CLASSPATH
echo $EXEDIR

java -cp siddhi-service-4.0.0.jar":$SIDDHI_CLASSPATH:." org.wso2.siddhi.service.Application
