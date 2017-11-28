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
import org.ballerinalang.composer.service.workspace.composerapi.ComposerApi;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.json.JsonRpcMethod;
import org.eclipse.lsp4j.jsonrpc.messages.RequestMessage;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Handler to handle the request received from the web socket of the composer API.
 */
public class RequestHandler {
    private Gson gson = new Gson();

    public String routeRequestAndNotify(Endpoint endpoint, String text) {
        RequestMessage jsonrpcRequest = null;
        // Check if the text sent by the client is a valid JSON
        if (isJSONValid(text)) {
            jsonrpcRequest = gson.fromJson(text, RequestMessage.class);
            if (jsonrpcRequest.getId() != null) { // Its a request
                return handlerRequest(endpoint, text);
            } else { // Its a notification
                handlerNotification(endpoint, text);
                return null;
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
     * @param text              Request as a string
     * @return
     */
    public String handlerRequest(Endpoint serviceAsEndpoint, String text) {
        RequestMessage jsonrpcRequest = null;
        ResponseMessage jsonrpcResponse = null;
        ResponseError responseError = null;
        String responseStr = null;
        jsonrpcRequest = gson.fromJson(text, RequestMessage.class);

        Map<String, JsonRpcMethod> methods = ServiceEndpoints.getSupportedMethods(ComposerApi.class);
        JsonRpcMethod delegateMethod = methods.get(jsonrpcRequest.getMethod());

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
     * @param text              Request as a string
     * @return
     */
    public void handlerNotification(Endpoint serviceAsEndpoint, String text) {
        RequestMessage jsonrpcRequest = null;
        jsonrpcRequest = gson.fromJson(text, RequestMessage.class);

        Map<String, JsonRpcMethod> methods = ServiceEndpoints.getSupportedMethods(ComposerApi.class);
        JsonRpcMethod delegateMethod = methods.get(jsonrpcRequest.getMethod());

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
        ResponseError responseError = null;
        // Check if response object is null or not
        if (completableFutureResp != null) {
            try {
                jsonrpcResponse.setResult(completableFutureResp.get());
                jsonrpcResponse.setJsonrpc(jsonrpcRequest.getJsonrpc());
                jsonrpcResponse.setId(jsonrpcRequest.getId());
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
}
