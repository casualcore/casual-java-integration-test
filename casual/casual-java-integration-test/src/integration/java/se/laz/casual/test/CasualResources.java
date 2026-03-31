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

public class CasualResources
{

    public static final String CASUAL_CONTAINER_NAME = "casual";
    public static final String CASUAL_CONTAINER_IMAGE_16 = "192.168.68.106:32000/casual:1.6.18";
    public static final String CASUAL_CONTAINER_IMAGE_17 = "192.168.68.106:32000/casual:1.7.10";
    public static final String CASUAL_CONTAINER_IMAGE_18 = "192.168.68.106:32000/casual:1.8.10";
    public static final String CASUAL_CONTAINER_IMAGE = CASUAL_CONTAINER_IMAGE_18;

    public static final int CASUAL_CONTAINER_PORT = 7771;

    public static final String SIMPLE_CASUAL_POD_NAME = "casual";
    public static final String SIMPLE_CASUAL_POD_NAME2 = "casual2";

    public static final Map<String, String> SELECTOR = Map.of( "app", "casual-test-app" );
    public static final Map<String, String> SELECTOR2 = Map.of( "app", "casual-test-app2" );

    public static final String CASUAL_POD_NAME = "casual-test";
    public static final String CASUAL_POD_NAME2 = "casual-test-2";

    public static final Pod SIMPLE_CASUAL_POD = new PodBuilder()
            .withNewMetadata()
                .withName( CASUAL_POD_NAME )
                .addToLabels( SELECTOR )
            .endMetadata()
            .withNewSpec()
                .addNewContainer()
                    .withName( CASUAL_CONTAINER_NAME )
                    .withImage( CASUAL_CONTAINER_IMAGE )
                    .withImagePullPolicy( "Always" )
                    .addNewPort().withContainerPort( CASUAL_CONTAINER_PORT ).endPort()
                    .withNewReadinessProbe()
                        .withNewTcpSocket()
                            .withNewPort()
                                .withValue( CASUAL_CONTAINER_PORT )
                            .endPort()
                        .endTcpSocket()
                    .endReadinessProbe()
                .endContainer()
            .endSpec()
            .build();

    public static final Pod SIMPLE_CASUAL_POD2 = new PodBuilder()
            .withNewMetadata()
                .withName( CASUAL_POD_NAME2 )
                .addToLabels( SELECTOR2 )
            .endMetadata()
            .withNewSpec()
                .addNewContainer()
                    .withName( CASUAL_CONTAINER_NAME )
                    .withImage( CASUAL_CONTAINER_IMAGE )
                    .withImagePullPolicy( "Always" )
                    .addNewPort().withContainerPort( CASUAL_CONTAINER_PORT ).endPort()
                .endContainer()
            .endSpec()
            .build();

    public static final String SIMPLE_CASUAL_SERVICE_NAME = "casual-svc";

    public static final Service SIMPLE_CASUAL_SERVICE = new ServiceBuilder()
            .withNewMetadata()
            .withName( SIMPLE_CASUAL_SERVICE_NAME )
            .endMetadata()
            .withNewSpec()
            .addToSelector( SELECTOR )
            .addNewPort().withName( "casual" ).withPort( CASUAL_CONTAINER_PORT ).endPort()
            .endSpec()
            .build();

    public static final String SIMPLE_CASUAL_SERVICE_NAME2 = "casual-svc-2";

    public static final Service SIMPLE_CASUAL_SERVICE2 = new ServiceBuilder()
            .withNewMetadata()
            .withName( SIMPLE_CASUAL_SERVICE_NAME2 )
            .endMetadata()
            .withNewSpec()
            .addToSelector( SELECTOR2 )
            .addNewPort().withName( "casual" ).withPort( CASUAL_CONTAINER_PORT ).endPort()
            .endSpec()
            .build();
}
