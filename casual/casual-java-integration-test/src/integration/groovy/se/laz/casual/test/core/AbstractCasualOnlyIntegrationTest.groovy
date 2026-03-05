/*
 * Copyright (c) 2024 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.core

import io.fabric8.kubernetes.api.model.ConfigMap
import io.fabric8.kubernetes.api.model.Pod
import se.laz.casual.test.CasualResources
import se.laz.casual.test.tdk8s.TestKube
import se.laz.casual.test.tdk8s.exec.ExecResult
import se.laz.casual.test.tdk8s.resources.ConfigMapFactory
import se.laz.casual.test.tdk8s.resources.FileMount
import se.laz.casual.test.tdk8s.resources.ImageUpdater
import se.laz.casual.test.tdk8s.resources.VolumeMounter
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

abstract class AbstractCasualOnlyIntegrationTest extends Specification
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

        Pod casualInOnlyPod = ImageUpdater.setImage( CasualResources.SIMPLE_CASUAL_POD, getCasualImage(  )  )

        Pod casualOutOnlyPod = CasualResources.SIMPLE_CASUAL_POD2

        ConfigMap domainConfigs = ConfigMapFactory.fromFiles( "domain-configs",
                Paths.get("src/integration/resources/domain-out-only.yaml" ),
                Paths.get("src/integration/resources/domain-in-only.yaml" ) )

        FileMount mount1 = FileMount.newBuilder()
                .configMap( domainConfigs )
                .mountPath( "/test/casual/configuration/domain.yaml" )
                .subPath( "domain-out-only.yaml" )
                .build(  )

        FileMount mount2 = FileMount.newBuilder()
                .configMap( domainConfigs )
                .mountPath( "/test/casual/configuration/domain.yaml" )
                .subPath( "domain-in-only.yaml" )
                .build(  )

        casualOutOnlyPod = VolumeMounter.mount( casualOutOnlyPod, mount1 )
        casualInOnlyPod = VolumeMounter.mount( casualInOnlyPod, mount2 )

        tk = TestKube.newBuilder(  )
                .addConfigMap( "domain-config", domainConfigs )
                .addPod( "casual-in-only", casualInOnlyPod )
                .addPod( "casual-out-only", casualOutOnlyPod )
                .addService( "casual-svc-01", CasualResources.SIMPLE_CASUAL_SERVICE )
                .addService( "casual-svc-02", CasualResources.SIMPLE_CASUAL_SERVICE2 )
                .addProvisioningProbe( "casual connection.", (t)->{
                    String payload = "This is what i want to echo back."
                    String serviceName = "casual/example/echo"
                    String pod = "casual-out-only"

                    String actualCommand = """source \$CASUAL_DOMAIN_HOME/casual.env && echo -n '${payload}' | casual buffer --compose | casual call --service ${serviceName} | casual buffer --extract"""
                    String[] command = ["sh", "-c", actualCommand ]

                    ExecResult result = t.getController(  ).executeCommandAsync( pod, command )
                            .get( 5, TimeUnit.SECONDS )
                    boolean passed = result.getExitCode(  ) == 0 && result.getOutput() == payload
                    if( !passed )
                    {
                        println( "Casual Connection Failed: " + result )
                    }
                    return passed
                } )
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

    def "Check service list casual."()
    {
        when:
        String[] command = ["sh", "-c", "source \$CASUAL_DOMAIN_HOME/casual.env && casual service --list-services" ]
        ExecResult result = tk.getController(  ).executeCommand( "casual-out-only", command )

        then:
        result.getExitCode(  ) == 0
        result.getOutput(  ).contains( "casual/example/echo" )
    }

    def "Check service list downstream."()
    {
        when:
        String[] command = ["sh", "-c", "source \$CASUAL_DOMAIN_HOME/casual.env && casual service --list-services" ]
        ExecResult result = tk.getController(  ).executeCommand( "casual-in-only", command )

        then:
        result.getExitCode(  ) == 0
        result.getOutput(  ).contains( "casual/example/echo" )
    }

    def "enqueue then dequeue from other pod."()
    {
        given:
        String payload = "Hello there queue."
        String queue = "ex1"
        String actualCommand = """source \$CASUAL_DOMAIN_HOME/casual.env && echo -n "${payload}" | casual buffer --compose | casual queue -e ${queue}"""
        String[] command = ["sh", "-c", actualCommand]
        String pod = "casual-out-only"

        when:
        ExecResult result = tk.getController(  ).executeCommand( pod, command )

        then:
        result.getExitCode(  ) == 0
        result.getOutput(  ) != ""

        when:
        String queueId = result.getOutput()
        actualCommand = """source \$CASUAL_DOMAIN_HOME/casual.env && casual queue -d ${queue} | casual buffer --extract"""
        command = ["sh", "-c", actualCommand ]
        pod = "casual-in-only"

        ExecResult result2 = tk.getController(  ).executeCommand( pod, command )

        then:
        result2.getExitCode(  ) == 0
        result2.getOutput(  ) == payload
    }

    def "enqueue invalid queue name."()
    {
        given:
        String payload = "Hello there queue."
        String queue = "invalid"
        String actualCommand = """source \$CASUAL_DOMAIN_HOME/casual.env && echo -n "${payload}" | casual buffer --compose | casual queue -e ${queue}"""
        String[] command = ["sh", "-c", actualCommand ]
        String pod = "casual-out-only"

        when:
        ExecResult result = tk.getController(  ).executeCommand( pod, command )

        then:
        result.getExitCode(  ) == 10
        result.getOutput(  ).contains( "queue:no_queue" )
    }

    def "dequeue invalid queue name."()
    {
        given:
        String queue = "invalid"
        String actualCommand = """source \$CASUAL_DOMAIN_HOME/casual.env && casual queue -d ${queue}"""
        String[] command = ["sh", "-c", actualCommand ]
        String pod = "casual-out-only"

        when:
        ExecResult result = tk.getController(  ).executeCommand( pod, command )

        then:
        result.getExitCode(  ) == 10
        result.getOutput(  ).contains( "queue:no_queue" )
    }

    def "Call inbound echo casual to from other casual."()
    {
        given:
        String payload = "This is what i want to echo back."
        String serviceName = "casual/example/echo"
        String pod = "casual-out-only"

        String actualCommand = """source \$CASUAL_DOMAIN_HOME/casual.env && echo -n '${payload}' | casual buffer --compose | casual call --service ${serviceName} | casual buffer --extract"""
        String[] command = ["sh", "-c", actualCommand ]
        when:
        ExecResult result = tk.getController(  ).executeCommand( pod, command )

        then:
        result.getExitCode(  ) == 0
        result.getOutput(  ) == payload
    }

    def "Call inbound uppercase casual to from other casual."()
    {
        given:
        String payload = "This is what i want to echo back."
        String serviceName = "casual/example/uppercase"
        String pod = "casual-out-only"

        String actualCommand = """source \$CASUAL_DOMAIN_HOME/casual.env && echo -n '${payload}' | casual buffer --compose | casual call --service ${serviceName} | casual buffer --extract"""
        String[] command = ["sh", "-c", actualCommand ]
        when:
        ExecResult result = tk.getController(  ).executeCommand( pod, command )

        then:
        result.getExitCode(  ) == 0
        result.getOutput(  ) == payload.toUpperCase()
    }

    def "Call inbound lowercase casual to from other casual."()
    {
        given:
        String payload = "This is what i want to echo back."
        String serviceName = "casual/example/lowercase"
        String pod = "casual-out-only"

        String actualCommand = """source \$CASUAL_DOMAIN_HOME/casual.env && echo -n '${payload}' | casual buffer --compose | casual call --service ${serviceName} | casual buffer --extract"""
        String[] command = ["sh", "-c", actualCommand ]
        when:
        ExecResult result = tk.getController(  ).executeCommand( pod, command )

        then:
        result.getExitCode(  ) == 0
        result.getOutput(  ) == payload.toLowerCase()
    }

    def "Call nonexisting service TPENOENT."()
    {
        given:
        String payload = "This is what i want to echo back."
        String serviceName = "casual/example/invalid"
        String pod = "casual-out-only"

        String actualCommand = """source \$CASUAL_DOMAIN_HOME/casual.env && echo -n '${payload}' | casual buffer --compose | casual call --service ${serviceName} | casual buffer --extract"""
        String[] command = ["sh", "-c", actualCommand ]
        when:
        ExecResult result = tk.getController(  ).executeCommand( pod, command )

        then:
        result.getExitCode(  ) == 0
        result.getOutput(  ).contains( "TPENOENT" )
    }

    def "Call error/system service TPESVRERR."()
    {
        given:
        String payload = "This is what i want to echo back."
        String serviceName = "casual/example/error/system"
        String pod = "casual-out-only"

        String actualCommand = """source \$CASUAL_DOMAIN_HOME/casual.env && echo -n '${payload}' | casual buffer --compose | casual call --service ${serviceName} | casual buffer --extract"""
        String[] command = ["sh", "-c", actualCommand ]
        when:
        ExecResult result = tk.getController(  ).executeCommand( pod, command )

        then:
        result.getExitCode(  ) == 0
        result.getOutput(  ).contains( "TPESVCERR" )
    }
}
