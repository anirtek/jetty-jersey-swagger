package com.cloudian.hfs.handlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@Path("/featuregroups")
@Produces({"application/json", "application/xml"})
public class FeatureGroupsHandler extends AbstractHandler {

    final String HOST = "localhost";
    final String FEATURE_GROUP = "FeatureGroup";
    final String POST_REQUEST = "POST";
    final String CREATION_TIME = "CreationTime";
    final String FEATURE_GROUP_ARN = "FeatureGroupArn";
    final String FEATURE_GROUP_NAME = "FeatureGroupName";
    final String FEATURE_GROUP_STATUS = "FeatureGroupStatus";
    final String FEATURE_GROUP_SUMMARIES = "FeatureGroupSummaries";
    final String OFFLINE_STORE_STATUS = "OfflineStoreStatus";
    final String RESPONSE_CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
    final String RESPONSE_CONTENT_TYPE_TEXT = "text/html;charset=UTF-8";
    final String NEXT_TOKEN = "NextToken";
    final String CREATION_TIME_AFTER = "CreationTimeAfter";
    final String CREATION_TIME_BEFORE = "CreationTimeBefore";
    final String FEATURE_GROUP_STATUS_EQUALS = "FeatureGroupStatusEquals";
    final String MAX_RESULTS = "MaxResults";
    final String NAME = "Name";
    final String NAME_CONTAINS = "NameContains";
    final String OFFLINE_STORE_STATUS_EQUALS = "OfflineStoreStatusEquals";
    final String SORT_BY = "SortBy";
    final String SORT_ORDER = "SortOrder";
    final String ASCENDING = "Ascending";
    final String DESCENDING = "Descending";


    @Override
    public void handle(String target, Request jettyRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(request.getMethod().equals(POST_REQUEST)) {
            try {
                listFeatureGroups(jettyRequest, request, response);
            } catch (JSONException e) {
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
        }
        jettyRequest.setHandled(true);
    }

    @GET
    @Path("/")
    public void listFeatureGroups(Request jettyRequest,  HttpServletRequest request,  HttpServletResponse response) throws IOException, ParseException, JedisException, JSONException {
        String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        JSONObject jsonRequestBody = new JSONObject(requestBody);
        Jedis jedis = new Jedis(HOST);

        JSONObject jsonResponseBody = new JSONObject();
        JSONArray featureGroupSummaries = new JSONArray();
        Set<String> featureGroups = jedis.smembers(FEATURE_GROUP);

        for(String featureGroup: featureGroups) {
            if(!jsonRequestBody.has(SORT_BY) && jsonRequestBody.has(MAX_RESULTS)) {
                if(featureGroupSummaries.length() == jsonRequestBody.getInt(MAX_RESULTS)){
                    break;
                }
            }
            JSONObject featureGroupSummary = new JSONObject();
            String featureGroupKey = FEATURE_GROUP + ":" + featureGroup;
            if(jsonRequestBody.has(CREATION_TIME_AFTER)) {
                if(getTimestamp(jedis.hget(featureGroupKey, CREATION_TIME)).before(getTimestamp(jsonRequestBody.getString(CREATION_TIME_AFTER)))) {
                    continue;
                }
            }
            if(jsonRequestBody.has(CREATION_TIME_BEFORE)) {
                if(getTimestamp(jedis.hget(featureGroupKey, CREATION_TIME)).after(getTimestamp(jsonRequestBody.getString(CREATION_TIME_BEFORE)))) {
                    continue;
                }
            }
            featureGroupSummary.put(CREATION_TIME, jedis.hget(featureGroupKey, CREATION_TIME));
            featureGroupSummary.put(FEATURE_GROUP_ARN, JSONObject.NULL);
            if(jsonRequestBody.has(NAME_CONTAINS)) {
                if(!((jedis.hget(featureGroupKey, FEATURE_GROUP_NAME)).contains(jsonRequestBody.getString(NAME_CONTAINS)))) {
                    continue;
                }
            }
            featureGroupSummary.put(FEATURE_GROUP_NAME, jedis.hget(featureGroupKey, FEATURE_GROUP_NAME));
            if(jsonRequestBody.has(FEATURE_GROUP_STATUS_EQUALS)) {
                if(!((jsonRequestBody.getString(FEATURE_GROUP_STATUS_EQUALS)).equals(jedis.hget(featureGroupKey, FEATURE_GROUP_STATUS)))) {
                    continue;
                }
            }
            featureGroupSummary.put(FEATURE_GROUP_STATUS, jedis.hget(featureGroupKey, FEATURE_GROUP_STATUS));
            featureGroupSummary.put(OFFLINE_STORE_STATUS, JSONObject.NULL);
            featureGroupSummaries.put(featureGroupSummary);
        }

        if(jsonRequestBody.has(SORT_BY)) {
            featureGroupSummaries = sort(featureGroupSummaries, jsonRequestBody.getString(SORT_BY),
                    jsonRequestBody.has(SORT_ORDER)? jsonRequestBody.getString(SORT_ORDER): ASCENDING,
                    jsonRequestBody.has(MAX_RESULTS)? jsonRequestBody.getInt(MAX_RESULTS): -1);
        }
        jsonResponseBody.put(FEATURE_GROUP_SUMMARIES, featureGroupSummaries);
        jsonResponseBody.put(NEXT_TOKEN, JSONObject.NULL);
        response.setContentType(RESPONSE_CONTENT_TYPE_JSON);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(jsonResponseBody);
    }

    private Timestamp getTimestamp(String time) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        Date parsedDate = dateFormat.parse(time);
        Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
        return timestamp;
    }

    private JSONArray sort(JSONArray jsonArray, String sortKey, String sortOrder, int maxResults) {
        JSONArray sortedArray = new JSONArray();
        if(sortKey.equals(NAME)) {
            sortKey = FEATURE_GROUP_NAME;
        }
        String finalSortKey = sortKey;
        PriorityQueue<JSONObject> pq;

        if(sortOrder.equals(ASCENDING)) {
            pq = new PriorityQueue<>(Comparator.comparing(a -> a.getString(finalSortKey)));
        }
        else if(sortOrder.equals(DESCENDING)){
            pq = new PriorityQueue<>((a,b) -> (b.getString(finalSortKey)).compareTo(a.getString(finalSortKey)));
        }
        else {
            return jsonArray;
        }

        for(int i = 0; i < jsonArray.length(); i++) {
            pq.offer(jsonArray.getJSONObject(i));
        }

        while(!pq.isEmpty()) {
            sortedArray.put(pq.poll());
            if(maxResults >= 0 && sortedArray.length() == maxResults) {
                break;
            }
        }

        return sortedArray;
    }
}
