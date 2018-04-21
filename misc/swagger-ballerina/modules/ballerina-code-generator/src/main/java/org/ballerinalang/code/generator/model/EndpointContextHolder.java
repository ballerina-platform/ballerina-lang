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

package org.ballerinalang.code.generator.model;

import org.ballerinalang.code.generator.GeneratorConstants;
import org.ballerinalang.code.generator.util.GeneratorUtils;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.expressions.SimpleVariableReferenceNode;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

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
     * @param ep      {@link EndpointNode} which to extract details from
     * @return if {@code ep} is bound to the {@code service}, endpoint context will be returned.
     * otherwise empty null will be returned
     */
    public static EndpointContextHolder buildContext(ServiceNode service, EndpointNode ep) {
        EndpointContextHolder endpoint = null;
        for (SimpleVariableReferenceNode node : service.getBoundEndpoints()) {
            if (node.getVariableName().equals(ep.getName())) {
                endpoint = new EndpointContextHolder();
                AnnotationAttachmentNode ann = GeneratorUtils
                        .getAnnotationFromList("ServiceConfig", GeneratorConstants.HTTP_PKG_ALIAS,
                                service.getAnnotationAttachments());

                endpoint.extractDetails(ep, ann);
                break;
            }
        }

        return endpoint;
    }

    private void extractDetails(EndpointNode ep, AnnotationAttachmentNode ann) {
        this.name = ep.getName().getValue();

        BLangRecordLiteral bLiteral = (BLangRecordLiteral) ep.getConfigurationExpression();
        List<BLangRecordLiteral.BLangRecordKeyValue> list = bLiteral.getKeyValuePairs();
        Map<String, String[]> configs = GeneratorUtils.getKeyValuePairAsMap(list);

        String httpsPort = configs.get(GeneratorConstants.ATTR_HTTPS_PORT) != null ?
                configs.get(GeneratorConstants.ATTR_HTTPS_PORT)[0] :
                null;
        this.host =
                configs.get(GeneratorConstants.ATTR_HOST) != null ? configs.get(GeneratorConstants.ATTR_HOST)[0] : null;
        this.port = configs.get(GeneratorConstants.ATTR_HTTP_PORT) != null ?
                configs.get(GeneratorConstants.ATTR_HTTP_PORT)[0] :
                null;

        bLiteral = ((BLangRecordLiteral) ((BLangAnnotationAttachment) ann).getExpression());
        List<BLangRecordLiteral.BLangRecordKeyValue> attrList = bLiteral.getKeyValuePairs();
        Map<String, String[]> attrs = GeneratorUtils.getKeyValuePairAsMap(attrList);

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
