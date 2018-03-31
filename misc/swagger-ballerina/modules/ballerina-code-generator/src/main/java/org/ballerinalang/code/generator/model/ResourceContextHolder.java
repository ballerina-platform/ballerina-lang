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
import org.ballerinalang.model.tree.VariableNode;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.ArrayList;
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

    /**
     * path wil be in the the format of /foo/{{bar}}
     * Note the double curly braces. This is to support string templating.
     */
    private String path;

    public static ResourceContextHolder buildContext(ResourceNode resource) throws CodeGeneratorException {
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
        AnnotationAttachmentNode ann = GeneratorUtils
                .getAnnotationFromList(GeneratorConstants.RES_CONFIG_ANNOTATION, GeneratorConstants.HTTP_PKG_ALIAS,
                        resource.getAnnotationAttachments());

        if (ann == null) {
            throw new CodeGeneratorException("Incomplete resource configuration found");
        }
        BLangRecordLiteral bLiteral = ((BLangRecordLiteral) ((BLangAnnotationAttachment) ann).getExpression());
        List<BLangRecordLiteral.BLangRecordKeyValue> list = bLiteral.getKeyValuePairs();
        Map<String, String[]> attrs = GeneratorUtils.getKeyValuePairAsMap(list);

        // We don't expect multiple http methods to be supported by single action
        // We only consider first content type for a single resource
        context.method = attrs.get(GeneratorConstants.ATTR_METHODS) != null ?
                attrs.get(GeneratorConstants.ATTR_METHODS)[0] :
                null;
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
