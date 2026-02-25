/*
 * Copyright (c) 2024 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.ConfigMapBuilder
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import jakarta.json.Json
import jakarta.json.JsonObject
import se.laz.casual.test.tdk8s.TestKube
import se.laz.casual.test.tdk8s.connection.KubeConnection
import se.laz.casual.test.tdk8s.exec.ExecResult
import spock.lang.Shared
import spock.lang.Specification

import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

abstract class AbstractCasualIntegrationTest extends Specification
{

    @Shared
    TestKube tk

    /**
     * Use to run these test multiple times with different version of casual.
     * @return casual container image to use.
     */
    abstract String getCasualImage();

    def setupSpec()
    {

        Pod casualPod = CasualResources.SIMPLE_CASUAL_POD.edit(  )
                .editSpec(  )
                .editContainer( 0 )
                .withImage( getCasualImage(  )  )
                .endContainer(  )
                .endSpec(  )
                .build(  )

        Pod casualJavaPod = CasualJavaResources.SIMPLE_CASUAL_JAVA_POD

//         Following can can be used to mount your own domain configuration.
//         Currently it replaces with the same as it is built with.
//
//        Path replacementFile = new File( "./src/integration/resources/casual-config-inbound-discover.json").toPath(  )
//
//        String mapName = "test-config-map"
//        ConfigMap map = new ConfigMapBuilder().withNewMetadata(  )
//                .withName( mapName )
//                .addToLabels( TestKube.RESOURCE_LABEL_NAME, UUID.randomUUID(  ).toString(  ) )
//                .endMetadata(  )
//                .addToData( replacementFile.getFileName(  ).toString(  ), Files.readString( replacementFile ) )
//                .build(  )
//
//        casualJavaPod = casualJavaPod.edit(  )
//                .editSpec(  )
//                .addNewVolume(  )
//                .withName( "test" )
//                .withNewConfigMap(  )
//                .withName( map.getMetadata(  ).getName(  ) )
//                .endConfigMap(  )
//                .endVolume(  )
//                .editContainer( 0 )
//                .addNewVolumeMount(  )
//                .withName("test"  )
//                .withMountPath( "/opt/jboss/wildfly/casual/configs/casual-config-inbound-discover.json" )
//                .withSubPath( "casual-config-inbound-discover.json" )
//                .endVolumeMount(  )
//                .addNewEnv(  ).withName("CASUAL_CONFIG_FILE"  ).withValue("/opt/jboss/wildfly/casual/configs/casual-config-inbound-discover.json"  ).endEnv(  )
//                .endContainer(  )
//                .endSpec(  )
//                .build(  )
//
//        // Replace with tdk8s 0.0.2-beta to add configmaps.
//        KubernetesClient client = new KubernetesClientBuilder().build(  )
//        client.configMaps(  ).resource( map ).serverSideApply(  )

        tk = TestKube.newBuilder(  )
                .addPod( CasualResources.SIMPLE_CASUAL_POD_NAME, casualPod )
                .addPod( CasualJavaResources.SIMPLE_CASUAL_JAVA_POD_NAME, casualJavaPod )
                .addService( "casual-svc", CasualResources.SIMPLE_CASUAL_SERVICE )
                .addService( "casual-java-svc", CasualJavaResources.SIMPLE_CASUAL_JAVA_SERVICE )
                .addProvisioningProbe( "casual connection.", (t)->{
                    String body = "{\"hello\":\"there\"}"
                    String[] command = ["sh", "-c",
                                        "curl -s http://localhost:8080/casual/casual%2Fexample%2Fjava%2Fecho " +
                                                "-H 'Content-Type: application/casual-x-octet' " +
                                                "-d '" + body + "'"]
                    ExecResult result = t.getController(  ).executeCommandAsync( CasualJavaResources.SIMPLE_CASUAL_JAVA_POD_NAME, command )
                            .get( 5, TimeUnit.SECONDS )
                    boolean passed = result.getExitCode(  ) == 0 && result.getOutput() == body
                    if( !passed )
                    {
                        println( "Casual Connection Failed: " + result )
                    }
                    return passed
                } )
//                .addProvisioningProbe( "casual connection.", (t)->{
//                    String body = "{\"hello\":\"there\"}"
//                    String[] command = ["sh", "-c",
//                                        "curl -s http://localhost:8080/casual/casual%2Fexample%2Fecho " +
//                                                "-H 'Content-Type: application/casual-x-octet' " +
//                                                "-d '" + body + "'"]
//                    ExecResult result = t.getController(  ).executeCommandAsync( CasualJavaResources.SIMPLE_CASUAL_JAVA_POD_NAME, command )
//                            .get( 5, TimeUnit.SECONDS )
//                    boolean passed = result.getExitCode(  ) == 0 && result.getOutput() == body
//                    if( !passed )
//                    {
//                        println( "Casual Connection Failed: " + result )
//                    }
//                    return passed
//                } )
//                .addProvisioningProbe( "casual java connection.", (t)->{
//                    String payload = "This is what i want to echo back."
//                    String serviceName = "casual/example/java/echo"
//                    String pod = CasualResources.SIMPLE_CASUAL_POD_NAME
//                    String actualCommand = """echo -n '${payload}' | casual buffer --compose | casual call --service ${serviceName} | casual buffer --extract"""
//                    String[] command = ["sh", "-c", actualCommand ]
//
//                    ExecResult result = tk.getController(  ).executeCommandAsync( pod, command ).get( 5, TimeUnit.SECONDS)
//
//                    boolean passed = result.getExitCode(  ) == 0 && result.getOutput() == payload
//                    if( !passed )
//                    {
//                        println( "Casual Java Connection Failed: " + result )
//                    }
//                    return passed
//                } )
                .build(  )

        long start = System.currentTimeMillis(  )
        tk.init(  )
        long end = System.currentTimeMillis(  )
        println( "Init duration: " + (end - start ) )
    }

    def cleanupSpec()
    {
        long start = System.currentTimeMillis(  )
        tk.destroy(  )
        long end = System.currentTimeMillis(  )
        println( "Destroy duration: " + ( end - start ) )

    }

    def "Call echo service for outbound call to casual."()
    {
        given:
        String payload = "This is the message to echo."
        HttpResponse<String> response

        when:
        try( KubeConnection con = tk.getConnection( "casual-java-svc", 8080 ) )
        {
           response = Http.post( con, "/casual/casual%2Fexample%2Fecho", "application/casual-x-octet", payload )
        }

        then:
        response != null
        response.statusCode(  ) == 200
        response.body(  ) == payload
    }

    def "Call domain name service for outbound call to casual."()
    {
        given:
        String payload = ""
        HttpResponse<String> response

        when:
        try( KubeConnection con = tk.getConnection( "casual-java-svc", 8080 ) )
        {
            response = Http.post( con, "/casual/casual%2Fexample%2Fdomain%2Fname", "application/casual-x-octet", payload )
        }

        then:
        response != null
        response.statusCode(  ) == 200
        response.body(  ) == "test-domain\0"
    }

    def "Call lowercase service for outbound call to casual."()
    {
        given:
        String payload = "THIS IS UPPERCASE"
        HttpResponse<String> response

        when:
        try( KubeConnection con = tk.getConnection( "casual-java-svc", 8080 ) )
        {
            response = Http.post( con, "/casual/casual%2Fexample%2Flowercase", "application/casual-x-octet", payload )
        }

        then:
        response != null
        response.statusCode(  ) == 200
        response.body(  ) == payload.toLowerCase()
    }

    def "Call uppercase service for outbound call to casual."()
    {
        given:
        String payload = "this is lowercase"
        HttpResponse<String> response

        when:
        try( KubeConnection con = tk.getConnection( "casual-java-svc", 8080 ) )
        {
            response = Http.post( con, "/casual/casual%2Fexample%2Fuppercase", "application/casual-x-octet", payload )
        }

        then:
        response != null
        response.statusCode(  ) == 200
        response.body(  ) == payload.toUpperCase()
    }

    def "Call error/system service for outbound call to casual."()
    {
        given:
        String payload = ""
        HttpResponse<String> response

        when:
        try( KubeConnection con = tk.getConnection( "casual-java-svc", 8080 ) )
        {
            response = Http.post( con, "/casual/casual%2Fexample%2Ferror%2Fsystem", "application/casual-x-octet", payload )
        }

        then:
        response != null
        response.statusCode(  ) == 500
        response.body(  ).contains( "TPESVCERR" )
    }

    def "Call non-existing service for outbound call to casual."()
    {
        given:
        String payload = ""
        HttpResponse<String> response

        when:
        try( KubeConnection con = tk.getConnection( "casual-java-svc", 8080 ) )
        {
            response = Http.post( con, "/casual/casual%2Fnon-existing", "application/casual-x-octet", payload )
        }

        then:
        response != null
        response.statusCode(  ) == 500
        response.body(  ).contains( "TPENOENT" )
    }

    def "Check service list casual."()
    {
        when:
        String[] command = ["sh", "-c", "export CASUAL_LOG_PATH=/tmp/casual.log && casual service --list-services" ]
        ExecResult result = tk.getController(  ).executeCommand( "casual", command )

        then:
        result.getExitCode(  ) == 0
        result.getOutput(  ).contains( "casual/example/echo" )
    }

    def "enqueue then dequeue."()
    {
        given:
        String payload = "queue message body."
        String queueName = "ex1"
        HttpResponse<String> response

        when:
        try( KubeConnection con = tk.getConnection( "casual-java-svc", 8080 ) )
        {
            response = Http.post( con, "/queue/enqueue/"+queueName, "application/casual-x-octet", payload )
        }

        then:
        response != null
        response.statusCode(  ) == 200
        response.body(  ) != ""

        when:
        String messageId = response.body(  )
        try( KubeConnection con = tk.getConnection( "casual-java-svc", 8080 ) )
        {
            response = Http.post( con, "/queue/dequeue/"+queueName+"?uuid="+messageId, "application/casual-x-octet", "" )
        }

        then:
        response != null
        response.statusCode(  ) == 200
        JsonObject jo = Json.createReader( new StringReader( response.body(  ) ) ).readObject(  )
        jo.getString( "id" ) == messageId
        jo.getString( "payload" ) == payload
    }

    def "enqueue invalid queue name."()
    {
        given:
        String payload = "queue message body."
        String queueName = "invalidQueueName"
        HttpResponse<String> response

        when:
        try( KubeConnection con = tk.getConnection( "casual-java-svc", 8080 ) )
        {
            response = Http.post( con, "/queue/enqueue/"+queueName, "application/casual-x-octet", payload )
        }

        then:
        response != null
        response.statusCode(  ) == 500
        response.body(  ).contains( "TPENOENT" )
    }

    def "dequeue invalid queue name."()
    {
        given:
        String payload = ""
        String queueName = "invalidQueueName"
        HttpResponse<String> response

        when:
        try( KubeConnection con = tk.getConnection( "casual-java-svc", 8080 ) )
        {
            response = Http.post( con, "/queue/dequeue/"+queueName, "application/casual-x-octet", payload )
        }

        then:
        response != null
        response.statusCode(  ) == 500
        response.body(  ).contains( "TPENOENT" )
    }

    def "Call inbound echo casual to casual java."()
    {
        given:
        String payload = "This is what i want to echo back."
        String serviceName = "casual/example/java/echo"
        String pod = CasualResources.SIMPLE_CASUAL_POD_NAME

        String actualCommand = """export CASUAL_LOG_PATH=/tmp/casual.log && echo -n '${payload}' | casual buffer --compose | casual call --service ${serviceName} | casual buffer --extract"""
        String[] command = ["sh", "-c", actualCommand ]
        when:
        ExecResult result = tk.getController(  ).executeCommand( pod, command )

        then:
        result.getExitCode(  ) == 0
        result.getOutput(  ) == payload
    }

    def "Call echo service inbound for java via outbound call to casual."()
    {
        given:
        String payload = "This is the message to echo."
        HttpResponse<String> response

        when:
        try( KubeConnection con = tk.getConnection( "casual-java-svc", 8080 ) )
        {
            response = Http.post( con, "/casual/casual%2Fexample%2Fjava%2Fecho", "application/casual-x-octet", payload )
        }

        then:
        response != null
        response.statusCode(  ) == 200
        response.body(  ) == payload
    }
}
