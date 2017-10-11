#!/bin/bash
# one-liner to find the absolute path where this script is running from and using that for the Jar and Config files, so that
# this script can be invoked without a chdir from another script

LOGDIR="/var/log/apiserver"
mkdir -p ${LOGDIR}

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
#echo ${DIR}
exec java -jar "${DIR}/target/apiserver-1.0-SNAPSHOT.jar" server ${DIR}/conf/app.yml 2>&1
