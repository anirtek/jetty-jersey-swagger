package com.cloudian.hfs.handlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Path;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import java.io.IOException;

/**
 * Can be tested with curl using commands like: curl -i -X GET http://127.0.0.1:8080/hello/
 */

public class HelloHandler extends AbstractHandler {

    @Override
    public void handle(String target, Request jettyReq, HttpServletRequest req, HttpServletResponse res) throws IOException {

        res.setContentType("text/html;charset=utf-8");
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().println("<h1>Hello World!</h1>");

        jettyReq.setHandled(true);
    }
}
