package com.manulife.edl.atlas.tags;

import com.manulife.edl.atlas.tags.tagObject.HiveColumn;
import com.manulife.edl.atlas.tags.tagObject.HiveTable;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Vector;

public class RestfulHandler {

    private static final Logger LOGGER = LogManager.getLogger();
    private final String SCHEME = "https://";

    private String cluster;
    private String knoxHost;
    private int knoxPort;
    private String atlasHost;
    private int atlasPort;
    private String atlasService;
    RequestConfig autoRedirectReqConfig;
    private HttpClient httpClient;
    final HttpClientContext context = HttpClientContext.create();
    final static int POST = 0;
    final static int GET = 1;
    final static int DELETE = 2;

    public RestfulHandler(String cluster, String atlasHost, int atlasPort,
                          String user, String pharse) {
        this.atlasHost = atlasHost;
        this.atlasPort = atlasPort;
        this.atlasService = SCHEME + atlasHost + ":" + atlasPort;
        this.cluster = cluster;
        this.autoRedirectReqConfig = RequestConfig.copy(RequestConfig.DEFAULT).setRedirectsEnabled(true).
                setRelativeRedirectsAllowed(true).build();

        HttpHost targetHost = new HttpHost(atlasHost, atlasPort, "https");
        AuthCache authCache = new BasicAuthCache();
        authCache.put(targetHost, new BasicScheme());

        // Add AuthCache to the execution context
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials
                = new UsernamePasswordCredentials(user, pharse);
        provider.setCredentials(AuthScope.ANY, credentials);

        context.setCredentialsProvider(provider);
        context.setAuthCache(authCache);
        context.setCookieStore(new BasicCookieStore());
        httpClient = HttpClientBuilder.create().build();
    }

    public RestfulHandler(String cluster, String knoxHost, int knoxPort, String atlasHost,
                          int atlasPort, String user, String pharse) {
        this.knoxHost = knoxHost;
        this.knoxPort = knoxPort;
        this.atlasHost = atlasHost;
        this.atlasPort = atlasPort;
        this.atlasService = SCHEME + atlasHost + ":" + atlasPort;
        this.cluster = cluster;
        this.autoRedirectReqConfig = RequestConfig.copy(RequestConfig.DEFAULT).setRedirectsEnabled(true).
                setRelativeRedirectsAllowed(true).build();

        HttpHost targetHost = new HttpHost(knoxHost, knoxPort, "https");
        AuthCache authCache = new BasicAuthCache();
        authCache.put(targetHost, new BasicScheme());

        // Add AuthCache to the execution context
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials
                = new UsernamePasswordCredentials(user, pharse);
        provider.setCredentials(AuthScope.ANY, credentials);

        context.setCredentialsProvider(provider);
        context.setAuthCache(authCache);
        context.setCookieStore(new BasicCookieStore());
        httpClient = HttpClientBuilder.create().build();
    }

    public String makeRestfulCall(URIBuilder uri, int method) {
        return makeRestfulCall(uri, method, "");
    }

    public String makeRestfulCall(URIBuilder uri, int method, String json) {
        StringBuffer result = new StringBuffer();

        try {
            HttpGet getRequest;
            HttpPost postRequest;
            HttpDelete delRequest;
            HttpResponse response = null;

            switch (method) {
                case GET:
                    getRequest = new HttpGet(uri.build());
                    getRequest.setConfig(autoRedirectReqConfig);
                    getRequest.addHeader("Accept", "application/json");
                    LOGGER.info(getRequest);
                    response = httpClient.execute(getRequest, context);
                    break;
                case DELETE:
                    delRequest = new HttpDelete(uri.build());
                    delRequest.setConfig(autoRedirectReqConfig);
                    delRequest.addHeader("Accept", "application/json");
                    LOGGER.info(delRequest);
                    response = httpClient.execute(delRequest, context);
                    break;
                case POST:
                    postRequest = new HttpPost(uri.build());
                    postRequest.setConfig(autoRedirectReqConfig);
                    postRequest.addHeader("Content-Type", "application/json");
                    postRequest.addHeader("Accept", "application/json");
                    HttpEntity entity;
                    entity = new StringEntity(json);
                    postRequest.setEntity(entity);
                    LOGGER.info(postRequest);
                    LOGGER.info(json);
                    response = httpClient.execute(postRequest, context);
                    break;
                default:
                    break;
            }

            if (response != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                LOGGER.info(response.getStatusLine().getStatusCode());
                LOGGER.info(response);
                if (statusCode != 204) {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    String line = "";

                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                    LOGGER.info(result.toString());
                }
            }
        } catch (Exception ex) {
            LOGGER.catching(ex);
        } finally {

        }

        return result.toString();
    }

