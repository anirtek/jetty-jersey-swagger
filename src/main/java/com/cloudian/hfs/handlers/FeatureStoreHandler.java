package com.cloudian.hfs.handlers;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import javax.ws.rs.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.stream.Collectors;

enum FeatureGroupStatus {

    CREATING("Creating"),
    CREATED("Created"),
    CREATEFAILED("CreateFailed"),
    DELETING("Deleting"),
    DELETEFAILED("DeleteFailed");

    private String status;

    FeatureGroupStatus(String status) {
        this.status = status;
    }

    public String getValue() {
        return this.status;
    }

}

@Path("/featurestore/")
@Produces({"application/json", "application/xml"})
public class FeatureStoreHandler extends AbstractHandler {

    final String HOST = "localhost";
    final String FEATURE_GROUP = "FeatureGroup";
    final String FEATURE_GROUP_NAME = "FeatureGroupName";
    final String FEATURE_GROUP_STATUS = "FeatureGroupStatus";
    final String FEATURE_DEFINITIONS = "FeatureDefinitions";
    final String FEATURE_NAME = "FeatureName";
    final String FEATURE_TYPE = "FeatureType";
    final String ONLINE_STORE_CONFIG = "OnlineStoreConfig";
    final String DESCRIPTION = "Description";
    final String EVENT_TIME_FEATURE_NAME = "EventTimeFeatureName";
    final String ENABLE_ONLINE_STORE = "EnableOnlineStore";
    final String RECORD_IDENTIFIER_FEATURE_NAME = "RecordIdentifierFeatureName";
    final String RESPONSE_CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
    final String RESPONSE_CONTENT_TYPE_TEXT = "text/html;charset=UTF-8";
    final String FEATURE_GROUP_ARN = "FeatureGroupArn";
    final String FEATURE_GROUP_ARN_VALUE = "randomValue";
    final String POST_REQUEST = "POST";
    final String DELETE_REQUEST = "DELETE";
    final String GET_REQUEST = "GET";
    final String CREATION_TIME = "CreationTime";
    final String FAILURE_REASON = "FailureReason";
    final String NEXT_TOKEN = "NextToken";
    final String ROLE_ARN = "RoleArn";
    final String OFFLINE_STORE_CONFIG = "OfflineStoreConfig";
    final String OFFLINE_STORE_STATUS = "OfflineStoreStatus";
    final String SECURITY_CONFIG = "SecurityConfig";

