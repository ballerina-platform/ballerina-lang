/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.http.compiler;

import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;

import java.util.List;

/**
 * Service validator for WebSocket.
 */
public class WebSocketResourceValidator {

    public static void validate(String serviceName, BLangResource resource, DiagnosticLog dlog, boolean isClient) {
        switch (resource.getName().getValue()) {
            case WebSocketConstants.RESOURCE_NAME_ON_OPEN:
            case WebSocketConstants.RESOURCE_NAME_ON_IDLE_TIMEOUT:
                validateOnOpenResource(serviceName, resource, dlog, isClient);
                break;
            case WebSocketConstants.RESOURCE_NAME_ON_TEXT:
                validateOnTextResource(serviceName, resource, dlog, isClient);
                break;
            case WebSocketConstants.RESOURCE_NAME_ON_BINARY:
                validateOnBinaryResource(serviceName, resource, dlog, isClient);
                break;
            case WebSocketConstants.RESOURCE_NAME_ON_PING:
            case WebSocketConstants.RESOURCE_NAME_ON_PONG:
                validateOnPingPongResource(serviceName, resource, dlog, isClient);
                break;
            case WebSocketConstants.RESOURCE_NAME_ON_CLOSE:
                validateCloseResource(serviceName, resource, dlog, isClient);
                break;
            default:
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos,
                                   "Invalid resource name " + resource.getName().getValue() + " in service " +
                                           serviceName);
        }

    }

    private static void validateOnOpenResource(String serviceName, BLangResource resource, DiagnosticLog dlog,
                                               boolean isClient) {
        if (!isClient || !resource.getName().getValue().equals(WebSocketConstants.RESOURCE_NAME_ON_OPEN)) {
            List<BLangVariable> paramDetails = resource.getParameters();
            validateParamDetailsSize(paramDetails, 1, serviceName, resource, dlog);
            validateEndpointParameter(serviceName, resource, dlog, paramDetails, isClient);
        } else {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos,
                               "onOpen resource is not supported for " + WebSocketConstants.WEBSOCKET_CLIENT_SERVICE +
                                       " " + serviceName);
        }
    }

    private static void validateOnTextResource(String serviceName, BLangResource resource, DiagnosticLog dlog,
                                               boolean isClient) {
        List<BLangVariable> paramDetails = resource.getParameters();
        validateParamDetailsSize(paramDetails, 2, 3, serviceName, resource, dlog);
        validateEndpointParameter(serviceName, resource, dlog, paramDetails, isClient);
        if (paramDetails.size() < 2 || !"string".equals(paramDetails.get(1).type.toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos,
                               "Invalid resource signature for " + resource.getName().getValue() +
                                       " resource in service " +
                                       serviceName + ": The second parameter should be a string");
        }
        if (paramDetails.size() == 3 && !"boolean".equals(paramDetails.get(2).type.toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos,
                               "Invalid resource signature for " + resource.getName().getValue() +
                                       " resource in service " +
                                       serviceName + ": The third parameter should be a boolean");
        }
    }

    private static void validateOnBinaryResource(String serviceName, BLangResource resource, DiagnosticLog dlog,
                                                 boolean isClient) {
        List<BLangVariable> paramDetails = resource.getParameters();
        validateParamDetailsSize(paramDetails, 2, 3, serviceName, resource, dlog);
        validateEndpointParameter(serviceName, resource, dlog, paramDetails, isClient);
        if (paramDetails.size() < 2 || !"blob".equals(paramDetails.get(1).type.toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos,
                               "Invalid resource signature for " + resource.getName().getValue() +
                                       " resource in service " +
                                       serviceName + ": The second parameter should be a blob");
        }
        if (paramDetails.size() == 3 && !"boolean".equals(paramDetails.get(2).type.toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos,
                               "Invalid resource signature for " + resource.getName().getValue() +
                                       " resource in service " +
                                       serviceName + ": The third parameter should be a boolean");
        }
    }

    private static void validateOnPingPongResource(String serviceName, BLangResource resource, DiagnosticLog dlog,
                                                   boolean isClient) {
        List<BLangVariable> paramDetails = resource.getParameters();
        validateParamDetailsSize(paramDetails, 2, serviceName, resource, dlog);
        validateEndpointParameter(serviceName, resource, dlog, paramDetails, isClient);
        if (paramDetails.size() < 2 || !"blob".equals(paramDetails.get(1).type.toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos,
                               "Invalid resource signature for " + resource.getName().getValue() +
                                       " resource in service " +
                                       serviceName + ": The second parameter should be a blob");
        }
    }

    private static void validateCloseResource(String serviceName, BLangResource resource, DiagnosticLog dlog,
                                              boolean isClient) {
        List<BLangVariable> paramDetails = resource.getParameters();
        validateParamDetailsSize(paramDetails, 3, serviceName, resource, dlog);
        validateEndpointParameter(serviceName, resource, dlog, paramDetails, isClient);
        if (paramDetails.size() < 2 || !"int".equals(paramDetails.get(1).type.toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos,
                               "Invalid resource signature for " + resource.getName().getValue() +
                                       " resource in service " +
                                       serviceName + ": The second parameter should be an int");
        }
        if (paramDetails.size() < 3 || !"string".equals(paramDetails.get(2).type.toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos,
                               "Invalid resource signature for " + resource.getName().getValue() +
                                       " resource in service " +
                                       serviceName + ": The third parameter should be a string");
        }
    }

    private static void validateParamDetailsSize(List<BLangVariable> paramDetails, int expectedSize, String serviceName,
                                                 BLangResource resource, DiagnosticLog dlog) {
        if (paramDetails == null || paramDetails.size() != expectedSize) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos,
                               "Invalid resource signature for " + resource.getName().getValue() +
                                       " resource in service " +
                                       serviceName + ": Expected parameter count = " + expectedSize);
        }
    }

    private static void validateParamDetailsSize(List<BLangVariable> paramDetails, int min, int max, String serviceName,
                                                 BLangResource resource, DiagnosticLog dlog) {
        if (paramDetails == null || paramDetails.size() < min || paramDetails.size() > max) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos,
                               "Invalid resource signature for " + resource.getName().getValue() +
                                       " resource in service " +
                                       serviceName + ": Unexpected parameter count");
        }
    }

    private static void validateEndpointParameter(String serviceName, BLangResource resource, DiagnosticLog dlog,
                                                  List<BLangVariable> paramDetails, boolean isClient) {
        if (paramDetails == null || paramDetails.size() == 0 ||
                (!isClient && !WebSocketConstants.WEBSOCKET_ENDPOINT_NAME.equals(
                        paramDetails.get(0).type.toString())) ||
                (isClient && !WebSocketConstants.WEBSOCKET_CLIENT_ENDPOINT_NAME.equals(
                        paramDetails.get(0).type.toString()))) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos,
                               "Invalid resource signature for " + resource.getName().getValue() +
                                       " resource in service " +
                                       serviceName + ": The first parameter should be an endpoint");
        }
    }
}
