/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.net.http.compiler;

import org.ballerinalang.compiler.plugins.SupportedResourceParamTypes;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.util.AbstractTransportCompilerPlugin;

import java.util.List;

import static org.ballerinalang.net.http.WebSocketConstants.WEBSOCKET_ANNOTATION_CONFIGURATION;
import static org.ballerinalang.net.http.WebSocketConstants.WEBSOCKET_CALLER;
import static org.ballerinalang.net.http.WebSocketConstants.WEBSOCKET_SERVICE;

/**
 * Compiler plugin for validating WebSocket service.
 *
 * @since 0.965.0
 */
@SupportedResourceParamTypes(
        expectedListenerType = @SupportedResourceParamTypes.Type(
                packageName = WebSocketConstants.PACKAGE_HTTP,
                name = WebSocketConstants.WEBSOCKET_LISTENER
        ),
        paramTypes = {
                @SupportedResourceParamTypes.Type(
                        packageName = WebSocketConstants.PACKAGE_HTTP,
                        name = WEBSOCKET_CALLER
                )
        }
)
public class WebSocketServiceCompilerPlugin extends AbstractTransportCompilerPlugin {

    private DiagnosticLog dlog = null;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        dlog = diagnosticLog;
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        if (annotations.size() > 1) {
            int count = 0;
            for (AnnotationAttachmentNode annotation : annotations) {
                if (annotation.getAnnotationName().getValue().equals(WEBSOCKET_ANNOTATION_CONFIGURATION)) {
                    count++;
                }
            }
            if (count > 1) {
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                                   "There cannot be more than one " + WEBSOCKET_SERVICE + " annotations");
            }
        }
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        List<BLangFunction> resources = (List<BLangFunction>) serviceNode.getResources();
        resources.forEach(
                res -> WebSocketResourceValidator.validate(res, dlog, isResourceReturnsErrorOrNil(res), false));
    }
}


