/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.composer.service.workspace.tryit;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpCoreContext;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Try it client for http protocol
 */
public class HttpTryItClient extends TryItClient {
    private Gson gson;
    private boolean calculateTime = true;
    
    /**
     * Initializes the client
     * @param serviceUrl The service url. Example: hostname:port.
     */
    public HttpTryItClient(String serviceUrl, String clientArgs) {
        super(serviceUrl, clientArgs);
        gson = new Gson();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String execute() throws TryItException {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpCoreContext localContext = new HttpCoreContext();
            // Create url for the request.
            String requestUrl = this.buildUrl();
            String httpMethod = this.clientArgs.get(TryItConstants.HTTP_METHOD).getAsString();
            // TODO : Check if can use HttpEntityEnclosingRequestBase for get, delete as well.
            switch (httpMethod.toLowerCase(Locale.getDefault())) {
                case "get":
                case "delete":
                case "options":
                case "head":
                case "trace":
                    HttpRequestBase httpRequest = new TryItHttpRequestBase(httpMethod.toUpperCase(Locale.getDefault()));
                    
                    // Setting the url for the request.
                    httpRequest.setURI(URI.create(requestUrl));
                    
                    // Setting the request headers.
                    JsonArray getHeaders = this.clientArgs.getAsJsonArray(TryItConstants.REQUEST_HEADERS);
                    for (JsonElement getHeader : getHeaders) {
                        JsonObject header = getHeader.getAsJsonObject();
                        httpRequest.setHeader(header.get("key").getAsString(), header.get("value").getAsString());
                    }
                    
                    // Setting the content type.
                    httpRequest.setHeader(HttpHeaders.CONTENT_TYPE,
                                                        this.clientArgs.get(TryItConstants.CONTENT_TYPE).getAsString());
                    
                    // To track how long the request took.
                    long start = System.currentTimeMillis();
                    
                    // Executing the request.
                    HttpResponse response = client.execute(httpRequest, localContext);
                    long elapsed = System.currentTimeMillis() - start;
                    
                    return jsonStringifyResponse(response, localContext.getRequest().getAllHeaders(), requestUrl,
                                                                                                            elapsed);
                case "post":
                case "put":
                case "patch":
                    HttpEntityEnclosingRequestBase httpEntityRequest =
                                new TryItHttpEntityEnclosingRequestBase(httpMethod.toUpperCase(Locale.getDefault()));
                
                    // Setting the url for the request.
                    httpEntityRequest.setURI(URI.create(requestUrl));
                    
                    // Setting the request headers.
                    JsonArray getEntityHeaders = this.clientArgs.getAsJsonArray(TryItConstants.REQUEST_HEADERS);
                    for (JsonElement getHeader : getEntityHeaders) {
                        JsonObject header = getHeader.getAsJsonObject();
                        httpEntityRequest.setHeader(header.get("key").getAsString(), header.get("value").getAsString());
                    }
                    
                    // Setting the body of the request.
                    StringEntity postEntity =
                                    new StringEntity(this.clientArgs.get(TryItConstants.REQUEST_BODY).getAsString());
                    httpEntityRequest.setEntity(postEntity);
                    
                    // Setting the content type.
                    httpEntityRequest.setHeader(HttpHeaders.CONTENT_TYPE,
                                                        this.clientArgs.get(TryItConstants.CONTENT_TYPE).getAsString());
    
                    // To track how long the request took.
                    long entityRequestStart = System.currentTimeMillis();
                    
                    // Executing the request.
                    HttpResponse entityResponse = client.execute(httpEntityRequest, localContext);
                    long entityRequestDuration = System.currentTimeMillis() - entityRequestStart;
                    return jsonStringifyResponse(entityResponse, localContext.getRequest().getAllHeaders(), requestUrl,
                                                                                                entityRequestDuration);
                default:
                    return null;
            }
        } catch (java.io.IOException e) {
            throw new TryItException(e.getMessage());
        }
    }
    
    /**
     * Converts a response to json string.
     * @param response The http response.
     * @return The response as a json string.
     * @throws IOException Error while getting the body of the response.
     */
    private String jsonStringifyResponse(HttpResponse response, Header[] headers, String requestUrl, long timeConsumed)
                                                                                                    throws IOException {
        JsonObject responseContent = new JsonObject();
        
        // Setting the response code.
        responseContent.addProperty(TryItConstants.RESPONSE_CODE,
                                                            Integer.toString(response.getStatusLine().getStatusCode()));
        
        // Setting the body of the response.
        String responseBody = "";
        if (null != response.getEntity()) {
            responseBody = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
            IOUtils.closeQuietly(response.getEntity().getContent());
        }
        responseContent.addProperty(TryItConstants.RESPONSE_BODY, responseBody);
        
        // Setting the headers of the response.
        if (response.getAllHeaders().length > 0) {
            JsonObject responseHeaders = new JsonObject();
            for (Header header : response.getAllHeaders()) {
                responseHeaders.addProperty(header.getName(), header.getValue());
            }
            responseContent.add(TryItConstants.RESPONSE_HEADERS, responseHeaders);
        }
    
        // Setting the headers of the request.
        JsonObject requestHeadersMap = new JsonObject();
        for (Header requestHeader : headers) {
            requestHeadersMap.addProperty(requestHeader.getName(), requestHeader.getValue());
        }
        requestHeadersMap.addProperty(HttpHeaders.USER_AGENT, "Ballerina Composer");
        responseContent.add(TryItConstants.RETURNED_REQUEST_HEADERS, requestHeadersMap);
    
        // Setting the time consumed.
        if (this.calculateTime) {
            responseContent.addProperty(TryItConstants.TIME_CONSUMED, Long.toString(timeConsumed));
        }
        
        // Setting the request url.
        responseContent.addProperty(TryItConstants.REQUEST_URL, requestUrl);
    
        // Json stringifying.
        return gson.toJson(responseContent);
    }
    
    /**
     * Builds the url for the service.
     * @return The urls of the service.
     */
    private String buildUrl() {
        if (this.clientArgs.get(TryItConstants.APPEND_URL).getAsString().length() > 0 &&
            this.clientArgs.get(TryItConstants.APPEND_URL).getAsString().charAt(0) == '/') {
            return "http://" + this.serviceUrl + this.clientArgs.get(TryItConstants.APPEND_URL).getAsString();
        } else {
            return "http://" + this.serviceUrl + "/" + this.clientArgs.get(TryItConstants.APPEND_URL).getAsString();
        }
    }
    
    /**
     * Disables sending the duration of the http request in the response.
     */
    public void disableTimeDurationCalculation() {
        this.calculateTime = false;
    }
    
    /**
     * Static class for http requests without entity.
     */
    private static class TryItHttpRequestBase extends HttpRequestBase {
    
        private final String httpMethod;
    
        TryItHttpRequestBase(String httpMethod) {
            this.httpMethod = httpMethod;
        }
        
        @Override
        public String getMethod() {
            return httpMethod;
        }
    }
    
    /**
     * Static class for http requests with entity.
     */
    private static class TryItHttpEntityEnclosingRequestBase extends HttpEntityEnclosingRequestBase {
        
        private final String httpMethod;
    
        TryItHttpEntityEnclosingRequestBase(String httpMethod) {
            this.httpMethod = httpMethod;
        }
        
        @Override
        public String getMethod() {
            return httpMethod;
        }
    }
}
