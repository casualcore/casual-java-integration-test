ARG BASE_IMAGE_VERSION=1.6.18
FROM docker.io/casual/middleware:${BASE_IMAGE_VERSION}

USER root

RUN yum update-minimal -y
RUN yum install -y strace gettext gdb nmap-ncat
RUN yum clean all -y

USER 1001

COPY files/casual.env $CASUAL_DOMAIN_HOME/
COPY files/domain.yaml $CASUAL_DOMAIN_HOME/configuration/

ENV CASUAL_LOG "(error|warning|information)"
ENV CASUAL_LOG_PATH=/tmp/casual.log

COPY files/uid_entrypoint.sh $CASUAL_DOMAIN_HOME/uid_entrypoint.sh

ENTRYPOINT $CASUAL_DOMAIN_HOME/uid_entrypoint.sh
