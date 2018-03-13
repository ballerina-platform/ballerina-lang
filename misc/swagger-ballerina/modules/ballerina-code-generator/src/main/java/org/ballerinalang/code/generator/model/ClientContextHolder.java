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
import org.ballerinalang.code.generator.exception.CodeGeneratorException;
import org.ballerinalang.code.generator.util.GeneratorUtils;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ResourceNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.expressions.AnnotationAttachmentAttributeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Context holder for ballerina client generation.
 * This class will hold the context information required for ballerina code generation
 */
public class ClientContextHolder {
    private String name;
    private String url;
    private List<ResourceContextHolder> resources;

    /**
     * Build a parsable context from a Ballerina {@link ServiceNode}
     *
     * @param service {@code ServiceNode} for a valid ballerina source file
     * @return A parsable data model for provided ballerina {@code service}
     */
    public static ClientContextHolder buildContext(ServiceNode service) throws CodeGeneratorException {
        ClientContextHolder context = new ClientContextHolder();
        context.name = service.getName().getValue();
        context.resources = new ArrayList<>();

        AnnotationAttachmentNode ann = GeneratorUtils
                .getAnnotationFromList(GeneratorConstants.HTTP_CONFIG_ANNOTATION, GeneratorConstants.HTTP_PKG_ALIAS,
                        service.getAnnotationAttachments());
        if (ann == null) {
            throw new CodeGeneratorException("Incomplete service configuration found");
        }

        Map<String, AnnotationAttachmentAttributeNode> attrs = GeneratorUtils.getAttributeMap(ann.getAttributes());
        String host = GeneratorUtils.getAttributeValue(attrs.get("host"));
        String port = GeneratorUtils.getAttributeValue(attrs.get("port"));
        String httpsPort = GeneratorUtils.getAttributeValue(attrs.get("httpsPort"));
        String basePath = GeneratorUtils.getAttributeValue(attrs.get("basePath"));
        StringBuffer sb = new StringBuffer();

        // Select protocol according to given ports. HTTPS is given the priority
        if (httpsPort != null) {
            sb.append("https://");
            port = httpsPort;
        } else {
            sb.append("http://");
        }

        context.url = sb.append(host).append(':').append(port).append(basePath).toString();

        // Extract ballerina resource nodes as parsable resources
        for (ResourceNode resource: service.getResources()) {
            ResourceContextHolder operation = ResourceContextHolder.buildContext(resource);
            context.resources.add(operation);
        }

        return context;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public List<ResourceContextHolder> getResources() {
        return resources;
    }
}
