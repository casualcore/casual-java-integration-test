ARG BASE_IMAGE_VERSION=39.0.1.Final-2-jdk21
FROM quay.io/wildfly/wildfly:${BASE_IMAGE_VERSION}

USER 1001

RUN mkdir /opt/jboss/wildfly/casual

RUN $JBOSS_HOME/bin/add-user.sh admin admin --silent

ARG CASUAL_JAVA_SRC="casual-java"
ARG CASUAL_CALLER_SRC="casual-caller"
ARG CASUAL_JAVA_TEST_APPS_SRC="casual-java-test-apps/casual-java-test-app"
ARG CASUAL_JAVA_INTEGRATION_TEST_SRC="casual-java-integration-test"

ARG CASUAL_JAVA_VERSION=3.3.8
ARG CASUAL_CALLER_VERSION=3.2.22

ENV CASUAL_ROOT /opt/jboss/wildfly/casual

COPY files/*.jar $CASUAL_ROOT
COPY files/*.ear $CASUAL_ROOT
RUN mkdir $CASUAL_ROOT/configs
COPY files/configs/* $CASUAL_ROOT/configs
COPY files/casual-config.json $CASUAL_ROOT/casual-config.json

ENV CASUAL_CONFIG_FILE $CASUAL_ROOT/casual-config.json

RUN /opt/jboss/wildfly/bin/jboss-cli.sh --command="module add --name=se.laz.casual \
--resources=/opt/jboss/wildfly/casual/casual-inbound-handler-api-${CASUAL_JAVA_VERSION}.jar\
:/opt/jboss/wildfly/casual/casual-fielded-annotations-${CASUAL_JAVA_VERSION}.jar\
:/opt/jboss/wildfly/casual/casual-service-discovery-extension-${CASUAL_JAVA_VERSION}.jar\
:/opt/jboss/wildfly/casual/casual-api-${CASUAL_JAVA_VERSION}.jar\
:/opt/jboss/wildfly/casual/casual-event-api-${CASUAL_JAVA_VERSION}.jar \
    --dependencies=javaee.api,sun.jdk"

ENV WILDFLY_HOME=/opt/jboss/wildfly

COPY files/setup.cli /tmp
RUN ${WILDFLY_HOME}/bin/jboss-cli.sh --file=/tmp/setup.cli
RUN rm -rf /opt/jboss/wildfly/standalone/configuration/standalone_xml_history/current

COPY files/*.war $WILDFLY_HOME/standalone/deployments/

USER root

RUN chgrp -R 0 /opt && \
    chmod -R g=u /opt

USER 1001

RUN echo 'bazinga!'

EXPOSE 8080 7772 9990 7698

CMD ["/opt/jboss/wildfly/bin/standalone.sh",  "-c", "standalone-full.xml", "--debug", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
