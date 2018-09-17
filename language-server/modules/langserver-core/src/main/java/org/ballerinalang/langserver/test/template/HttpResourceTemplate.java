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

package org.ballerinalang.langserver.test.template;

import org.ballerinalang.langserver.test.TestGeneratorException;
import org.ballerinalang.langserver.test.template.io.FileTemplate;
import org.ballerinalang.net.http.HttpConstants;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.ballerinalang.langserver.test.TestGeneratorUtil.getAnnotationValueAsArray;
import static org.ballerinalang.langserver.test.TestGeneratorUtil.getAnnotationValueAsString;
import static org.ballerinalang.langserver.test.TestGeneratorUtil.lowerCaseFirstLetter;
import static org.ballerinalang.langserver.test.TestGeneratorUtil.upperCaseFirstLetter;

/**
 * To represent a Resource template.
 */
public class HttpResourceTemplate implements BallerinaTestTemplate {
    private final List<String[]> resourceMethods;
    private final String resourcePath;
    private final String serviceUriStrName;

    public HttpResourceTemplate(String serviceUriStrName, String basePath, BLangResource resource) {
        this.serviceUriStrName = serviceUriStrName;
        this.resourceMethods = new ArrayList<>();
        String resourceName = resource.name.value;

        // Resource path
        String tempResourcePath = basePath + "/" + resourceName;

        // If resource path & methods overridden by annotations
        if (resource.annAttachments.size() > 0) {
            for (BLangAnnotationAttachment annotation : resource.annAttachments) {
                List<String> methods = getAnnotationValueAsArray(HttpConstants.ANN_RESOURCE_ATTR_METHODS, annotation);
                methods.forEach(val -> resourceMethods.add(new String[]{resourceName, val}));
                String annotPath = getAnnotationValueAsString(HttpConstants.ANN_RESOURCE_ATTR_PATH, annotation);
                tempResourcePath = basePath + (("/".equals(annotPath)) ? "" : annotPath);
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
     * @param rootFileTemplate root {@link FileTemplate}
     * @throws TestGeneratorException when template population process fails
     */
    @Override
    public void render(FileTemplate rootFileTemplate) throws TestGeneratorException {
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

            FileTemplate template = new FileTemplate("httpResource.bal");
            template.put("resourceMethodAllCaps", resourceMethodAllCaps);
            template.put("responseFieldName", responseFieldName);
            template.put("resourceMethod", resourceMethod);
            template.put("resourcePath", resourcePath);
            template.put("additionalParams", additionalParams);
            template.put("serviceUriStrName", serviceUriStrName);
            methods.append(template.getRenderedContent());
        }
        rootFileTemplate.append(HttpServiceTemplate.PLACEHOLDER_ATTR_RESOURCES, methods.toString());
    }
}
