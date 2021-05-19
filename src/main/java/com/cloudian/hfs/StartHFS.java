package com.cloudian.hfs;

import com.cloudian.hfs.handlers.*;
import io.swagger.v3.jaxrs2.integration.OpenApiServlet;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import java.util.logging.Level;
import java.util.logging.Logger;


public class StartHFS {

    public static void main(String[] args) throws Exception {
        System.out.println("StartHFS");

        // Create and configure a ThreadPool.
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setName("server");

        // Create a Server instance.
        Server server = new Server(threadPool);

        // HTTP configuration and connection factory.
        HttpConfiguration httpConfig = new HttpConfiguration();
        HttpConnectionFactory http11 = new HttpConnectionFactory(httpConfig);

        // Create a ServerConnector to accept connections from clients.
        ServerConnector connector = new ServerConnector(server, 1, 1, http11);
        connector.setPort(8080);
        connector.setHost("0.0.0.0");
        connector.setAcceptQueueSize(128);
        server.addConnector(connector);

        addHandlers(server);

        // Start the Server so it starts accepting connections from clients.
        try {
            server.start();
            server.join();
        } catch (Exception ex) {
            Logger.getLogger(StartHFS.class.getName()).log(Level.SEVERE, "Fail to start HFS Server", ex);
        } finally {
            server.destroy();
        }

        System.out.println("StartHFS DONE");
    }

    static void addHandlers(final Server server) throws Exception {
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        server.setHandler(contexts);

        ContextHandler logHandler = new ContextHandler("/log");
        logHandler.setHandler(new LoggingHandler());
        contexts.addHandler(logHandler);

        ContextHandler helloHandler = new ContextHandler("/hello");
        helloHandler.setHandler(new HelloHandler());
        contexts.addHandler(helloHandler);

        ContextHandler featureStoreHandler = new ContextHandler("/featurestore");
        featureStoreHandler.setHandler(new FeatureStoreHandler());
        contexts.addHandler(featureStoreHandler);

        ContextHandler featureGroupsHandler = new ContextHandler("/featuregroups");
        featureGroupsHandler.setHandler(new FeatureGroupsHandler());
        contexts.addHandler(featureGroupsHandler);

        ContextHandler recordsHandler = new ContextHandler("/FeatureGroup");
        recordsHandler.setHandler(new RecordsHandler());
        contexts.addHandler(recordsHandler);


        // Setup Jetty Servlet
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");
        contexts.addHandler(servletContextHandler);


        // Setup API resources
        ServletHolder jersey = servletContextHandler.addServlet(ServletContainer.class, "/api/*");
        jersey.setInitOrder(1);
        jersey.setInitParameter("jersey.config.server.provider.packages", "com.cloudian.hfs.handlers;io.swagger.v3.jaxrs2.integration.resources");

        // Expose API definition independently into yaml/json
        ServletHolder openApi = servletContextHandler.addServlet(OpenApiServlet.class, "/openapi/*");
        openApi.setInitOrder(2);
        openApi.setInitParameter("openApi.configuration.resourcePackages", "com.cloudian.handlers;io.swagger.sample.resource");

//        ServletHolder swaggerServlet = servletContextHandler.addServlet(.class, "/swagger-core");
//        swaggerServlet.setInitOrder(2);
//        swaggerServlet.setInitParameter("api.version", "1.0.0");
//        swaggerServlet.setInitParameter("swagger.api.basepath", "http://localhost:8080/api");

//        // Setup Swagger-UI static resources
//        String resourceBasePath = ServicesRunner.class.getResource("/webapp").toExternalForm();
//        servletContextHandler.setWelcomeFiles(new String[] {"index.html"});
//        servletContextHandler.setResourceBase(resourceBasePath);
//        servletContextHandler.addServlet(new ServletHolder(new DefaultServlet()), "/*");

    }

}
