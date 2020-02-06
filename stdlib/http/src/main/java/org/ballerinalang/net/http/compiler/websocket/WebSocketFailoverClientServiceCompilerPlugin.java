/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.compiler.websocket;

import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedResourceParamTypes;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;

import java.util.List;

import static org.ballerinalang.net.http.websocket.WebSocketConstants.FAILOVER_WEBSOCKET_CLIENT;

/**
 * Compiler plugin for validating the failover WebSocket service.
 *
 * @since 1.2.0
 */
@SupportedResourceParamTypes(
        paramTypes = {
                @SupportedResourceParamTypes.Type(
                        packageName = WebSocketConstants.PACKAGE_HTTP,
                        name = FAILOVER_WEBSOCKET_CLIENT
                )
        }
)
public class WebSocketFailoverClientServiceCompilerPlugin extends AbstractCompilerPlugin {

    private DiagnosticLog dlog = null;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        dlog = diagnosticLog;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        List<BLangFunction> resources = (List<BLangFunction>) serviceNode.getResources();
        resources.forEach(
                res -> new WebSocketFailoverClientResourceValidator(dlog, res).validate());
    }
}
