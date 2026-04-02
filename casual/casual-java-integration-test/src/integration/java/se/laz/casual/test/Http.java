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
        String host = connection.getHostName();
        int port = connection.getPort();

        HttpClient httpClient = HttpClient.newBuilder(  ).build(  );
        HttpRequest request = HttpRequest.newBuilder( )
                .uri( URI.create( "http://" + host + ":" + port + path ) )
                .header( "Content-Type", contentType )
                .POST( HttpRequest.BodyPublishers.ofString( body ) )
                .build(  );

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
