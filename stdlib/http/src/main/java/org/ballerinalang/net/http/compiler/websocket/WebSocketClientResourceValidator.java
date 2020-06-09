/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.ballerinalang.compiler.tree.BLangFunction;

/**
 * Service validator for WebSocket.
 */
class WebSocketClientResourceValidator extends WebSocketResourceValidator {
    WebSocketClientResourceValidator(DiagnosticLog dlog, BLangFunction resource) {
        super(dlog, resource);
    }

    @Override
    void validateOnOpenResource() {
        dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos,
                           "onOpen resource is not supported for " + WebSocketConstants.WEBSOCKET_CLIENT_SERVICE);
    }

    void validateEndpointParameter() {
        if (paramDetails != null && !paramDetails.isEmpty() && !WebSocketConstants.FULL_WEBSOCKET_CLIENT_NAME.equals(
                paramDetails.get(0).type.toString())) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, resource.pos, INVALID_RESOURCE_SIGNATURE_FOR
                    + resource.getName().getValue() + RESOURCE_IN_SERVICE +
                    ": The first parameter should be a " + WebSocketConstants.FULL_WEBSOCKET_CLIENT_NAME);
        }
    }
}
