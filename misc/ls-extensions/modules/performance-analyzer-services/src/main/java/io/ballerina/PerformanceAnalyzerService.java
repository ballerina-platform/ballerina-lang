/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.ballerina.component.AnalyzeType;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 * The extended service for the performance analyzer.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("performanceAnalyzer")
public class PerformanceAnalyzerService implements ExtendedLanguageServerService {

    static final String ERROR = "error";
    static final String SUCCESS = "Success";
    static final String CONNECTION_ERROR = "CONNECTION_ERROR";
    static final String AUTHENTICATION_ERROR = "AUTHENTICATION_ERROR";
    static final String SOME_ERROR = "SOME_ERROR_OCCURRED";
    static final String ENDPOINT_RESOLVE_ERROR = "ENDPOINT_RESOLVE_ERROR";
    private static final HashMap<JsonObject, JsonObject> realTimeCachedResponses = new HashMap<>();
    private static final HashMap<JsonObject, JsonObject> advancedCachedResponses = new HashMap<>();

    private WorkspaceManager workspaceManager;

    @Override
    public Class<?> getRemoteInterface() {

        return getClass();
    }

    @Override
    public void init(LanguageServer langServer, WorkspaceManager workspaceManager) {

        this.workspaceManager = workspaceManager;
    }

    @JsonNotification
    public CompletableFuture<JsonObject> getEndpoints(PerformanceAnalyzerGraphRequest request) {

        return CompletableFuture.supplyAsync(() -> {
            String fileUri = request.getDocumentIdentifier().getUri();
            return EndpointsFinder.getEndpoints(fileUri, this.workspaceManager, request.getRange());
        });
    }

    /**
     * Get advanced graph data.
     *
     * @param request data
     * @return string of json
     */
    @JsonNotification
    public CompletableFuture<JsonObject> getGraphData(PerformanceAnalyzerGraphRequest request) {

        return CompletableFuture.supplyAsync(() -> {
            String fileUri = request.getDocumentIdentifier().getUri();
            JsonObject data = EndpointsFinder.getEndpoints(fileUri, this.workspaceManager, request.getRange());
            if (data.entrySet().isEmpty()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("type", ERROR);
                obj.addProperty("message", ENDPOINT_RESOLVE_ERROR);
                return obj;
            }

            JsonObject graphData;
            if (advancedCachedResponses.get(data) != null) {
                graphData = advancedCachedResponses.get(data);
            } else {
                graphData = getDataFromChoreo(request.getChoreoAPI(), data, AnalyzeType.ADVANCED,
                        request.getChoreoToken(), request.getChoreoCookie());

                if (graphData == null) {
                    return null;
                }

                if (graphData.get("type") == null) {
                    graphData.addProperty("type", SUCCESS);
                    graphData.addProperty("message", SUCCESS);
                    advancedCachedResponses.put(data, graphData);
                }
            }

            return graphData;
        });
    }

    /**
     * Get realtime graph data.
     *
     * @param request data
     * @return String of json
     */
    @JsonNotification
    public CompletableFuture<JsonObject> getRealtimeData(PerformanceAnalyzerGraphRequest request) {

        return CompletableFuture.supplyAsync(() -> {
            String fileUri = request.getDocumentIdentifier().getUri();
            JsonObject data = EndpointsFinder.getEndpoints(fileUri, this.workspaceManager, request.getRange());

            if (data.entrySet().isEmpty()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("type", ERROR);
                obj.addProperty("message", ENDPOINT_RESOLVE_ERROR);
                return obj;
            }

            JsonObject realTimeData;
            if (realTimeCachedResponses.get(data) != null) {
                realTimeData = realTimeCachedResponses.get(data);
            } else {
                realTimeData = getDataFromChoreo(request.getChoreoAPI(), data, AnalyzeType.REALTIME,
                        request.getChoreoToken(), request.getChoreoCookie());

                if (realTimeData.get("type") == null) {
                    realTimeData.addProperty("type", SUCCESS);
                    realTimeData.addProperty("message", SUCCESS);
                    realTimeCachedResponses.put(data, realTimeData);
                }
            }

            return realTimeData;
        });
    }

    /**
     * Get graph data from Choreo.
     *
     * @param data        action invocations
     * @param analyzeType analyze type
     * @return graph data json
     */
    private JsonObject getDataFromChoreo(String api, JsonObject data, AnalyzeType analyzeType,
                                         String authToken, String authCookie) {

        Gson gson = new Gson();
        data.add("analyzeType", gson.toJsonTree(analyzeType.getAnalyzeType()));

        try {
            HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(api))
                    .headers("Content-Type", "application/json",
                            "Authorization", authToken,
                            "Cookie", authCookie)
                    .POST(HttpRequest.BodyPublishers.ofString(data.toString()))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
            data.remove("analyzeType");

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), JsonObject.class);
            } else if (response.statusCode() == 401) {
                JsonObject obj = new JsonObject();
                obj.addProperty("type", ERROR);
                obj.addProperty("message", AUTHENTICATION_ERROR);
                return obj;
            }
            JsonObject obj = new JsonObject();
            obj.addProperty("type", ERROR);
            obj.addProperty("message", SOME_ERROR);
            return obj;

        } catch (IOException e) {
            // No connection
            data.remove("analyzeType");
            JsonObject obj = new JsonObject();
            obj.addProperty("type", ERROR);
            obj.addProperty("message", CONNECTION_ERROR);
            return obj;
        } catch (InterruptedException | URISyntaxException e) {
            data.remove("analyzeType");
            JsonObject obj = new JsonObject();
            obj.addProperty("type", ERROR);
            obj.addProperty("message", e.getMessage());
            return obj;
        }
    }
}
