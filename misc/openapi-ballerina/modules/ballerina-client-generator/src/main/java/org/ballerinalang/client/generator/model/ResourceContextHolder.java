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
import org.ballerinalang.client.generator.exception.ClientGeneratorException;
import org.ballerinalang.client.generator.util.GeneratorUtils;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * ResourceContextHolder to hold ballerina resource information.
 */
public class ResourceContextHolder {
    private String method;
    private String name;
    private String contentType;
    private List<ParameterContextHolder> parameters;
    private boolean isMultiMethod;
    private List<String> supportedMethods;

    /**
     * path wil be in the the format of /foo/${bar}
     * Note the dollar symbol followed by the curly braces. This is to support string templating.
     */
    private String path;

    public static ResourceContextHolder buildContext(FunctionNode resource) throws ClientGeneratorException {
        ResourceContextHolder context = new ResourceContextHolder();
        context.name = resource.getName().getValue();
        context.parameters = new ArrayList<>();

        for (SimpleVariableNode node : resource.getParameters()) {
            ParameterContextHolder parameter = ParameterContextHolder.buildContext(node);
            if (parameter != null) {
                context.parameters.add(parameter);
            }
        }

        // Iterate through all resource level annotations and find out resource configuration information
        AnnotationAttachmentNode ann = GeneratorUtils
                .getAnnotationFromList(GeneratorConstants.RES_CONFIG_ANNOTATION, GeneratorConstants.HTTP_PKG_ALIAS,
                        resource.getAnnotationAttachments());

        if (ann == null) {
            throw new ClientGeneratorException("Incomplete resource configuration found");
        }
        Map<String, String[]> attrs = GeneratorUtils.getKeyValuePairAsMap(
                ((BLangRecordLiteral) ((BLangAnnotationAttachment) ann).getExpression()).getFields());

        if (attrs.get(GeneratorConstants.ATTR_METHODS) != null) {

            // If multiple http methods are supported by this resource. Mark those separately
            if (attrs.get(GeneratorConstants.ATTR_METHODS).length > 1) {
                context.isMultiMethod = true;
                context.supportedMethods = Arrays.asList(attrs.get(GeneratorConstants.ATTR_METHODS));
            } else {
                context.method = attrs.get(GeneratorConstants.ATTR_METHODS)[0];
            }

        } else {
            context.method = null;
        }

        context.contentType = attrs.get(GeneratorConstants.ATTR_CONSUMES) != null ?
                attrs.get(GeneratorConstants.ATTR_CONSUMES)[0] :
                null;
        String path =
                attrs.get(GeneratorConstants.ATTR_PATH) != null ? attrs.get(GeneratorConstants.ATTR_PATH)[0] : null;
        context.path = context.getTemplatePath(path);

        return context;
    }

    private String getTemplatePath(String path) {
        if (path == null) {
            return null;
        }

        return path.replaceAll("\\{", "\\$\\{");
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

    public boolean isMultiMethod() {
        return isMultiMethod;
    }

    public List<String> getSupportedMethods() {
        return supportedMethods;
    }
}
