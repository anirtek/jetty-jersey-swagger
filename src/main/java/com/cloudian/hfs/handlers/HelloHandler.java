package com.cloudian.hfs.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.ws.rs.*;
import java.io.IOException;

/**
 * Can be tested with curl using commands like: curl -i -X GET http://127.0.0.1:8080/hello/
 */
@Path("/hello")
public class HelloHandler extends AbstractHandler {

    @Override
    public void handle(String target, Request jettyReq, HttpServletRequest req, HttpServletResponse res) throws IOException {

        res.setContentType("text/html;charset=utf-8");
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().println("<h1>Hello World!</h1>");

        jettyReq.setHandled(true);
    }
}
