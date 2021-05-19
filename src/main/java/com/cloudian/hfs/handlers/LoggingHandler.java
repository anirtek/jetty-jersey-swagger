package com.cloudian.hfs.handlers;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.ws.rs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/log")
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
