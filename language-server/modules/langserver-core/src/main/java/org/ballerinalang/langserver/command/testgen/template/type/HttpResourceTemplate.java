/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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
package org.ballerinalang.langserver.command.testgen.template.type;

import org.ballerinalang.langserver.command.testgen.TestGeneratorException;
import org.ballerinalang.langserver.command.testgen.renderer.RendererOutput;
import org.ballerinalang.langserver.command.testgen.renderer.TemplateBasedRendererOutput;
import org.ballerinalang.langserver.command.testgen.template.AbstractTestTemplate;
import org.ballerinalang.langserver.command.testgen.template.PlaceHolder;
import org.ballerinalang.net.http.HttpConstants;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.ballerinalang.langserver.command.testgen.AnnotationConfigsProcessor.searchArrayField;
import static org.ballerinalang.langserver.command.testgen.AnnotationConfigsProcessor.searchStringField;

/**
 * To represent a Resource template.
 *
 * @since 0.985.0
 */
public class HttpResourceTemplate extends AbstractTestTemplate {
    private final List<String[]> resourceMethods;
    private final String resourcePath;
    private final String serviceUriStrName;

    public HttpResourceTemplate(String serviceUriStrName, String basePath, BLangResource resource) {
        super(null);
        this.serviceUriStrName = serviceUriStrName;
        this.resourceMethods = new ArrayList<>();
        String resourceName = resource.name.value;

        // Resource path
        String tempResourcePath = basePath + "/" + resourceName;

        // If resource path & methods overridden by annotations
        List<BLangAnnotationAttachment> annAttachments = resource.annAttachments;
        if (annAttachments.size() > 0) {
            for (BLangAnnotationAttachment annotation : annAttachments) {
                List<String> methods = searchArrayField(HttpConstants.ANN_RESOURCE_ATTR_METHODS, annotation);
                methods.forEach(resourceMethod -> resourceMethods.add(new String[]{resourceName, resourceMethod}));
                Optional<String> annotPath = searchStringField(HttpConstants.ANN_RESOURCE_ATTR_PATH, annotation);
                tempResourcePath = basePath + annotPath.filter(path -> (!"/".equals(path))).orElse("");
            }
        } else {
            // Or else, add default resource method
            resourceMethods.add(new String[]{resourceName, "get"});
        }
        this.resourcePath = tempResourcePath;
    }

    /**
     * Renders content into this file template.
     *
     * @param rendererOutput root {@link RendererOutput}
     * @throws TestGeneratorException when template population process fails
     */
    @Override
    public void render(RendererOutput rendererOutput) throws TestGeneratorException {
        StringBuilder methods = new StringBuilder();
        for (String[] method : resourceMethods) {
            String resourceName = method[0];
            String resourceMethod = method[1].toLowerCase(Locale.getDefault());
            String resourceMethodAllCaps = resourceMethod.toUpperCase(Locale.getDefault());

            // Add method name, when multiple methods for the same resource
            String responseFieldMethodName = (resourceMethods.size() > 1) ? upperCaseFirstLetter(resourceMethod) : "";
            String responseFieldName = lowerCaseFirstLetter(resourceName) + responseFieldMethodName + "Response";

            // To handle additional `message` parameter in http methods such as put, post, delete
            String additionalParams = "";
            if (!"get".equals(resourceMethod) && !"head".equals(resourceMethod)) {
                additionalParams = ",\"\"";
            }

            RendererOutput resourceOutput = new TemplateBasedRendererOutput("httpResource.bal");
            resourceOutput.put(PlaceHolder.OTHER.get("resourceMethodAllCaps"), resourceMethodAllCaps);
            resourceOutput.put(PlaceHolder.OTHER.get("responseFieldName"), responseFieldName);
            resourceOutput.put(PlaceHolder.OTHER.get("resourceMethod"), resourceMethod);
            resourceOutput.put(PlaceHolder.OTHER.get("resourcePath"), resourcePath);
            resourceOutput.put(PlaceHolder.OTHER.get("additionalParams"), additionalParams);
            resourceOutput.put(PlaceHolder.OTHER.get("serviceUriStrName"), serviceUriStrName);
            methods.append(resourceOutput.getRenderedContent());
        }
        rendererOutput.append(PlaceHolder.OTHER.get("resources"), methods.toString());
    }
}
