/*
 * Copyright (c) 2024 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.jca

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.Pod
import se.laz.casual.test.CasualJavaResources
import se.laz.casual.test.Http
import se.laz.casual.test.tdk8s.TestKube
import se.laz.casual.test.tdk8s.connection.KubeConnection
import se.laz.casual.test.tdk8s.resources.ConfigMapFactory
import se.laz.casual.test.tdk8s.resources.FileMount
import se.laz.casual.test.tdk8s.resources.VolumeMounter
import spock.lang.Shared
import spock.lang.Specification

import java.net.http.HttpResponse
import java.nio.file.Paths

class CasualJavaIntegrationTest extends Specification
{

    @Shared
    TestKube tk

    def setupSpec()
    {
        Pod pod = CasualJavaResources.SIMPLE_CASUAL_JAVA_POD

        ConfigMap config = ConfigMapFactory.fromFile( "casual-java-config", Paths.get( "src/integration/resources/casual-config-inbound-discover.json") )
        String configFile = "/opt/jboss/wildfly/casual-config.json"
        FileMount fileMount = FileMount.newBuilder(  ).configMap( config ).mountPath( configFile ).build(  )
        pod = VolumeMounter.mount( pod, fileMount )

        pod = pod.edit(  ).editSpec(  ).editContainer( 0 )
                .addNewEnv(  )
                    .withName( "CASUAL_CONFIG_FILE" )
                    .withValue( configFile )
                .endEnv(  )
                .endContainer(  )
                .endSpec(  )
                .build(  )

        tk = TestKube.newBuilder(  )
                //.addConfigMap( "casual-java-config", config )
                .addPod( CasualJavaResources.SIMPLE_CASUAL_JAVA_POD_NAME,  pod )
                .addService( "casual-java-svc", CasualJavaResources.SIMPLE_CASUAL_JAVA_SERVICE )
                .build(  )
        long start = System.currentTimeMillis(  )
        tk.init(  )
        long end = System.currentTimeMillis(  )
        println( "Init duration: " + (end - start ) )
    }

    def cleanupSpec()
    {   long start = System.currentTimeMillis(  )
        tk.destroy(  )
        long end = System.currentTimeMillis(  )
        println( "Destroy duration: " + ( end - start ) )

    }

    def "Call echo fielded."()
    {
        given:
        String payload = "{\"id\":1,\"name\":\"myname\"}"
        HttpResponse<String> response

        when:
        try( KubeConnection con = tk.getConnection( "casual-java-svc", 8080 ) )
        {
            response = Http.post( con, "/simple/echoFielded", "application/json", payload )
        }

        then:
        response != null
        response.statusCode(  ) == 200
        response.body(  ) == payload
    }
}
