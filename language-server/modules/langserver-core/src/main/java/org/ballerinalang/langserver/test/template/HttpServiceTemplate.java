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
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.expressions.SimpleVariableReferenceNode;
import org.ballerinalang.net.http.HttpConstants;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.List;

import static io.netty.util.internal.StringUtil.LINE_FEED;
import static org.ballerinalang.langserver.test.TestGeneratorUtil.getRecordValueAsString;
import static org.ballerinalang.langserver.test.TestGeneratorUtil.isRecordValueExists;
import static org.ballerinalang.langserver.test.TestGeneratorUtil.lowerCaseFirstLetter;
import static org.ballerinalang.langserver.test.TestGeneratorUtil.upperCaseFirstLetter;

/**
 * To represent a Service template.
 */
public class HttpServiceTemplate implements BallerinaTestTemplate {
    private final String serviceUri;
    private final boolean isSecure;
    private final String serviceUriStrName;
    private final String testServiceFunctionName;
    private final String serviceBasePath;
    private final List<BLangResource> resources;
    public final static String PLACEHOLDER_ATTR_RESOURCES = "resources";

    public HttpServiceTemplate(BLangPackage bLangPackage, BLangService service) {
        String serviceName = service.name.value;
        this.serviceUriStrName = lowerCaseFirstLetter(serviceName) + "Uri";
        this.testServiceFunctionName = "test" + upperCaseFirstLetter(serviceName);
        boolean isSecureTemp = false;
        final String[] tempServiceUri = {"http://0.0.0.0:9092"};

        // If Anonymous Endpoint bounded, get `port` and `isSecure` from it
        BLangRecordLiteral anonEndpointBind = service.anonymousEndpointBind;
        if (anonEndpointBind != null) {
            String port = getRecordValueAsString(HttpConstants.ANN_CONFIG_ATTR_PORT, anonEndpointBind);
            isSecureTemp = isRecordValueExists(HttpConstants.ENDPOINT_CONFIG_SECURE_SOCKET, anonEndpointBind);
            tempServiceUri[0] =
                    (port != null) ? ((isSecureTemp) ? "https" : "http") + "://0.0.0.0:" + port : tempServiceUri[0];
        }

        // Check for the bounded endpoint to get `port` and `isSecure` from it
        List<? extends SimpleVariableReferenceNode> boundEndpoints = service.getBoundEndpoints();
        EndpointNode endpoint = (boundEndpoints.size() > 0) ? bLangPackage.getGlobalEndpoints().stream()
                .filter(ep -> service.getBoundEndpoints().get(0).getVariableName().getValue()
                        .equals(ep.getName().getValue()))
                .findFirst().orElse(null) : null;
        if (endpoint != null && endpoint.getConfigurationExpression() instanceof BLangRecordLiteral) {
            BLangRecordLiteral configs = (BLangRecordLiteral) endpoint.getConfigurationExpression();
            String port = getRecordValueAsString(HttpConstants.ANN_CONFIG_ATTR_PORT, configs);
            isSecureTemp = isRecordValueExists(HttpConstants.ENDPOINT_CONFIG_SECURE_SOCKET, configs);
            tempServiceUri[0] =
                    (port != null) ? ((isSecureTemp) ? "https" : "http") + "://0.0.0.0:" + port : tempServiceUri[0];
        }
        this.isSecure = isSecureTemp;
        this.serviceUri = tempServiceUri[0];

        // Retrieve Service base path
        String tempServiceBasePath = "/" + serviceName;
        // If service base path overridden by annotations
        for (BLangAnnotationAttachment annotation : service.annAttachments) {
            if (annotation.expr instanceof BLangRecordLiteral) {
                BLangRecordLiteral record = (BLangRecordLiteral) annotation.expr;
                String basePath = getRecordValueAsString(HttpConstants.ANN_CONFIG_ATTR_BASE_PATH, record);
                tempServiceBasePath = (basePath != null) ? basePath : tempServiceBasePath;
            }
        }
        this.serviceBasePath = tempServiceBasePath;
        this.resources = service.getResources();
    }

    private String getServiceUriDeclarationString() {
        return "string " + serviceUriStrName + " = \"" + serviceUri + "\";";
    }

    /**
     * Renders content into this file template.
     *
     * @param rootFileTemplate root {@link FileTemplate}
     * @throws TestGeneratorException when template population process fails
     */
    public void render(FileTemplate rootFileTemplate) throws TestGeneratorException {
        String filename = (isSecure) ? "httpsService.bal" : "httpService.bal";
        FileTemplate template = new FileTemplate(filename);
        template.put("testServiceFunctionName", testServiceFunctionName);
        template.put("serviceUriStrName", serviceUriStrName);

        // Iterate through resources
        for (BLangResource resource : resources) {
            HttpResourceTemplate resTemplate = new HttpResourceTemplate(serviceUriStrName, serviceBasePath, resource);
            resTemplate.render(template);
        }

        //Append to root template
        rootFileTemplate.append(RootTemplate.PLACEHOLDER_ATTR_DECLARATIONS,
                                getServiceUriDeclarationString() + LINE_FEED);
        rootFileTemplate.append(RootTemplate.PLACEHOLDER_ATTR_CONTENT, template.getRenderedContent());
    }
}
