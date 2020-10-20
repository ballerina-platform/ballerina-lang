/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerina.testobserve.listenerendpoint;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;

import static org.ballerina.testobserve.listenerendpoint.Constants.NETTY_CONTEXT_NATIVE_DATA_KEY;
import static org.ballerina.testobserve.listenerendpoint.Constants.WEB_SERVER_NATIVE_DATA_KEY;

/**
 * Mock listener endpoint related methods which will be invoked by Ballerina Java Inter-Op calls.
 */
public class Endpoint {

    public static void initEndpoint(Environment env, BObject listenerEndpoint, int port) {
        WebServer webServer = new WebServer(port, env.getRuntime());
        listenerEndpoint.addNativeData(WEB_SERVER_NATIVE_DATA_KEY, webServer);
        Utils.logInfo("Initialized Web Server with port " + port);
    }

    public static BError attachService(BObject listenerEndpoint, BObject serviceObject) {
        try {
            WebServer webServer = (WebServer) listenerEndpoint.getNativeData(WEB_SERVER_NATIVE_DATA_KEY);
            Service service = webServer.addService(serviceObject);
            Utils.logInfo("Attached Service %s", service.getServiceName());
            return null;
        } catch (Throwable t) {
            return Utils.createError(t);
        }
    }

    public static BError detachService(BObject listenerEndpoint, BObject serviceObject) {
        try {
            WebServer webServer = (WebServer) listenerEndpoint.getNativeData(WEB_SERVER_NATIVE_DATA_KEY);
            Service service = webServer.removeService(serviceObject);
            Utils.logInfo("Detached Service %s", service.getServiceName());
            return null;
        } catch (Throwable t) {
            return Utils.createError(t);
        }
    }

    public static void start(BObject listenerEndpoint) {
        WebServer webServer = (WebServer) listenerEndpoint.getNativeData(WEB_SERVER_NATIVE_DATA_KEY);
        new Thread(() -> {
            try {
                webServer.start();
            } catch (Throwable e) {
                Utils.logError("Error initializing agent server, error - " + e.getMessage());
            }
        }).start();
    }

    public static BError shutdownGracefully(BObject listenerEndpoint) {
        try {
            WebServer webServer = (WebServer) listenerEndpoint.getNativeData(WEB_SERVER_NATIVE_DATA_KEY);
            webServer.shutdownGracefully();
            Utils.logInfo("Shutting down gracefully");
            return null;
        } catch (Throwable e) {
            return Utils.createError(e);
        }
    }

    public static BError shutdownNow(BObject listenerEndpoint) {
        try {
            WebServer webServer = (WebServer) listenerEndpoint.getNativeData(WEB_SERVER_NATIVE_DATA_KEY);
            webServer.shutdownNow();
            Utils.logInfo("Shutting down");
            return null;
        } catch (Throwable e) {
            return Utils.createError(e);
        }
    }

    public static BError respond(BObject caller, BString message) {
        try {
            ChannelHandlerContext ctx = (ChannelHandlerContext) caller.getNativeData(NETTY_CONTEXT_NATIVE_DATA_KEY);
            WebServer.writeResponse(ctx, HttpResponseStatus.OK, message.getValue());
            return null;
        } catch (Throwable e) {
            return Utils.createError(e);
        }
    }
}
