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

import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportEndpointTypes;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangResource;

import java.util.List;

import static org.ballerinalang.net.http.HttpConstants.ANN_NAME_HTTP_SERVICE_CONFIG;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;

/**
 * Compiler plugin for validating HTTP service.
 *
 * @since 0.965.0
 */
@SupportEndpointTypes(
        value = {@SupportEndpointTypes.EndpointType(packageName = "ballerina.http", name = "ServiceEndpoint"),
                @SupportEndpointTypes.EndpointType(packageName = "ballerina.http", name = "WebSocketEndpoint")}
)
public class HTTPServiceCompilerPlugin extends AbstractCompilerPlugin {

    @Override
    public void init(DiagnosticLog diagnosticLog) {
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        for (AnnotationAttachmentNode annotation : annotations) {
            if (!PROTOCOL_PACKAGE_HTTP.equals(
                    ((BLangAnnotationAttachment) annotation).annotationSymbol.pkgID.name.value)) {
                continue;
            }
            if (annotation.getAnnotationName().getValue().equals(ANN_NAME_HTTP_SERVICE_CONFIG) || annotation
                    .getAnnotationName().getValue().equals(WebSocketConstants.WEBSOCKET_ANNOTATION_CONFIGURATION)) {
                handleServiceConfigAnnotation(serviceNode, (BLangAnnotationAttachment) annotation);
            }
        }
        if (HttpConstants.HTTP_SERVICE_TYPE.equals(serviceNode.getServiceTypeStruct().getTypeName().getValue())) {
            List<BLangResource> resources = (List<BLangResource>) serviceNode.getResources();
            resources.forEach(resource -> ResourceSignatureValidator.validate(resource.getParameters()));
        }
    }

    @Override
    public void process(EndpointNode endpointNode, List<AnnotationAttachmentNode> annotations) {
        // TODO: process endpoint configuration.
    }

    private void handleServiceConfigAnnotation(ServiceNode serviceNode, BLangAnnotationAttachment annotation) {
    }
}
