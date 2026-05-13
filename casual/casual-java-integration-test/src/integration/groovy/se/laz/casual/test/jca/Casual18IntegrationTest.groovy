/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.jca

import se.laz.casual.test.CasualResources
import se.laz.casual.test.Http
import se.laz.casual.test.tdk8s.connection.KubeConnection
import spock.lang.Ignore

import java.net.http.HttpResponse

class Casual18IntegrationTest extends AbstractCasualIntegrationTest
{
    @Override
    String getCasualImage()
    {
        return CasualResources.CASUAL_CONTAINER_IMAGE_18
    }

    def "Call echo service with headers for inbound call to casual java."()
    {
        given:
        String payload = "This is the message to echo."
        HttpResponse<String> response
        Map<String,List<String>> headers = ["Content-Type": ["application/casual-x-octet"], "test":["my header."] ]

        when:
        try( KubeConnection con = tk.getConnection( "casual-java-svc", 8080 ) )
        {
            response = Http.post( con, "/casual/casual%2Fexample%2Fjava%2Fecho/?includeHeaders=test", headers, payload )
        }

        then:
        response != null
        response.statusCode(  ) == 200
        response.body(  ) == payload
        response.headers(  ).firstValue( "test" ).get() == "my header."
    }

    def "Call echo service with headers for outbound call to casual."()
    {
        given:
        String payload = "This is the message to echo."
        HttpResponse<String> response
        Map<String,List<String>> headers = ["Content-Type": ["application/casual-x-octet"], "test":["my header."] ]



        when:
        try( KubeConnection con = tk.getConnection( "casual-java-svc", 8080 ) )
        {
            response = Http.post( con, "/casual/casual%2Fexample%2Fecho/?includeHeaders=test", headers, payload )
        }

        then:
        response != null
        response.statusCode(  ) == 200
        response.body(  ) == payload
        response.headers(  ).firstValue( "test" ).get() == "my header."
    }
}
