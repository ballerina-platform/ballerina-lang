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

package org.ballerinalang.net.http.compiler.websocket;

import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.types.ArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service validator for WebSocket.
 */
public abstract class WebSocketResourceValidator {

    static final String INVALID_RESOURCE_SIGNATURE_FOR = "Invalid resource signature for ";
    static final String RESOURCE_IN_SERVICE = " resource in service ";
    protected DiagnosticLog dlog;
    private Map<String, Runnable> validationMap = new HashMap<>();
    protected BLangFunction resource;
    List<BLangSimpleVariable> paramDetails;

    WebSocketResourceValidator(DiagnosticLog dlog, BLangFunction resource) {
        this.dlog = dlog;
        this.resource = resource;
        this.paramDetails = resource.getParameters();
        validationMap.put(WebSocketConstants.RESOURCE_NAME_ON_OPEN, this::validateOnOpenResource);
        validationMap.put(WebSocketConstants.RESOURCE_NAME_ON_IDLE_TIMEOUT, this::validateOnIdleTimeoutResource);
        validationMap.put(WebSocketConstants.RESOURCE_NAME_ON_TEXT, this::validateOnTextResource);
        validationMap.put(WebSocketConstants.RESOURCE_NAME_ON_BINARY, this::validateOnBinaryResource);
        validationMap.put(WebSocketConstants.RESOURCE_NAME_ON_PING, this::validateOnPingPongResource);
        validationMap.put(WebSocketConstants.RESOURCE_NAME_ON_PONG, this::validateOnPingPongResource);
        validationMap.put(WebSocketConstants.RESOURCE_NAME_ON_CLOSE, this::validateOnCloseResource);
        validationMap.put(WebSocketConstants.RESOURCE_NAME_ON_ERROR, this::validateOnErrorResource);
    }

    public void validate() {
        String name = resource.getName().getValue();
        if (validationMap.containsKey(name)) {
            validateEndpointParameter();
            validationMap.get(name).run();
        } else {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos,
                               "Invalid resource name " + resource.getName().getValue() + " in service ");
        }
    }

    abstract void validateOnOpenResource();

    void validateOnIdleTimeoutResource() {
        validateParamDetailsSize(1);
    }

    private void validateOnTextResource() {
        validateParamDetailsSize();
        if (paramDetails.size() < 2) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE +
                    ": A second parameter needs to be specified");
        }
        BType secondParamType = paramDetails.get(1).type;
        int secondParamTypeTag = secondParamType.tag;
        if (secondParamTypeTag != TypeTags.STRING && secondParamTypeTag != TypeTags.JSON &&
                secondParamTypeTag != TypeTags.XML && secondParamTypeTag != TypeTags.RECORD &&
                (secondParamTypeTag != TypeTags.ARRAY || (secondParamType instanceof ArrayType &&
                        ((ArrayType) secondParamType).getElementType().tag != TypeTags.BYTE))) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE +
                    ": The second parameter should be a string, json, xml, byte[] or a record type");
        } else if (paramDetails.size() == 3) {
            if (!"string".equals(secondParamType.toString())) {
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                        + resource.getName().getValue() + RESOURCE_IN_SERVICE +
                        ": Final fragment is not valid if the second parameter is not a string");
            } else if (!"boolean".equals(paramDetails.get(2).type.toString())) {
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                        + resource.getName().getValue() + RESOURCE_IN_SERVICE +
                        ": The third parameter should be a boolean");
            }
        }
    }

    private void validateOnBinaryResource() {
        validateParamDetailsSize();
        if (paramDetails.size() < 2 || !"byte[]".equals(paramDetails.get(1).type.toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE +
                    ": The second parameter should be a byte[]");
        }
        if (paramDetails.size() == 3 && !"boolean".equals(paramDetails.get(2).type.toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE +
                    ": The third parameter should be a boolean");
        }
    }

    private void validateOnPingPongResource() {
        validateParamDetailsSize(2);
        if (paramDetails.size() < 2 || !"byte[]".equals(paramDetails.get(1).type.toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE +
                    ": The second parameter should be a byte[]");
        }
    }

    private void validateOnCloseResource() {
        validateParamDetailsSize(3);
        if (paramDetails.size() < 2 || TypeTags.INT != paramDetails.get(1).type.tag) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR +
                    resource.getName().getValue() + RESOURCE_IN_SERVICE +
                    ": The second parameter should be an int");
        }
        if (paramDetails.size() < 3 || TypeTags.STRING != paramDetails.get(2).type.tag) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE +
                    ": The third parameter should be a string");
        }
    }

    private void validateOnErrorResource() {
        validateParamDetailsSize(2);
        if (paramDetails.size() < 2 || !"error".equals(paramDetails.get(1).type.toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, String.format(
                    "Invalid resource signature for %s resource in service : The second parameter should be an error",
                    resource.getName().getValue()));
        }
    }

    private void validateParamDetailsSize(int expectedSize) {
        if (paramDetails == null || paramDetails.size() != expectedSize) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE +
                    ": Expected parameter count = " + expectedSize);
        }
    }

    private void validateParamDetailsSize() {
        if (paramDetails == null || paramDetails.size() < 2 || paramDetails.size() > 3) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE + ": Unexpected parameter count");
        }
    }

    abstract void validateEndpointParameter();
}
