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
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;

/**
 * Service validator for WebSocket.
 */
public class WebSocketResourceValidator {

    private static final String INVALID_RESOURCE_SIGNATURE_FOR = "Invalid resource signature for ";
    private static final String RESOURCE_IN_SERVICE = " resource in service ";

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
                validateOnCloseResource(serviceName, resource, dlog, isClient);
                break;
            case WebSocketConstants.RESOURCE_NAME_ON_ERROR:
                validateOnErrorResource(serviceName, resource, dlog, isClient);
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
            List<BLangSimpleVariable> paramDetails = resource.getParameters();
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
        List<BLangSimpleVariable> paramDetails = resource.getParameters();
        validateParamDetailsSize(paramDetails, 2, 3, serviceName, resource, dlog);
        validateEndpointParameter(serviceName, resource, dlog, paramDetails, isClient);
        if (paramDetails.size() < 2) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE + serviceName +
                    ": A second parameter needs to be specified");
        }
        BType secondParamType = paramDetails.get(1).type;
        int secondParamTypeTag = secondParamType.tag;
        if (secondParamTypeTag != TypeTags.STRING && secondParamTypeTag != TypeTags.JSON &&
                secondParamTypeTag != TypeTags.XML && secondParamTypeTag != TypeTags.RECORD &&
                (secondParamTypeTag != TypeTags.ARRAY || (secondParamType instanceof BArrayType &&
                        ((BArrayType) secondParamType).getElementType().tag !=
                                org.ballerinalang.model.types.TypeTags.BYTE_TAG))) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE + serviceName +
                    ": The second parameter should be a string, json, xml, byte[] or a record type");
        } else if (paramDetails.size() == 3) {
            if (!"string".equals(secondParamType.toString())) {
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                        + resource.getName().getValue() + RESOURCE_IN_SERVICE + serviceName +
                        ": Final fragment is not valid if the second parameter is not a string");
            } else if (!"boolean".equals(paramDetails.get(2).type.toString())) {
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                        + resource.getName().getValue() + RESOURCE_IN_SERVICE + serviceName +
                        ": The third parameter should be a boolean");
            }
        }
    }

    private static void validateOnBinaryResource(String serviceName, BLangResource resource, DiagnosticLog dlog,
                                                 boolean isClient) {
        List<BLangSimpleVariable> paramDetails = resource.getParameters();
        validateParamDetailsSize(paramDetails, 2, 3, serviceName, resource, dlog);
        validateEndpointParameter(serviceName, resource, dlog, paramDetails, isClient);
        if (paramDetails.size() < 2 || !"byte[]".equals(paramDetails.get(1).type.toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE + serviceName +
                    ": The second parameter should be a byte[]");
        }
        if (paramDetails.size() == 3 && !"boolean".equals(paramDetails.get(2).type.toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE + serviceName +
                    ": The third parameter should be a boolean");
        }
    }

    private static void validateOnPingPongResource(String serviceName, BLangResource resource, DiagnosticLog dlog,
                                                   boolean isClient) {
        List<BLangSimpleVariable> paramDetails = resource.getParameters();
        validateParamDetailsSize(paramDetails, 2, serviceName, resource, dlog);
        validateEndpointParameter(serviceName, resource, dlog, paramDetails, isClient);
        if (paramDetails.size() < 2 || !"byte[]".equals(paramDetails.get(1).type.toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE + serviceName +
                    ": The second parameter should be a byte[]");
        }
    }

    private static void validateOnCloseResource(String serviceName, BLangResource resource, DiagnosticLog dlog,
                                                boolean isClient) {
        List<BLangSimpleVariable> paramDetails = resource.getParameters();
        validateParamDetailsSize(paramDetails, 3, serviceName, resource, dlog);
        validateEndpointParameter(serviceName, resource, dlog, paramDetails, isClient);
        if (paramDetails.size() < 2 || TypeTags.INT != paramDetails.get(1).type.tag) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR +
                    resource.getName().getValue() + RESOURCE_IN_SERVICE + serviceName +
                    ": The second parameter should be an int");
        }
        if (paramDetails.size() < 3 || TypeTags.STRING != paramDetails.get(2).type.tag) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE + serviceName +
                    ": The third parameter should be a string");
        }
    }

    private static void validateOnErrorResource(String serviceName, BLangResource resource, DiagnosticLog dlog,
                                                boolean isClient) {
        List<BLangSimpleVariable> paramDetails = resource.getParameters();
        validateParamDetailsSize(paramDetails, 2, serviceName, resource, dlog);
        validateEndpointParameter(serviceName, resource, dlog, paramDetails, isClient);
        if (paramDetails.size() < 2 || !"error".equals(paramDetails.get(1).type.toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, String.format(
                    "Invalid resource signature for %s resource in service %s: The second parameter should be an error",
                    resource.getName().getValue(), serviceName));
        }
    }

    private static void validateParamDetailsSize(List<BLangSimpleVariable> paramDetails, int expectedSize,
                                                 String serviceName, BLangResource resource, DiagnosticLog dlog) {
        if (paramDetails == null || paramDetails.size() != expectedSize) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE + serviceName +
                    ": Expected parameter count = " + expectedSize);
        }
    }

    private static void validateParamDetailsSize(List<BLangSimpleVariable> paramDetails, int min, int max,
                                                 String serviceName, BLangResource resource, DiagnosticLog dlog) {
        if (paramDetails == null || paramDetails.size() < min || paramDetails.size() > max) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE + serviceName +
                    ": Unexpected parameter count");
        }
    }

    private static void validateEndpointParameter(String serviceName, BLangResource resource, DiagnosticLog dlog,
                                                  List<BLangSimpleVariable> paramDetails, boolean isClient) {
        if (paramDetails == null || paramDetails.isEmpty() ||
                (!isClient && !WebSocketConstants.WEBSOCKET_ENDPOINT_NAME.equals(
                        paramDetails.get(0).type.toString())) ||
                (isClient && !WebSocketConstants.WEBSOCKET_CLIENT_ENDPOINT_NAME.equals(
                        paramDetails.get(0).type.toString()))) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE + serviceName +
                    ": The first parameter should be an endpoint");
        }
    }

    private WebSocketResourceValidator() {
    }
}
