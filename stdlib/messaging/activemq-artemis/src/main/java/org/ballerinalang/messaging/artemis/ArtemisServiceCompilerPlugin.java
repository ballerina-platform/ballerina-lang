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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.messaging.artemis;

import org.ballerinalang.compiler.plugins.SupportedResourceParamTypes;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.util.AbstractTransportCompilerPlugin;

import java.util.List;

/**
 * Compiler plugin for validating Artemis Listener service.
 *
 * @since 0.995.0
 */
@SupportedResourceParamTypes(
        expectedListenerType = @SupportedResourceParamTypes.Type(
                packageName = ArtemisConstants.ARTEMIS,
                name = ArtemisConstants.LISTENER_OBJ
        ),
        paramTypes = {
                @SupportedResourceParamTypes.Type(
                        packageName = ArtemisConstants.ARTEMIS,
                        name = ArtemisConstants.MESSAGE_OBJ
                )})
public class ArtemisServiceCompilerPlugin extends AbstractTransportCompilerPlugin {

    private DiagnosticLog dlog = null;
    private boolean onErrorAvailable = false;
    private boolean onMessageAvailable = false;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        dlog = diagnosticLog;
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        int count = 0;
        for (AnnotationAttachmentNode annotation : annotations) {
            if (annotation.getAnnotationName().getValue().equals(ArtemisConstants.SERVICE_CONFIG)) {
                count++;
            }
        }
        if (count == 0) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                    "There has to be an artemis:ServiceConfig annotation declared for service");
        }

        @SuppressWarnings(ArtemisConstants.UNCHECKED)
        List<BLangFunction> resources = (List<BLangFunction>) serviceNode.getResources();
        // More than two resource functions are found
        if (resources.size() > 2) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                    "Only onMessage and onError are allowed in the service");
        }
        // No resource functions are found
        if (resources.isEmpty()) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                    "There has to be at least one resource function declared for the service");
        } else {
            for (BLangFunction res : resources) {
                if (res.getName().getValue().equals(ArtemisConstants.ON_ERROR)) {
                    onErrorAvailable = true;
                } else if (res.getName().getValue().equals(ArtemisConstants.ON_MESSAGE)) {
                    onMessageAvailable = true;
                }
            }
            // Mandatory onMessage resource function not found
            if (!onMessageAvailable) {
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                        "There has to be onMessage resource declared for the service");
            }
            resources.forEach(
                    res -> ArtemisResourceValidator.validate(res, dlog, isResourceReturnsErrorOrNil(res),
                            onErrorAvailable)
            );
        }
    }
}
