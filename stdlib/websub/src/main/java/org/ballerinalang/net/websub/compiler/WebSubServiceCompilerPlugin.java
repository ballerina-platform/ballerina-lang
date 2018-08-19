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
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.types.UserDefinedTypeNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangResource;

import java.util.List;

import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.GENERIC_SUBSCRIBER_SERVICE_TYPE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_NOTIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;
import static org.ballerinalang.net.websub.WebSubSubscriberServiceValidator.validateDefaultResources;

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
        final UserDefinedTypeNode serviceType = serviceNode.getServiceTypeStruct();

        int webSubAnnotationConfigCount = 0;
        for (AnnotationAttachmentNode annotation : annotations) {
            if (ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG.equals(annotation.getAnnotationName().getValue())) {
                webSubAnnotationConfigCount++;
                // TODO: 8/19/18 intro annotation validation if required
            }
        }

        if (webSubAnnotationConfigCount > 1) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                               "cannot have more than one '" + ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG
                                       + "' annotations");
        }

        if (serviceType != null && GENERIC_SUBSCRIBER_SERVICE_TYPE.equals(serviceType.getTypeName().getValue())) {
            List<BLangResource> resources = (List<BLangResource>) serviceNode.getResources();
            if (resources.size() > 2) {
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                               "cannot have more than two resources with " + WEBSUB_PACKAGE + ":"
                                       + GENERIC_SUBSCRIBER_SERVICE_TYPE);
                return;
            }
            resources.forEach(res -> {
                validateDefaultResources(res, dlog);
            });

            if (resources.size() < 1
                    || (resources.size() == 1
                                && !(RESOURCE_NAME_ON_NOTIFICATION.equals(resources.get(0).getName().getValue())))) {
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                               "required resource '" + RESOURCE_NAME_ON_NOTIFICATION + "' not "
                                       + "specified with " +  WEBSUB_PACKAGE + ":" + GENERIC_SUBSCRIBER_SERVICE_TYPE);
            }
        }
        // get value from endpoint.
        // ((BLangSimpleVarRef) serviceNode.getBoundEndpoints().get(0)).varSymbol.getType().tsymbol.name.value
    }

}
