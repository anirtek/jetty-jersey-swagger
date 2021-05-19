package com.cloudian.hfs.handlers;

import javax.inject.Qualifier;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import javax.ws.rs.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/FeatureGroup")
@Produces({"application/json", "application/xml"})
public class RecordsHandler extends AbstractHandler {

    final String PUT_REQUEST = "PUT";
    final String GET_REQUEST = "GET";
    final String DELETE_REQUEST = "DELETE";
    final String HOST = "localhost";
    final String FEATURE_NAME = "FeatureName";
    final String FEATURE_NAMES = "FeatureNames";
    final String FEATURE_GROUP = "FeatureGroup";
    final String RECORD = "Record";
    final String RECORD_IDENTIFIER_FEATURE_NAME = "RecordIdentifierFeatureName";
    final String RECORD_IDENTIFIER_VALUE = "RecordIdentifierValueAsString";
    final String VALUE_AS_STRING = "ValueAsString";
    final String RESPONSE_CONTENT_TYPE_TEXT = "text/html;charset=UTF-8";
    final String RESPONSE_CONTENT_TYPE_JSON = "application/json;charset=UTF-8";

    @Override
    public void handle(String target, Request jettyRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            String featureGroup = target.substring(1);
            Jedis jedis = new Jedis(HOST);

            if(jedis.sismember(FEATURE_GROUP, featureGroup)) {
                if(request.getMethod().equals(PUT_REQUEST)) {
                    putRecord(featureGroup, jedis, request, response);
                }
                else if(request.getMethod().equals(GET_REQUEST)) {
                    getRecord(featureGroup, jedis, request, response);
                }
                else if(request.getMethod().equals(DELETE_REQUEST)) {
                    deleteRecord(featureGroup, jedis, request, response);
                }
            }
            else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType(RESPONSE_CONTENT_TYPE_TEXT);
                response.getWriter().println("FeatureGroup:"+ featureGroup +" does not exist");
            }
        }catch (NullPointerException | JSONException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(RESPONSE_CONTENT_TYPE_TEXT);
            response.getWriter().println(e.getClass().getSimpleName() +": " +e.getMessage());
        }
        catch (JedisException e) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType(RESPONSE_CONTENT_TYPE_TEXT);
            response.getWriter().println(e.getClass().getSimpleName() +": " +e.getMessage());
        }
        catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType(RESPONSE_CONTENT_TYPE_TEXT);
            response.getWriter().println(e.getClass().getSimpleName() +": " +e.getMessage());
        }

        jettyRequest.setHandled(true);
    }

    //Hash Record:+<FG>+:+<RecordIdentifier> -> Features as keys and its values as values

    @PUT
    @Path("/{id}")
    @Consumes("application/json")
    public void putRecord(@PathParam("id") String featureGroup, Jedis jedis, @RequestBody HttpServletRequest request, HttpServletResponse response) throws IOException, JedisException, JSONException {
        String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        JSONObject jsonRequestBody = new JSONObject(requestBody);
        JSONArray record = (JSONArray) jsonRequestBody.get(RECORD);
        String featureGroupKey = FEATURE_GROUP + ":" + featureGroup;
        String recordKey = null, recordIdentifier;

        recordIdentifier = jedis.hget(featureGroupKey,RECORD_IDENTIFIER_FEATURE_NAME);

        for(int i = 0; i < record.length(); i++) {
            if(recordIdentifier.equals(String.valueOf(record.getJSONObject(i).get(FEATURE_NAME)))) {
                recordKey = RECORD+":"+featureGroup+":"+record.getJSONObject(i).get(VALUE_AS_STRING);
                break;
            }
        }

        if(recordKey == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(RESPONSE_CONTENT_TYPE_TEXT);
            response.getWriter().println("RecordIdentifierFeature not present");
            return;
        }

        for(int i = 0; i < record.length(); i++) {
            jedis.hset(recordKey, String.valueOf(record.getJSONObject(i).get(FEATURE_NAME)),
                    String.valueOf(record.getJSONObject(i).get(VALUE_AS_STRING)));
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }

    @GET
    @Path("/{id}")
    public void getRecord(@PathParam("id") String featureGroup, @Schema Jedis jedis, @RequestBody HttpServletRequest request, HttpServletResponse response) throws IOException, JedisException, JSONException {
        String recordIdValue = request.getParameter(RECORD_IDENTIFIER_VALUE);
        String[] featureNames = request.getParameterValues(FEATURE_NAME);
        String recordKey = RECORD+":"+featureGroup+":"+recordIdValue;
        JSONObject jsonResponseBody = new JSONObject();
        JSONArray featuresArray = new JSONArray();

        if(!jedis.exists(recordKey)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType(RESPONSE_CONTENT_TYPE_TEXT);
            response.getWriter().println("RecordIdentifierFeatureValue:"+recordIdValue+" not present");
            return;
        }

        Map<String, String> recordMap = jedis.hgetAll(recordKey);

        if(featureNames == null) {
            Iterator<Map.Entry<String, String>> iterator = recordMap.entrySet().iterator();

            while(iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                JSONObject featurePair = new JSONObject();
                featurePair.put(FEATURE_NAME,entry.getKey());
                featurePair.put(VALUE_AS_STRING, entry.getValue());
                featuresArray.put(featurePair);
            }
        }
        else {
            for(int i = 0; i < featureNames.length; i++) {
                String featureValue = recordMap.get(featureNames[i]);
                if(featureValue != null) {
                    JSONObject featurePair = new JSONObject();
                    featurePair.put(FEATURE_NAME, featureNames[i]);
                    featurePair.put(VALUE_AS_STRING, featureValue);
                    featuresArray.put(featurePair);
                }
            }
        }

        jsonResponseBody.put(RECORD, featuresArray);
        response.setContentType(RESPONSE_CONTENT_TYPE_JSON);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(jsonResponseBody);
    }

    @DELETE
    @Path("/{id}")
    @Consumes("application/json")
    public void deleteRecord(@PathParam("id") String featureGroup, @Schema Jedis jedis, @RequestBody HttpServletRequest request, HttpServletResponse response) throws IOException, JedisException, JSONException {
        String recordIdValue = request.getParameter(RECORD_IDENTIFIER_VALUE);
        String recordKey = RECORD+":"+featureGroup+":"+recordIdValue;

        if(!jedis.exists(recordKey)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(RESPONSE_CONTENT_TYPE_TEXT);
            response.getWriter().println("RecordIdentifierFeatureValue:"+recordIdValue+ " not present");
            return;
        }

        jedis.del(recordKey);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
