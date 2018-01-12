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
package org.ballerinalang.composer.service.workspace.composerapi.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.composer.service.workspace.Constants;
import org.ballerinalang.composer.service.workspace.common.Utils;
import org.ballerinalang.composer.service.workspace.composerapi.ComposerApi;
import org.ballerinalang.composer.service.workspace.langconstruct.ModelPackage;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.json.JsonRpcMethod;
import org.eclipse.lsp4j.jsonrpc.messages.RequestMessage;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Handler to handle the request received from the web socket of the composer API.
 */
public class RequestHandler {

    private static Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    /**
     * Caching the built in packages.
     */
    private Map<String, ModelPackage> builtInNativePackages;
    private Set<Map.Entry<String, ModelPackage>> packages;

    private Gson gson = new Gson();

    public String routeRequestAndNotify(Endpoint endpoint, String text) {
        RequestMessage jsonrpcRequest = null;
        // Check if the text sent by the client is a valid JSON
        if (isJSONValid(text)) {
            try {
                jsonrpcRequest = gson.fromJson(text, RequestMessage.class);
                if (jsonrpcRequest.getMethod().equals("PING")) {
                    return sendPong();
                } else if (jsonrpcRequest.getMethod().equals(Constants.BUILT_IN_PACKAGES)) {
                    return getBuiltInPackages(jsonrpcRequest);
                } else if (jsonrpcRequest.getId() != null) { // Its a request
                    return handlerRequest(endpoint, jsonrpcRequest);
                } else { // Its a notification
                    handlerNotification(endpoint, jsonrpcRequest);
                    return null;
                }
            } catch (Exception e) {
                final String error = "Error while handling request. " + e.getMessage();
                logger.error(error, e);
                ResponseMessage jsonrpcResponse = new ResponseMessage();
                jsonrpcResponse.setId(null);
                ResponseError responseError = handleError(-32701, error);
                jsonrpcResponse.setError(responseError);
                return gson.toJson(jsonrpcResponse);
            }
        } else {
            ResponseMessage jsonrpcResponse = new ResponseMessage();
            jsonrpcResponse.setId(null);
            ResponseError responseError = handleError(-32700, "Parse error : Invalid JSON was received " +
                    "by the server");
            jsonrpcResponse.setError(responseError);
            return gson.toJson(jsonrpcResponse);
        }
    }

    /**
     * Handles the request sent to the endpoint.
     *
     * @param serviceAsEndpoint Endpoint service
     * @param jsonrpcRequest    Request message
     * @return {@link String}   Response as a String
     */
    public String handlerRequest(Endpoint serviceAsEndpoint, RequestMessage jsonrpcRequest) {
        ResponseMessage jsonrpcResponse = null;
        ResponseError responseError = null;
        String responseStr = null;
        JsonRpcMethod delegateMethod = getDelegateMethod(jsonrpcRequest.getMethod());
        if (delegateMethod != null) {
            // Cast parameters to the type requested by the delegate method
            Class paramCls = (Class) delegateMethod.getParameterTypes()[0];
            CompletableFuture completableFutureResp = serviceAsEndpoint.request(jsonrpcRequest.getMethod(),
                    new Gson().fromJson(new Gson().toJson((jsonrpcRequest.getParams())), paramCls));
            jsonrpcResponse = handleResult(jsonrpcRequest, completableFutureResp);
        } else {
            jsonrpcResponse = new ResponseMessage();
            jsonrpcResponse.setId(jsonrpcRequest.getId());
            responseError = handleError(-32601, "Method not found");
            jsonrpcResponse.setError(responseError);
        }

        responseStr = gson.toJson(jsonrpcResponse);
        return responseStr;
    }

