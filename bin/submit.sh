#!/bin/sh
if [ -z "${DISTDEEP_HOME}" ]; then
  export DISTDEEP_HOME="$(cd ..; pwd)"
fi
#echo "DISTDEEP_HOME = $DISTDEEP_HOME"

CLASS="com.doohh.akkaClustering.submit.SubmitMain"
java -cp $DISTDEEP_HOME/jars/distDeep-core-0.0.1.jar $CLASS