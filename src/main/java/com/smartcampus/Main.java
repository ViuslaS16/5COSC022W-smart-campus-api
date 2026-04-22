package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class Main {
    public static final String BASE_URI = "http://localhost:8080/";

    public static HttpServer startServer() {
        final ResourceConfig rc = ResourceConfig.forApplicationClass(SmartCampusApplication.class);
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) {
        try {
            final HttpServer server = startServer();
            System.out.println(String.format("Smart Campus API Server started!\n" +
                    "Discovery Endpoint available at: " + "%sapi/v1\n" +
                    "Hit Ctrl-C to stop it...", BASE_URI));
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.err.println("Server interrupted: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}
