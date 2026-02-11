/*
 * Copyright (c) 2024 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test


import se.laz.casual.test.tdk8s.TestKube
import se.laz.casual.test.tdk8s.connection.KubeConnection
import spock.lang.Shared
import spock.lang.Specification

import java.net.http.HttpResponse

class CasualJavaIntegrationTest extends Specification
{

    @Shared
    TestKube tk

    def setupSpec()
    {
        tk = TestKube.newBuilder(  )
                .addPod( CasualJavaResources.SIMPLE_CASUAL_JAVA_POD_NAME, CasualJavaResources.SIMPLE_CASUAL_JAVA_POD )
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
