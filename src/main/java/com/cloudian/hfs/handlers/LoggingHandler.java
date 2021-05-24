package com.cloudian.hfs.handlers;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Path;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LoggingHandler extends AbstractHandler {

    static Logger logger = (Logger) LoggerFactory.getLogger(LoggingHandler.class.getName());

    @Override
    public void handle(String target, Request jettyReq, HttpServletRequest req, HttpServletResponse res) {
        res.setContentType("text/plain");
        res.setStatus(HttpServletResponse.SC_OK);
        logger.info("LoggingHandler is called");
        jettyReq.setHandled(true);
    }
}
