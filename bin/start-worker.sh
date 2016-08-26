#!/bin/sh
if [ -z "${DISTDEEP_HOME}" ]; then
  export DISTDEEP_HOME="$(cd ..; pwd)"
fi
echo "DISTDEEP_HOME = $DISTDEEP_HOME"

if [ "$DISTDEEP_MASTER_PORT" = "" ]; then
  DISTDEEP_MASTER_PORT=2551
fi
echo "DISTDEEP_MASTER_PORT = $DISTDEEP_MASTER_PORT"

if [ "$DISTDEEP_MASTER_HOST" = "" ]; then
  DISTDEEP_MASTER_HOST="$(wget http://ipecho.net/plain -O - -q ; echo)"
fi
echo "DISTDEEP_MASTER_HOST = $DISTDEEP_MASTER_HOST"

CLASS="com.doohh.akkaClustering.Worker"
java -cp $DISTDEEP_HOME/jars/distDeep-core-0.0.1.jar $CLASS $DISTDEEP_MASTER_HOST