    /**
     * The purpose of this method is to finish the authentication.
     * The core entity matters the most here are the hadoop-jwt and AtlasSessionID Cookies
     * which contains the authentication token returned from the Knox and Atlas respectively.
     * <p>
     * In order to fetch and store the cookies from Knox in the
     * CookieStore {@link org.apache.http.impl.client.BasicCookieStore} to complete the authentication of
     * HDP, this implementation calls Knox for proxy request and then
     * the HttpClient will take care of the rest including the cookie part.
     *
     * @param
     * @return
     * @author Tao
     * @date
     */
    public void setupKnoxCookie() throws URISyntaxException {
        URIBuilder uri = new URIBuilder(SCHEME + knoxHost + ":" + knoxPort +
                "/gateway/knoxsso/api/v1/websso");
        uri.addParameter("originalUrl", SCHEME + atlasHost + ":" + atlasPort);
        String result = makeRestfulCall(uri, POST);
        LOGGER.info(result);
    }

    public Vector<HiveTable> getTableGuid(Vector<HiveTable> tables) throws JSONException, URISyntaxException {

        for (HiveTable table : tables) {
            JSONObject o;
            URIBuilder uri = new URIBuilder(atlasService + "/api/atlas/v2/search/basic");
            uri.addParameter("limit", "25");
            uri.addParameter("query", "where qualifiedName='" + table.getSchemaName() + "." + table.getTableName().toLowerCase() + "@" + cluster + "'");
            //uri.addParameter("typeName","hive_table");

            String result = makeRestfulCall(uri, GET);
            o = new JSONObject(result);
            LOGGER.info(result);
            JSONArray entities = o.getJSONArray("entities");
            JSONObject object = (JSONObject) entities.get(0);
            LOGGER.info(object.getString("guid"));
            table.setGuid(object.getString("guid"));
        }
        return tables;
    }

    public String getColumGuid(HiveColumn column) throws URISyntaxException {
        URIBuilder uri = new URIBuilder(atlasService + "/api/atlas/v2/search/dsl");
        uri.addParameter("limit", "25");
        uri.addParameter("offset", "0");

        //uri.addParameter("query", "where qualifiedName='"+column.getSchemaName()+"."+column.getTableName()+"."+column.getColumnName()+"@"+cluster+"', columns");
        uri.addParameter("query", "hive_column where qualifiedName='" + column.getSchemaName() + "." + column.getTableName() + "." + column.getColumnName() + "@" + cluster + "' and __state='ACTIVE'");
        //uri.addParameter("typeName","hive_table");
        //uri.addParameter("typeName","hive_column");

        return makeRestfulCall(uri, GET);
    }

    public void setTagByColumnGuid(String columnGuid, String tag) throws URISyntaxException {
        URIBuilder uri = new URIBuilder(atlasService + "/api/atlas/v2/entity/bulk/classification");
        makeRestfulCall(uri, POST, "{\"classification\":{\"typeName\":\"" + tag + "\",\"attributes\":{}},\"entityGuids\":[\"" + columnGuid + "\"]}");
//		System.out.println("set completed rows " + ++StandaloneApp.i);
    }

    public void setTagByColumnMultipleGuid(Vector<HiveColumn> col, String tag) throws URISyntaxException {
        Iterator<HiveColumn> itr = col.iterator();
        StringBuilder sb = new StringBuilder();
        while (itr.hasNext()) {
            sb.append('"');
            sb.append(itr.next().getGuid());
            sb.append('"');
            if (itr.hasNext()) {
                sb.append(",");
            }
        }

        URIBuilder uri = new URIBuilder(atlasService + "/api/atlas/v2/entity/bulk/classification");
        LOGGER.info("Tagging entities for  " + tag);
        // TODO Verification on remove propagation on entity delete
        // add in propagate and removePropagationsOnEntityDelete entries
        makeRestfulCall(uri, POST, "{\"classification\":{\"typeName\":\"" + tag + "\",\"attributes\":{}, " +
                "\"propagate\":true, \"removePropagationsOnEntityDelete\":true}," +
                "\"entityGuids\":[" + sb.toString() + "]}");
    }

