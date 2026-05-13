/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test;

import se.laz.casual.test.tdk8s.connection.KubeConnection;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple HTTP Client for use against a tdk8s connection.
 */
public class Http
{
    public static HttpResponse<String> get( KubeConnection connection, String path, String contentType ) throws IOException, InterruptedException
    {
        String host = connection.getHostName();
        int port = connection.getPort();

        HttpClient httpClient = HttpClient.newBuilder(  ).build(  );
        HttpRequest request = HttpRequest.newBuilder( )
                .uri( URI.create( "http://" + host + ":" + port + path ) )
                .header( "Accept", contentType )
                .GET( )
                .build(  );

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static HttpResponse<String> post( KubeConnection connection, String path, String contentType, String body ) throws IOException, InterruptedException
    {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put( "Content-Type", Collections.singletonList( contentType ) );
        return post( connection, path, headers, body );
    }

    public static HttpResponse<String> post( KubeConnection connection, String path, Map<String,List<String>> headers, String body ) throws IOException, InterruptedException
    {
        String host = connection.getHostName();
        int port = connection.getPort();

        HttpClient httpClient = HttpClient.newBuilder(  ).build(  );
        HttpRequest.Builder builder = HttpRequest.newBuilder( )
                .uri( URI.create( "http://" + host + ":" + port + path ) );

        for( Map.Entry<String,List<String>> entry: headers.entrySet() )
        {
            for( String value: entry.getValue() )
            {
                builder.header( entry.getKey(), value );
            }
        }

        HttpRequest request = builder.POST( HttpRequest.BodyPublishers.ofString( body ) )
                .build(  );

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