    @Override
    public void handle(String target, Request jettyRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            if(request.getMethod().equals(POST_REQUEST)) {
                if(target.equals("/")) {
                    createFeatureGroup(jettyRequest, request, response);
                }
                else if(target.equals("/describe")){
                    describeFeatureGroup(jettyRequest, request, response);
                }
                else if(target.equals("/delete")){
                    deleteFeatureGroup(jettyRequest, request, response);
                }
            }
        }catch (JSONException e) {
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

    @POST
    @Path("/create")
    @Consumes("application/json")
    public void createFeatureGroup(@RequestBody Request jettyRequest, @RequestBody HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException, JedisException {
        String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        JSONObject jsonRequestBody = new JSONObject(requestBody);
        Jedis jedis = new Jedis(HOST);
        JSONArray featureDefinitions = (JSONArray) jsonRequestBody.get(FEATURE_DEFINITIONS);
        String featureDefinitionsKey = FEATURE_DEFINITIONS + ":" + jsonRequestBody.get(FEATURE_GROUP_NAME);
        JSONObject onlineStoreConfig = (JSONObject) jsonRequestBody.get(ONLINE_STORE_CONFIG);
        String featureGroupKey = FEATURE_GROUP + ":" + jsonRequestBody.get(FEATURE_GROUP_NAME);
        JSONObject responseObject = new JSONObject();

        if(jedis.sismember(FEATURE_GROUP, String.valueOf(jsonRequestBody.get(FEATURE_GROUP_NAME)))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(RESPONSE_CONTENT_TYPE_TEXT);
            response.getWriter().println("FeatureGroupName already exists");
            return;
        }

        jedis.sadd(FEATURE_GROUP, String.valueOf(jsonRequestBody.get(FEATURE_GROUP_NAME)));

        for(int i = 0; i < featureDefinitions.length(); i++) {
            jedis.hset(featureDefinitionsKey, String.valueOf(featureDefinitions.getJSONObject(i).get(FEATURE_NAME)),
                    String.valueOf(featureDefinitions.getJSONObject(i).get(FEATURE_TYPE)));
        }

        jedis.hset(featureGroupKey, FEATURE_GROUP_NAME, String.valueOf(jsonRequestBody.get(FEATURE_GROUP_NAME)));
        jedis.hset(featureGroupKey, FEATURE_GROUP_STATUS, FeatureGroupStatus.CREATING.getValue());
        jedis.hset(featureGroupKey, DESCRIPTION, String.valueOf(jsonRequestBody.get(DESCRIPTION)));
        jedis.hset(featureGroupKey, EVENT_TIME_FEATURE_NAME, String.valueOf(jsonRequestBody.get(EVENT_TIME_FEATURE_NAME)));
        jedis.hset(featureGroupKey, ENABLE_ONLINE_STORE, String.valueOf(onlineStoreConfig.get(ENABLE_ONLINE_STORE)));
        jedis.hset(featureGroupKey, RECORD_IDENTIFIER_FEATURE_NAME, String.valueOf(jsonRequestBody.get(RECORD_IDENTIFIER_FEATURE_NAME)));
        jedis.hset(featureGroupKey, CREATION_TIME, String.valueOf(new Timestamp(System.currentTimeMillis())));
        jedis.hset(featureGroupKey, FEATURE_GROUP_STATUS, FeatureGroupStatus.CREATED.getValue());

        response.setContentType(RESPONSE_CONTENT_TYPE_JSON);
        response.setStatus(HttpServletResponse.SC_OK);
        responseObject.put(FEATURE_GROUP_ARN,FEATURE_GROUP_ARN_VALUE);
        response.getWriter().println(responseObject);
    }

    @DELETE
    @Path("/delete")
    @Consumes("application/json")
    public void deleteFeatureGroup(@RequestBody Request jettyRequest, @RequestBody HttpServletRequest request, HttpServletResponse response) throws IOException, JedisException, JSONException {
        String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        JSONObject jsonRequestBody = new JSONObject(requestBody);
        Jedis jedis = new Jedis(HOST);
        String featureGroupName = String.valueOf(jsonRequestBody.get(FEATURE_GROUP_NAME));
        String featureGroupKey = FEATURE_GROUP + ":" + jsonRequestBody.get(FEATURE_GROUP_NAME);
        String featureDefinitionsKey = FEATURE_DEFINITIONS + ":" + jsonRequestBody.get(FEATURE_GROUP_NAME);

        if(!jedis.sismember(FEATURE_GROUP, featureGroupName)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(RESPONSE_CONTENT_TYPE_TEXT);
            response.getWriter().println("FeatureGroup: "+ featureGroupName +" not found");
        }
        else {
            jedis.hset(featureGroupKey, FEATURE_GROUP_STATUS, FeatureGroupStatus.DELETING.getValue());
            jedis.del(featureGroupKey);
            jedis.del(featureDefinitionsKey);
            jedis.srem(FEATURE_GROUP, featureGroupName);
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    @GET
    @Path("/describe")
    public void describeFeatureGroup(@RequestBody Request jettyRequest, @RequestBody HttpServletRequest request, HttpServletResponse response) throws IOException, JedisException, JSONException {
        String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        JSONObject jsonRequestBody = new JSONObject(requestBody);
        Jedis jedis = new Jedis(HOST);
        String featureGroupName = String.valueOf(jsonRequestBody.get(FEATURE_GROUP_NAME));
        String featureGroupKey = FEATURE_GROUP + ":" + featureGroupName;
        String featureDefinitionsKey = FEATURE_DEFINITIONS + ":" + jsonRequestBody.get(FEATURE_GROUP_NAME);
        JSONObject jsonResponseBody = new JSONObject();
        JSONArray featureDefinitionsArray = new JSONArray();
        JSONObject onlineStoreConfig = new JSONObject();

        if(!jedis.sismember(FEATURE_GROUP, featureGroupName)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(RESPONSE_CONTENT_TYPE_TEXT);
            response.getWriter().println("FeatureGroup: "+ featureGroupName +" not found");
            return;
        }

        Map<String, String> featureDefinitionsMap = jedis.hgetAll(featureDefinitionsKey);
        Iterator<Entry<String, String>> iterator = featureDefinitionsMap.entrySet().iterator();

        while(iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            JSONObject featureDefinition = new JSONObject();
            featureDefinition.put(FEATURE_NAME,entry.getKey());
            featureDefinition.put(FEATURE_TYPE, entry.getValue());
            featureDefinitionsArray.put(featureDefinition);
        }

        onlineStoreConfig.put(ENABLE_ONLINE_STORE, true);
        onlineStoreConfig.put(SECURITY_CONFIG, JSONObject.NULL);

        Map<String, String> featureGroupMap = jedis.hgetAll(featureGroupKey);

        jsonResponseBody.put(CREATION_TIME, featureGroupMap.get(CREATION_TIME));
        jsonResponseBody.put(DESCRIPTION, featureGroupMap.get(DESCRIPTION));
        jsonResponseBody.put(EVENT_TIME_FEATURE_NAME, featureGroupMap.get(EVENT_TIME_FEATURE_NAME));
        jsonResponseBody.put(FAILURE_REASON, JSONObject.NULL);
        jsonResponseBody.put(FEATURE_DEFINITIONS, featureDefinitionsArray);
        jsonResponseBody.put(FEATURE_GROUP_ARN, JSONObject.NULL);
        jsonResponseBody.put(FEATURE_GROUP_NAME, featureGroupMap.get(FEATURE_GROUP_NAME));
        jsonResponseBody.put(FEATURE_GROUP_STATUS, featureGroupMap.get(FEATURE_GROUP_STATUS));
        jsonResponseBody.put(NEXT_TOKEN, JSONObject.NULL);
        jsonResponseBody.put(OFFLINE_STORE_CONFIG, JSONObject.NULL);
        jsonResponseBody.put(OFFLINE_STORE_STATUS, JSONObject.NULL);
        jsonResponseBody.put(ONLINE_STORE_CONFIG, onlineStoreConfig);
        jsonResponseBody.put(RECORD_IDENTIFIER_FEATURE_NAME, featureGroupMap.get(RECORD_IDENTIFIER_FEATURE_NAME));
        jsonResponseBody.put(ROLE_ARN, JSONObject.NULL);
        response.setContentType(RESPONSE_CONTENT_TYPE_JSON);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(jsonResponseBody);
    }
}
