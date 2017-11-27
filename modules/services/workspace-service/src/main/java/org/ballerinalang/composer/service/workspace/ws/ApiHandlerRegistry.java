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

package org.ballerinalang.composer.service.workspace.ws;

import com.google.gson.Gson;
import org.ballerinalang.composer.service.workspace.ws.exception.BallerinaWebSocketException;
import org.ballerinalang.composer.service.workspace.ws.model.JSONRPCRequest;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

public class ApiHandlerRegistry {
    private static ApiHandlerRegistry apiHandlerRegistry = null;
    Map<String, Class<? extends ComposerApiHandler>> handlers = new HashMap<>();
    private Gson gson = new Gson();

    protected ApiHandlerRegistry() {
        // Exists only to defeat instantiation.
    }

    public static ApiHandlerRegistry getInstance() {
        if(apiHandlerRegistry == null) {
            apiHandlerRegistry = new ApiHandlerRegistry();
        }
        return apiHandlerRegistry;
    }

    public void registerHandler(Class<? extends ComposerApiHandler> handlerClass) throws BallerinaWebSocketException {
        String method = null;
        // TODO : Map already exists
        try {
            method = (String)handlerClass.getMethod("getMethodName", null).invoke(handlerClass.newInstance());
            handlers.put(method, handlerClass);
        }  catch (Exception e) {
            throw new BallerinaWebSocketException(e);
        }
    }
    public Object callHandler(JSONRPCRequest rpcObj) throws BallerinaWebSocketException {
        // Get the handler class from the method passed
        Class cls = handlers.get(rpcObj.getMethod());
        Object resultObj = null;
        try {
            Class genericClass = (Class) ((ParameterizedType)cls.getGenericInterfaces()[0]).getActualTypeArguments()[0];
            resultObj = cls.getMethod("process", genericClass).invoke(cls.newInstance(), new Gson().fromJson(new Gson().toJson((rpcObj.getParams())), genericClass));
        } catch (Exception e) {
            throw new BallerinaWebSocketException(e);
        }
        return resultObj;
    }
}