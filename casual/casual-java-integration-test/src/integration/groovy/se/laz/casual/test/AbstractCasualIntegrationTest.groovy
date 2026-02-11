/*
 * Copyright (c) 2024 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test

import io.fabric8.kubernetes.api.model.Pod
import jakarta.json.Json
import jakarta.json.JsonObject
import se.laz.casual.test.tdk8s.TestKube
import se.laz.casual.test.tdk8s.connection.KubeConnection
import se.laz.casual.test.tdk8s.exec.ExecResult
import spock.lang.Shared
import spock.lang.Specification

import java.net.http.HttpResponse
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

        tk = TestKube.newBuilder(  )
                .addPod( "casual", casualPod )
                .addPod( CasualJavaResources.SIMPLE_CASUAL_JAVA_POD_NAME, CasualJavaResources.SIMPLE_CASUAL_JAVA_POD )
                .addService( "casual-svc", CasualResources.SIMPLE_CASUAL_SERVICE )
                .addService( "casual-java-svc", CasualJavaResources.SIMPLE_CASUAL_JAVA_SERVICE )
                .addProvisioningProbe( "casual connection.", (t)->{
                    String body = "{\"hello\":\"there\"}"
                    String[] command = ["sh", "-c",
                                        "curl -s http://localhost:8080/casual/casual%2Fexample%2Fecho " +
                                                "-H 'Content-Type: application/casual-x-octet' " +
                                                "-d '" + body + "'"]
                    ExecResult result = t.getController(  ).executeCommandAsync( CasualJavaResources.SIMPLE_CASUAL_JAVA_POD_NAME, command )
                            .get( 5, TimeUnit.SECONDS )
                    return result.getExitCode(  ) == 0 && result.getOutput() == body
                } )
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
        String[] command = ["sh", "-c", "casual service --list-services" ]
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
}
