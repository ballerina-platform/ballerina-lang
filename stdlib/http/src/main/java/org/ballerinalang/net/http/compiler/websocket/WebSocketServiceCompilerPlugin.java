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

package org.ballerinalang.net.http.compiler.websocket;

import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedResourceParamTypes;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Compiler plugin for validating WebSocket server service.
 *
 * @since 0.965.0
 */
@SupportedResourceParamTypes(
        expectedListenerType = @SupportedResourceParamTypes.Type(
                packageName = WebSocketConstants.PACKAGE_HTTP,
                name = WebSocketConstants.LISTENER
        ),
        paramTypes = {
                @SupportedResourceParamTypes.Type(
                        packageName = WebSocketConstants.PACKAGE_HTTP,
                        name = WebSocketConstants.WEBSOCKET_CALLER
                )
        }
)
public class WebSocketServiceCompilerPlugin extends AbstractCompilerPlugin {

    private DiagnosticLog dlog = null;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        dlog = diagnosticLog;
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        List<BLangFunction> resources = (List<BLangFunction>) serviceNode.getResources();
        // If first resource's first parameter is HttpCaller, do not process in this plugin.
        // This is done on the assumption of resources does not mix each other (HTTP and WebSocket)
        if (!resources.isEmpty() && !resources.get(0).getParameters().isEmpty()
                && HttpConstants.HTTP_CALLER_NAME.equals(resources.get(0).getParameters().get(0).type.toString())) {
            return;
        }
        validateAnnotationCountAndPath(serviceNode, annotations);
        resources.forEach(
                res -> new WebSocketServiceResourceValidator(dlog, res).validate());
    }

    private void validateAnnotationCountAndPath(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        int count = 0;
        for (AnnotationAttachmentNode annotation : annotations) {
            if (annotation.getAnnotationName().getValue().equals(
                    WebSocketConstants.WEBSOCKET_ANNOTATION_CONFIGURATION)) {
                validatePathAnnotationForPathParam(annotation);
                count++;
            }
        }
        if (count > 1) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                               "There cannot be more than one " + WebSocketConstants.WEBSOCKET_SERVICE +
                                       " annotations");
        }
    }

    private void validatePathAnnotationForPathParam(AnnotationAttachmentNode annotation) {
        List<BLangRecordLiteral.BLangRecordKeyValue> keyValues = new ArrayList<>();
        for (RecordLiteralNode.RecordField field : ((BLangRecordLiteral) annotation.getExpression()).fields) {
            keyValues.add((BLangRecordLiteral.BLangRecordKeyValue) field);
        }

        Optional<BLangRecordLiteral.BLangRecordKeyValue> pathPair = keyValues.stream().filter(
                pair -> pair.key.toString().equals("path")).findAny();
        if (pathPair.isPresent()) {
            BLangExpression valueExpr = pathPair.get().valueExpr;
            String pathParam = valueExpr.toString();
            if (pathParam.contains("{") || pathParam.contains("}")) {
                dlog.logDiagnostic(Diagnostic.Kind.ERROR, valueExpr.getPosition(),
                                   "Path params are not supported in service path");
            }
        }
    }
}
