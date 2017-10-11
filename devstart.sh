#!/bin/bash
# one-liner to find the absolute path where this script is running from and using that for the Jar and Config files, so that
# this script can be invoked without a chdir from another script
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
NAME=dcservice

set -ex
LOGDIR="/var/log/apiserver"
CONFDIR="${DIR}/conf"
if [ ! -d "${LOGDIR}" ]; then
    mkdir -p "${LOGDIR}"
fi

if [ ! -d "${CONFDIR}" ]; then
    mkdir -p "${CONFDIR}"
fi

JAR=$(ls "${DIR}"/target/apiserver-*-SNAPSHOT.jar)
CP="${DIR}/target/lib/*":"${JAR}"
#if [ -f "${CONFDIR}/.wizard.lock" ]; then
#    echo "Wizard lock file found, skipping wizard run. [${CONFDIR}/.wizard.lock]"
#else
#    java -cp "${CP}" com.dc.mgmt.services.DCWizard /etc/dcserver/conf/wizard.yml
#    # "${DIR}/wizard.sh" || exit $?
#    touch "${CONFDIR}/.wizard.lock"
#fi


java -cp "${CP}" com.app.apiserver.services.AppMain server "${CONFDIR}/app.yml"