    /**
     * Handles the notification sent to the endpoint.
     *
     * @param serviceAsEndpoint Endpoint service
     * @param jsonrpcRequest    Request message
     */
    public void handlerNotification(Endpoint serviceAsEndpoint, RequestMessage jsonrpcRequest) {
        JsonRpcMethod delegateMethod = getDelegateMethod(jsonrpcRequest.getMethod());
        if (delegateMethod != null) {
            // Cast parameters to the type requested by the delegate method
            Class paramCls = (Class) delegateMethod.getParameterTypes()[0];
            serviceAsEndpoint.notify(jsonrpcRequest.getMethod(), new Gson()
                    .fromJson(new Gson().toJson((jsonrpcRequest.getParams())), paramCls));
        }
    }

    /**
     * Check if the json string is in the correct format.
     *
     * @param jsonInString request string sent to the endpoint
     * @return valid or not
     */
    public boolean isJSONValid(String jsonInString) {
        try {
            gson.fromJson(jsonInString, Object.class);
            return true;
        } catch (com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }

    /**
     * Handle the result/response from the endpoint.
     *
     * @param jsonrpcRequest        JSON RPC Request object
     * @param completableFutureResp Result of the request sent
     * @return JSON RPC Response object
     */
    public ResponseMessage handleResult(RequestMessage jsonrpcRequest, CompletableFuture completableFutureResp) {
        ResponseMessage jsonrpcResponse = new ResponseMessage();
        jsonrpcResponse.setId(jsonrpcRequest.getId());
        ResponseError responseError = null;
        // Check if response object is null or not
        if (completableFutureResp != null) {
            try {
                jsonrpcResponse.setResult(completableFutureResp.get());
                jsonrpcResponse.setJsonrpc(jsonrpcRequest.getJsonrpc());
            } catch (InterruptedException e) {
                responseError = handleError(-32002, "Attempted to retrieve the result of a task/s " +
                        "that was aborted by throwing an exception");
                jsonrpcResponse.setError(responseError);
            } catch (ExecutionException e) {
                responseError = handleError(-32001, "Current thread was interrupted");
                jsonrpcResponse.setError(responseError);
            }
        } else {
            responseError = handleError(-32003, "Response received from the endpoint is null");
            jsonrpcResponse.setError(responseError);
        }
        return jsonrpcResponse;
    }

    /**
     * Handles the JSON RPC Error object.
     *
     * @param code    error code
     * @param message error message
     * @return JSON RPC Error object to be attached to the Response object
     */
    public ResponseError handleError(int code, String message) {
        ResponseError responseError = new ResponseError();
        responseError.setCode(code);
        responseError.setMessage(message);
        return responseError;
    }

    /**
     * Find the delegate method in the endpoint registered.
     *
     * @param methodName delegate method name
     * @return delegate method object
     */
    public JsonRpcMethod getDelegateMethod(String methodName) {
        Map<String, JsonRpcMethod> methods = ServiceEndpoints.getSupportedMethods(ComposerApi.class);
        return methods.get(methodName);
    }

    /**
     * send Pong for the ping send by client.
     *
     * @return string json
     */
    private String sendPong() {
        //TODO: Move this to MSF4J ping pong functionality when websocket moved to MSF4J.
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setId("PONG");
        return gson.toJson(responseMessage);
    }

    /**
     * Get all the built-in packages.
     *
     * @param message Request Message
     */
    private String getBuiltInPackages(RequestMessage message) {
        //TODO: Move this to be a separate service.
        JsonObject response = new JsonObject();
        // Load all the packages associated the runtime
        if (builtInNativePackages == null) {
            builtInNativePackages = Utils.getAllPackages();
        }
        this.setPackages(builtInNativePackages.entrySet());

        // add package info into response
        Gson gson = new Gson();
        String json = gson.toJson(builtInNativePackages.values());
        JsonParser parser = new JsonParser();
        JsonArray packagesArray = parser.parse(json).getAsJsonArray();
        response.add("packages", packagesArray);

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setId(message.getId());
        responseMessage.setResult(response);
        return gson.toJson(responseMessage);
    }

    /**
     * Set packages.
     *
     * @param packages - packages set
     */
    private void setPackages(Set<Map.Entry<String, ModelPackage>> packages) {
        this.packages = packages;
    }
}