    /***
     * This method is for making call on entity bulk API in batches.
     * Currently, the threshold is controlled on 50 columns each time.
     *
     * @param col columns to tag
	 * @param tag Atlas Classification
     * @return
     * @author Tao
     * @date
     */
    public void setTagByColumnMultipleGuidbyBatches(Vector<HiveColumn> col, String tag) throws URISyntaxException {
        Iterator<HiveColumn> itr = col.iterator();
        StringBuilder sb = new StringBuilder();
        URIBuilder uri = new URIBuilder(atlasService + "/api/atlas/v2/entity/bulk/classification");

        // control the entity amount each invocation of entity bulk api as 50
        int count = 0;
        while (itr.hasNext()) {
            count++;
            sb.append('"');
            sb.append(itr.next().getGuid());
            sb.append('"');

            if (count % 50 == 0) {
                LOGGER.info("Tagging entities for  " + tag);
                // add in propagate and removePropagationsOnEntityDelete entries
                makeRestfulCall(uri, POST, "{\"classification\":{\"typeName\":\"" + tag + "\",\"attributes\":{}, " +
                        "\"propagate\":true, \"removePropagationsOnEntityDelete\":true}," +
                        "\"entityGuids\":[" + sb.toString() + "]}");
                sb.setLength(0);
            } else if (col.capacity() - count < 50 && !itr.hasNext()) {
                LOGGER.info("Tagging entities for  " + tag);
                // add in propagate and removePropagationsOnEntityDelete entries
                makeRestfulCall(uri, POST, "{\"classification\":{\"typeName\":\"" + tag + "\",\"attributes\":{}, " +
                        "\"propagate\":true, \"removePropagationsOnEntityDelete\":true}," +
                        "\"entityGuids\":[" + sb.toString() + "]}");
            }

            if (itr.hasNext()) {
                if (sb.length() > 0){
                    sb.append(",");
                }
            }
        }

    }

    public void delTagByColumnGuid(String columnGuid, String tag) throws URISyntaxException {
        URIBuilder uri = new URIBuilder(atlasService + "/api/atlas/v2/entity/guid/" + columnGuid + "/classification/" + tag);
        makeRestfulCall(uri, DELETE, "{}");
    }

    public String getAllTag() throws URISyntaxException {
        URIBuilder uri = new URIBuilder(atlasService + "/api/atlas/v2/types/typedefs");
//        URIBuilder uri = new URIBuilder("https://azalvedlmgtu01.d01saedl.manulife.com:21443/api/atlas/v2/types/typedefs");
        uri.addParameter("type", "TRAIT");
        uri.addParameter("notsupertype", "TaxonomyTerm");
        System.out.println("getAllTag" + uri);
        return makeRestfulCall(uri, GET);
    }

    public String getHiveColumnByTag(String limit, String tag, String offset) throws URISyntaxException {
        URIBuilder uri = new URIBuilder(atlasService + "/api/atlas/v2/search/basic");
        uri.addParameter("limit", limit);
        uri.addParameter("classification", tag);
        uri.addParameter("offset", offset);
        return makeRestfulCall(uri, GET);
    }

    public String getTags4ColumnSchema(String schema, int limit, int offset) throws URISyntaxException {
        URIBuilder uri = new URIBuilder(atlasService + "/api/atlas/v2/search/basic");
        // below commented one will only after HDP 2.6.5 version till then use basic search
//		String query = "hive_column where qualifiedName like '"+schema+"*' and __state='ACTIVE'";

        uri.addParameter("excludeDeletedEntities", "true");
        uri.addParameter("typeName", "hive_column");
        uri.addParameter("query", schema + "*");
        uri.addParameter("limit", limit + "");
        uri.addParameter("offset", offset + "");
        return makeRestfulCall(uri, GET);
    }
}
