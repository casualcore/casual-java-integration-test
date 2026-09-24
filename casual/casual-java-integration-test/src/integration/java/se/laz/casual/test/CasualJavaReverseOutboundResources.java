/*
 * Copyright (c) 2025 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;

import java.util.Map;

public class CasualJavaReverseOutboundResources
{

    public static final String CASUAL_JAVA_CONTAINER_NAME = "casual-java";
    public static final String CASUAL_JAVA_CONTAINER_IMAGE = "192.168.39.162:5000/casual-java:3.5.1-SNAPSHOT";
    public static final String SIMPLE_CASUAL_JAVA_POD_NAME = "casual-java-reverse-outbound";

    public static final Map<String, String> SELECTOR = Map.of( "app", "casual-java-reverse-outbound-app" );

    public static final String SIMPLE_CASUAL_JAVA_SERVICE_NAME = "casual-java-reverse-outbound-service";

    public static final Pod SIMPLE_CASUAL_JAVA_POD = new PodBuilder()
            .withNewMetadata()
                .withName( SIMPLE_CASUAL_JAVA_POD_NAME )
                .addToLabels( SELECTOR )
            .endMetadata()
            .withNewSpec()
                .addNewContainer()
                    .withName( CASUAL_JAVA_CONTAINER_NAME )
                    .withImage( CASUAL_JAVA_CONTAINER_IMAGE )
                    .withImagePullPolicy( "Always" )
                    .addNewPort().withContainerPort( 7771 ).endPort()
                    .addNewPort().withContainerPort( 7772 ).endPort()
                    .addNewPort().withContainerPort( 7773 ).endPort()
                    .addNewPort().withContainerPort( 8080 ).endPort()
                    .addNewPort().withContainerPort( 9990 ).endPort()
                    .withNewReadinessProbe()
                        .withNewTcpSocket()
                            .withNewPort()
                                .withValue( 7772 )
                            .endPort()
                        .endTcpSocket()
                    .withTimeoutSeconds(600)
                    .endReadinessProbe()
                    // no outbound specification, CLI has set up the reverse outbound pool along with reverse-outbound.json
                    .addNewEnv().withName("CASUAL_CONFIG_FILE").withValue("/opt/jboss/wildfly/casual/configs/reverse-outbound.json").endEnv()
                    .addNewEnv().withName( "CASUAL_CALLER_CONNECTION_FACTORY_JNDI_SEARCH_ROOT" ).withValue( "java:/eis" ).endEnv()
                    .addNewEnv().withName( "CASUAL_FIELD_TABLE" ).withValue( "/opt/jboss/wildfly/casual/configs/casual-fields.json" ).endEnv()
                    .addNewEnv().withName( "CASUAL_NETWORK_OUTBOUND_ENABLE_LOGHANDLER" ).withValue( "false" ).endEnv()
                    .addNewEnv().withName( "CASUAL_NETWORK_INBOUND_ENABLE_LOGHANDLER" ).withValue( "false" ).endEnv()
                    .withCommand("/opt/jboss/wildfly/bin/standalone.sh",  "-c", "standalone-full-reverse-outbound.xml", "--debug", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0")
                    .withImagePullPolicy("IfNotPresent")
                .endContainer()
            .endSpec()
        .build();

    public static final Service SIMPLE_CASUAL_JAVA_SERVICE = new ServiceBuilder()
            .withNewMetadata()
                .withName( SIMPLE_CASUAL_JAVA_SERVICE_NAME )
            .endMetadata()
            .withNewSpec()
                .addToSelector( SELECTOR )
                .addNewPort().withName( "output" ).withPort( 7771 ).endPort()
                .addNewPort().withName( "inbound" ).withPort( 7772 ).endPort()
                .addNewPort().withName( "http" ).withPort( 8080 ).endPort()
                .addNewPort().withName( "admin" ).withPort( 9990 ).endPort()
            .endSpec()
            .build();
}
