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
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.expressions.SimpleVariableReferenceNode;
import org.ballerinalang.net.http.HttpConstants;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.List;
import java.util.Optional;

import static org.ballerinalang.langserver.command.testgen.AnnotationConfigsProcessor.isRecordValueExists;
import static org.ballerinalang.langserver.command.testgen.AnnotationConfigsProcessor.searchStringField;
import static org.ballerinalang.langserver.common.utils.CommonUtil.LINE_SEPARATOR;

/**
 * To represent a Service template.
 *
 * @since 0.985.0
 */
public class HttpServiceTemplate extends AbstractTestTemplate {
    private final String serviceUri;
    private final boolean isSecure;
    private final String serviceUriStrName;
    private final String testServiceFunctionName;
    private final String serviceBasePath;
    private final List<BLangResource> resources;

    public HttpServiceTemplate(BLangPackage builtTestFile,
                               List<? extends EndpointNode> globalEndpoints, BLangService service) {
        super(builtTestFile);
        String serviceName = service.name.value;
        boolean isSecureTemp = false;
        String serviceUriTemp = HTTP + DEFAULT_IP + ":" + DEFAULT_PORT;

        // If Anonymous Endpoint bounded, get `port` and `isSecure` from it
        BLangRecordLiteral anonEndpointBind = service.anonymousEndpointBind;
        if (anonEndpointBind != null) {
            Optional<String> optionalPort = searchStringField(HttpConstants.ANN_CONFIG_ATTR_PORT, anonEndpointBind);
            isSecureTemp = isRecordValueExists(HttpConstants.ENDPOINT_CONFIG_SECURE_SOCKET, anonEndpointBind);
            String protocol = ((isSecureTemp) ? HTTPS : HTTP);
            serviceUriTemp = optionalPort.map(port -> protocol + DEFAULT_IP + ":" + port).orElse(serviceUriTemp);
        }

        // Check for the bounded endpoint to get `port` and `isSecure` from it
        List<? extends SimpleVariableReferenceNode> boundEndpoints = service.getBoundEndpoints();
        EndpointNode endpoint = (boundEndpoints.size() > 0) ? globalEndpoints.stream()
                .filter(ep -> service.getBoundEndpoints().get(0).getVariableName().getValue()
                        .equals(ep.getName().getValue()))
                .findFirst().orElse(null) : null;
        if (endpoint != null && endpoint.getConfigurationExpression() instanceof BLangRecordLiteral) {
            BLangRecordLiteral configs = (BLangRecordLiteral) endpoint.getConfigurationExpression();
            Optional<String> optionalPort = searchStringField(HttpConstants.ANN_CONFIG_ATTR_PORT, configs);
            isSecureTemp = isRecordValueExists(HttpConstants.ENDPOINT_CONFIG_SECURE_SOCKET, configs);
            String protocol = ((isSecureTemp) ? HTTPS : HTTP);
            serviceUriTemp = optionalPort.map(port -> protocol + DEFAULT_IP + ":" + port).orElse(serviceUriTemp);
        }

        this.isSecure = isSecureTemp;
        this.serviceUri = serviceUriTemp;

        // Retrieve Service base path
        String tempServiceBasePath = "/" + serviceName;

        // If service base path overridden by annotations
        for (BLangAnnotationAttachment annotation : service.annAttachments) {
            if (annotation.expr instanceof BLangRecordLiteral) {
                BLangRecordLiteral record = (BLangRecordLiteral) annotation.expr;
                Optional<String> basePath = searchStringField(HttpConstants.ANN_CONFIG_ATTR_BASE_PATH, record);
                tempServiceBasePath = basePath.orElse(tempServiceBasePath);
            }
        }
        this.serviceUriStrName = getSafeGlobalVariableName(lowerCaseFirstLetter(serviceName) + "Uri");
        this.testServiceFunctionName = getSafeFunctionName("test" + upperCaseFirstLetter(serviceName));
        this.serviceBasePath = tempServiceBasePath;
        this.resources = service.getResources();
    }

    /**
     * Renders content into this file template.
     *
     * @param rendererOutput root {@link RendererOutput}
     * @throws TestGeneratorException when template population process fails
     */
    public void render(RendererOutput rendererOutput) throws TestGeneratorException {
        String filename = (isSecure) ? "httpsService.bal" : "httpService.bal";
        RendererOutput serviceOutput = new TemplateBasedRendererOutput(filename);
        serviceOutput.put(PlaceHolder.OTHER.get("testServiceFunctionName"), testServiceFunctionName);
        serviceOutput.put(PlaceHolder.OTHER.get("serviceUriStrName"), serviceUriStrName);

        // Iterate through resources
        for (BLangResource resource : resources) {
            HttpResourceTemplate resTemplate = new HttpResourceTemplate(serviceUriStrName, serviceBasePath, resource);
            resTemplate.render(serviceOutput);
        }

        //Append to root template
        rendererOutput.append(PlaceHolder.DECLARATIONS, getServiceUriDeclaration() + LINE_SEPARATOR);
        rendererOutput.append(PlaceHolder.CONTENT, serviceOutput.getRenderedContent());
    }

    private String getServiceUriDeclaration() {
        return "string " + serviceUriStrName + " = \"" + serviceUri + "\";";
    }
}
