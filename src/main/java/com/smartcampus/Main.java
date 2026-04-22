package com.smartcampus;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.File;

public class Main {
    public static final String BASE_URI = "http://localhost:8080/";

    public static Tomcat startServer() throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector(); // Initialize the default connector

        // Add a dummy context
        Context context = tomcat.addContext("", new File(".").getAbsolutePath());

        // Create Jersey ResourceConfig
        ResourceConfig rc = ResourceConfig.forApplicationClass(SmartCampusApplication.class);

        // Add Jersey Servlet to Tomcat context
        Tomcat.addServlet(context, "jersey-container-servlet", new ServletContainer(rc));
        context.addServletMappingDecoded("/api/v1/*", "jersey-container-servlet");

        tomcat.start();
        return tomcat;
    }

    public static void main(String[] args) {
        try {
            final Tomcat server = startServer();
            System.out.println(String.format("Smart Campus API Server started!\n" +
                    "Discovery Endpoint available at: " + "%sapi/v1\n" +
                    "Hit Ctrl-C to stop it...", BASE_URI));
            server.getServer().await();
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}
