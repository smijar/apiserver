#------------------------------------------------------------------
# Description:
#
# This Docker file creates a Centos 7 image with mongodb and jdk 7
#	installed.  It also keeps the localhost's source tree as the deployment
#	folder within the docker container.
#
# This enables us to run the latest code inside Centos 7 with Mongodb
#	running.  This Dockerfile is specifically built to run DCService locally.
#------------------------------------------------------------------
FROM java:openjdk-7-jre

RUN apt-get update && \
    apt-get install qemu-utils -y --no-install-recommends && \
    rm -rf /var/lib/apt/lists/*

# configure mount points for services
VOLUME /opt/apiserver/services
VOLUME /etc/apiserver/conf/
VOLUME /opt/apiserver/bin
VOLUME /opt/apiserver/repo

EXPOSE 7100

CMD /opt/apiserver/app/devstart.sh 
