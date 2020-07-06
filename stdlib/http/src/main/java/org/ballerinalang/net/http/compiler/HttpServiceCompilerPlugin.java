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
import org.ballerinalang.compiler.plugins.SupportedResourceParamTypes;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.net.http.HttpConstants.ANN_CONFIG_ATTR_COMPRESSION;
import static org.ballerinalang.net.http.HttpConstants.ANN_CONFIG_ATTR_COMPRESSION_CONTENT_TYPES;
import static org.ballerinalang.net.http.HttpConstants.ANN_NAME_HTTP_SERVICE_CONFIG;

/**
 * Compiler plugin for validating HTTP service.
 *
 * @since 0.965.0
 */
@SupportedResourceParamTypes(expectedListenerType = @SupportedResourceParamTypes.Type(packageName = "http",
                                                                                        name = "Listener"),
                            paramTypes = {
                                    @SupportedResourceParamTypes.Type(packageName = "http", name = "Caller"),
                                    @SupportedResourceParamTypes.Type(packageName = "http", name = "Request")
                            }
)
public class HttpServiceCompilerPlugin extends AbstractCompilerPlugin {

    private DiagnosticLog dlog = null;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        dlog = diagnosticLog;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        List<BLangFunction> resources = (List<BLangFunction>) serviceNode.getResources();
        // If first resource's first parameter is WebSocketCaller, do not process in this plugin.
        // This is done on the assumption of resources does not mix each other (HTTP and WebSocket)
        if (resources.size() > 0 &&
                resources.get(0).getParameters().size() > 0 &&
                WebSocketConstants.WEBSOCKET_CALLER_NAME.equals(
                        resources.get(0).getParameters().get(0).type.toString())) {
            return;
        }
        int serviceConfigCount = 0;
        for (AnnotationAttachmentNode annotation : annotations) {
            if (annotation.getAnnotationName().getValue().equals(ANN_NAME_HTTP_SERVICE_CONFIG)) {
                handleServiceConfigAnnotation(serviceNode, (BLangAnnotationAttachment) annotation);
                serviceConfigCount++;
            }
        }
        if (serviceConfigCount > 1) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                               "multiple service configuration annotations found in service : " +
                                       serviceNode.getName().getValue());
        }
        resources.forEach(res -> {
            ResourceSignatureValidator.validate(res, dlog, res.pos);
        });
    }

    private void handleServiceConfigAnnotation(ServiceNode serviceNode, BLangAnnotationAttachment annotation) {
        // TODO: Handle service config annotation. Related issue #10476
        if (annotation.getExpression() == null) {
            return;
        }
        List<BLangRecordLiteral.BLangRecordKeyValueField> annotationValues = new ArrayList<>();
        for (RecordLiteralNode.RecordField field : ((BLangRecordLiteral) annotation.getExpression()).fields) {
            annotationValues.add((BLangRecordLiteral.BLangRecordKeyValueField) field);
        }

        int compressionConfigCount = 0;

        for (BLangRecordLiteral.BLangRecordKeyValueField keyValue : annotationValues) {
            // Validate compression configuration
            if (checkMatchingConfigKey(keyValue, ANN_CONFIG_ATTR_COMPRESSION.getValue())) {
                if (compressionConfigCount++ == 1) {
                    dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                                       "Invalid multiple configurations for compression");
                    return;
                }
                for (RecordLiteralNode.RecordField field : ((BLangRecordLiteral) keyValue.valueExpr).getFields()) {
                    BLangRecordLiteral.BLangRecordKeyValueField compressionConfig =
                            (BLangRecordLiteral.BLangRecordKeyValueField) field;
                    if (checkMatchingConfigKey(compressionConfig,
                                               ANN_CONFIG_ATTR_COMPRESSION_CONTENT_TYPES.getValue())) {
                        BLangListConstructorExpr valueArray = (BLangListConstructorExpr) compressionConfig.valueExpr;
                        if (valueArray.getExpressions().isEmpty()) {
                            break;
                        }
                        for (ExpressionNode expressionNode : valueArray.getExpressions()) {
                            String contentType = expressionNode.toString();
                            if (!MimeUtil.isValidateContentType(contentType)) {
                                dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(),
                                                   "Invalid Content-Type value for compression: '" + contentType + "'");
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean checkMatchingConfigKey(BLangRecordLiteral.BLangRecordKeyValueField keyValue, String key) {
        return ((BLangSimpleVarRef) (keyValue.key).expr).variableName.getValue().equals(key);
    }
}
