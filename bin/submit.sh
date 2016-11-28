#!/bin/sh
if [ -z "${DISTDEEP_HOME}" ]; then
  export DISTDEEP_HOME="$(cd ..; pwd)"
fi
#echo "DISTDEEP_HOME = $DISTDEEP_HOME"

if [ "$DISTDEEP_HOST" = "" ]; then
  DISTDEEP_HOST="$(wget http://ipecho.net/plain -O - -q ; echo)"
fi

CLASS="com.doohh.akkaClustering.deploy.SubmitMain"
java -cp $DISTDEEP_HOME/jars/akkaClustering-0.0.1-allinone.jar $CLASS -h $DISTDEEP_HOST $@ &