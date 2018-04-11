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
package org.ballerinalang.net.websub.compiler;

import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportEndpointTypes;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.types.UserDefinedTypeNode;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.compiler.ResourceSignatureValidator;
import org.ballerinalang.net.websub.WebSubSubscriberConstants;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangResource;

import java.util.List;

/**
 * Compiler plugin for validating WebSub service.
 *
 * @since 0.965.0
 */
@SupportEndpointTypes(
        value = {@SupportEndpointTypes.EndpointType(orgName = "ballerina", packageName = "websub", name = "Listener")}
)
public class WebSubServiceCompilerPlugin extends AbstractCompilerPlugin {

    private DiagnosticLog dlog = null;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        dlog = diagnosticLog;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        for (AnnotationAttachmentNode annotation : annotations) {
            if (!HttpConstants.PROTOCOL_PACKAGE_HTTP.equals(
                    ((BLangAnnotationAttachment) annotation).annotationSymbol.pkgID.name.value)) {
                continue;
            }
            if (annotation.getAnnotationName().getValue().equals(
                    WebSubSubscriberConstants.ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG)) {
                handleServiceConfigAnnotation(serviceNode, (BLangAnnotationAttachment) annotation);
            }
        }
        final UserDefinedTypeNode serviceType = serviceNode.getServiceTypeStruct();
        if (serviceType != null && HttpConstants.HTTP_SERVICE_TYPE.equals(serviceType.getTypeName().getValue())) {
            List<BLangResource> resources = (List<BLangResource>) serviceNode.getResources();
            resources.forEach(res -> ResourceSignatureValidator.validate(res.getParameters(), dlog, res.pos));
        }
    }

    @Override
    public void process(EndpointNode endpointNode, List<AnnotationAttachmentNode> annotations) {
        // TODO: process endpoint configuration.
    }

    private void handleServiceConfigAnnotation(ServiceNode serviceNode, BLangAnnotationAttachment annotation) {
    }
}
