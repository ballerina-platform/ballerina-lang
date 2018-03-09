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

import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ResourceNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.expressions.AnnotationAttachmentAttributeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * ResourceContextHolder to hold ballerina resource information.
 */
public class ResourceContextHolder {
    private static final String RES_CONFIG_ANNOTATION = "resourceConfig";

    private String method;
    private String name;
    private String contentType;
    private List<ParameterContextHolder> parameters;

    /**
     * path wil be in the the format of /foo/{{bar}}
     * Note the double curly braces. This is to support string templating.
     */
    private String path;

    public static ResourceContextHolder buildContext(ResourceNode resource) {
        ResourceContextHolder context = new ResourceContextHolder();
        context.name = resource.getName().getValue();
        context.parameters = new ArrayList<>();

        for (VariableNode node: resource.getParameters()) {
            ParameterContextHolder parameter = ParameterContextHolder.buildContext(node);
            if (parameter != null) {
                context.parameters.add(parameter);
            }
        }

        // Iterate through all resource level annotations and find out resource configuration information
        for (AnnotationAttachmentNode ann: resource.getAnnotationAttachments()) {

            if (RES_CONFIG_ANNOTATION.equals(ann.getAnnotationName().getValue())) {
                for (AnnotationAttachmentAttributeNode attr: ann.getAttributes()) {
                    String attrName = attr.getName().getValue();

                    if ("path".equals(attrName)) {
                        context.path = context.getTemplatePath(attr.getValue().getValue().toString());
                    } else if ("methods".equals(attrName)) {
                        // Consider only first http method since we don't expect multiple http methods to be
                        // supported by single action
                        context.method = attr.getValue().getValueArray().get(0).getValue().toString();
                    } else if ("consumes".equals(attrName)) {
                        // We don't need to consider one of all content types for client generation
                        context.contentType = attr.getValue().getValueArray().get(0).getValue().toString();
                    }
                }

                break;
            }
        }

        return context;
    }

    private String getTemplatePath(String path) {
        String templatePath = path.replaceAll("\\{", "{{");
        templatePath = templatePath.replaceAll("}", "}}");

        return templatePath;
    }

    public String getMethod() {
        return method;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getContentType() {
        return contentType;
    }

    public List<ParameterContextHolder> getParameters() {
        return parameters;
    }
}
