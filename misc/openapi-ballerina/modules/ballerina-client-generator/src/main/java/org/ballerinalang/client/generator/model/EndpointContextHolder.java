/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.client.generator.model;

import org.ballerinalang.client.generator.GeneratorConstants;
import org.ballerinalang.client.generator.util.GeneratorUtils;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;

import java.util.List;
import java.util.Map;

/**
 * * Context Model for holding endpoint information.
 */
public class EndpointContextHolder {
    private String name;
    private String host;
    private String port;
    private String basePath;
    private String url;

    /**
     * Build a parsable object model for a service endpoint. {@code ep} must be bound to the {@code service}
     * inorder to extract its details. If {@code ep} is not bound to the {@code service} null
     * will be returned
     *
     * @param service service with bound endpoints
     * @param ep      {@link BLangSimpleVariable} which to extract details from
     * @return if {@code ep} is bound to the {@code service}, endpoint context will be returned.
     * otherwise empty null will be returned
     */
    public static EndpointContextHolder buildContext(BLangService service, BLangSimpleVariable ep) {
        EndpointContextHolder endpoint = null;
        if (service == null || service.getAttachedExprs().isEmpty()
                || service.getAttachedExprs().get(0).getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return null;
        }

        for (BLangExpression node : service.getAttachedExprs()) {
            if (node instanceof BLangSimpleVarRef) {
                BLangSimpleVarRef variableRef = (BLangSimpleVarRef) node;
                if (variableRef.getVariableName().equals(ep.getName())) {
                    endpoint = new EndpointContextHolder();
                    AnnotationAttachmentNode ann = GeneratorUtils
                            .getAnnotationFromList("ServiceConfig", GeneratorConstants.HTTP_PKG_ALIAS,
                                    service.getAnnotationAttachments());

                    endpoint.extractDetails(ep, ann);
                    break;
                }
            }

        }

        return endpoint;
    }

    private void extractDetails(BLangSimpleVariable ep, AnnotationAttachmentNode ann) {
        this.name = ep.getName().getValue();

        BLangTypeInit bTypeInit = (BLangTypeInit) ep.getInitialExpression();
        List<BLangExpression> list = bTypeInit.argsExpr;
        String httpsPort = null;

//        List<BLangRecordLiteral.BLangRecordKeyValue> list = bLiteral.getFields();
//        Map<String, String[]> configs = GeneratorUtils.getKeyValuePairAsMap(list);

        if (list.size() == 1 && list.get(0) instanceof BLangLiteral) {
            port = list.get(0).toString();
        }

        if (list.size() > 1
                && list.get(1) instanceof BLangNamedArgsExpression
                && ((BLangNamedArgsExpression) list.get(1)).getExpression() instanceof BLangRecordLiteral) {
            BLangRecordLiteral bLangRecordLiteral = (BLangRecordLiteral) ((BLangNamedArgsExpression) list.get(1))
                    .getExpression();
            Map<String, String[]> configs = GeneratorUtils.getKeyValuePairAsMap(bLangRecordLiteral.fields);

            httpsPort = configs.get(GeneratorConstants.ATTR_HTTPS_PORT) != null ?
                    configs.get(GeneratorConstants.ATTR_HTTPS_PORT)[0] :
                    null;
            this.host =
                    configs.get(GeneratorConstants.ATTR_HOST) != null
                            ? configs.get(GeneratorConstants.ATTR_HOST)[0]
                            : null;
//        this.port = configs.get(GeneratorConstants.ATTR_HTTP_PORT) != null ?
//                configs.get(GeneratorConstants.ATTR_HTTP_PORT)[0] :
//                null;
        }

        BLangRecordLiteral bLiteral = ((BLangRecordLiteral) ((BLangAnnotationAttachment) ann).getExpression());
        Map<String, String[]> attrs = GeneratorUtils.getKeyValuePairAsMap(bLiteral.getFields());

        this.basePath = attrs.get(GeneratorConstants.ATTR_BASE_PATH) != null ?
                attrs.get(GeneratorConstants.ATTR_BASE_PATH)[0] :
                null;
        StringBuffer sb = new StringBuffer();

        // Select protocol according to given ports. HTTPS is given the priority
        if (httpsPort != null) {
            sb.append(GeneratorConstants.ATTR_HTTPS);
            port = httpsPort;
        } else {
            sb.append(GeneratorConstants.ATTR_HTTP);
        }

        // Set default values to host and port if values are not defined
        if (host == null) {
            host = GeneratorConstants.ATTR_DEF_HOST;
        }
        if (port == null) {
            port = GeneratorConstants.ATTR_DEF_PORT;
        }

        this.url = sb.append(host).append(':').append(port).append(basePath).toString();
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }
}
