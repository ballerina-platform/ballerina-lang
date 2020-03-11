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
import org.ballerinalang.compiler.plugins.SupportedResourceParamTypes;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangService;

import java.util.List;

import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_NAME_WEBSUB_SPECIFIC_SUBSCRIBER;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.BALLERINA;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.GENERIC_SUBSCRIBER_SERVICE_TYPE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_NOTIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_INTENT_VERIFICATION_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_NOTIFICATION_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_SERVICE_CALLER;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_SERVICE_LISTENER;
import static org.ballerinalang.net.websub.WebSubSubscriberServiceValidator.validateDefaultResources;

/**
 * Compiler plugin for validating WebSub service.
 *
 * @since 0.965.0
 */
@SupportedResourceParamTypes(
        expectedListenerType = @SupportedResourceParamTypes.Type(packageName = WEBSUB, name = WEBSUB_SERVICE_LISTENER),
        paramTypes = {
                @SupportedResourceParamTypes.Type(packageName = WEBSUB, name = WEBSUB_SERVICE_CALLER),
                @SupportedResourceParamTypes.Type(packageName = WEBSUB,
                        name = WEBSUB_INTENT_VERIFICATION_REQUEST),
                @SupportedResourceParamTypes.Type(packageName = WEBSUB, name = WEBSUB_NOTIFICATION_REQUEST)
        }
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
        int webSubAnnotationConfigCount = 0;
        for (AnnotationAttachmentNode annotation : annotations) {
            if (ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG.equals(annotation.getAnnotationName().getValue())) {
                webSubAnnotationConfigCount++;
                // TODO: 8/19/18 intro annotation validation if required
            }
        }

        if (webSubAnnotationConfigCount == 0) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                               "'" + ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG + "' annotation is compulsory");
            return;
        }

        BType listenerType = ((BLangService) serviceNode).listenerType;
        if (listenerType == null) {
            if (annotations.stream()
                    .anyMatch(annotation -> WEBSUB.equals(annotation.getPackageAlias().getValue()) &&
                            ANN_NAME_WEBSUB_SPECIFIC_SUBSCRIBER.equals(annotation.getAnnotationName().getValue()))) {
                return;
            }
        } else if (listenerType.tsymbol != null &&
                listenerType.tsymbol.pkgID.orgName.value.equals(BALLERINA) &&
                listenerType.tsymbol.pkgID.name.value.equals(WEBSUB) &&
                listenerType.tsymbol.name.value.equals(WEBSUB_SERVICE_LISTENER)) {
            return;
        }

        List<BLangFunction> resources = (List<BLangFunction>) serviceNode.getResources();
        if (resources.size() > 2) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                               "cannot have more than two resources with " + WEBSUB_PACKAGE + ":"
                                       + GENERIC_SUBSCRIBER_SERVICE_TYPE);
            return;
        }

        if (resources.size() < 1
                || (resources.size() == 1
                            && !(RESOURCE_NAME_ON_NOTIFICATION.equals(resources.get(0).getName().getValue())))) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                               "required resource '" + RESOURCE_NAME_ON_NOTIFICATION + "' not "
                                       + "specified with " + WEBSUB_PACKAGE + ":" +
                                       GENERIC_SUBSCRIBER_SERVICE_TYPE);
        }

        resources.forEach(res -> {
            validateDefaultResources(res, dlog);
        });
    }

}
