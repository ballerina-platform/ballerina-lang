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
import org.ballerinalang.langserver.commons.LSContext;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

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
    private final List<BLangFunction> resources;

    public HttpServiceTemplate(BLangPackage builtTestFile, BLangService service,
                               BLangTypeInit init,
                               BiConsumer<Integer, Integer> focusLineAcceptor,
                               LSContext context) {
        super(builtTestFile, focusLineAcceptor, context);
        String serviceName = service.name.value;
        String serviceUriTemp = HTTP + DEFAULT_IP + ":" + DEFAULT_PORT;

        boolean isSecureTemp = isSecureService(init);
        String protocol = ((isSecureTemp) ? HTTPS : HTTP);
        Optional<String> optionalPort = findServicePort(init);
        serviceUriTemp = optionalPort.map(port -> protocol + DEFAULT_IP + ":" + port).orElse(serviceUriTemp);

        this.isSecure = isSecureTemp;
        this.serviceUri = serviceUriTemp;

        // Retrieve Service base path
        String tempServiceBasePath = "/" + serviceName;

        // If service base path overridden by annotations
        for (BLangAnnotationAttachment annotation : service.annAttachments) {
            if (annotation.expr instanceof BLangRecordLiteral) {
                BLangRecordLiteral record = (BLangRecordLiteral) annotation.expr;
                Optional<String> basePath = searchStringField("basePath", record);
                tempServiceBasePath = basePath.orElse(tempServiceBasePath);
            }
        }
        this.serviceUriStrName = getSafeName(lowerCaseFirstLetter(serviceName) + "Uri");
        this.testServiceFunctionName = getSafeName("test" + upperCaseFirstLetter(serviceName));
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
        String httpEndpoint = getSafeName("httpEndpoint");
        RendererOutput serviceOutput = new TemplateBasedRendererOutput(filename);
        serviceOutput.put(PlaceHolder.OTHER.get("testServiceFunctionName"), testServiceFunctionName);
        serviceOutput.put(PlaceHolder.OTHER.get("serviceUriStrName"), serviceUriStrName);
        serviceOutput.put(PlaceHolder.OTHER.get("endpointName"), httpEndpoint);

        // Iterate through resources
        for (BLangFunction resource : resources) {
            HttpResourceTemplate resTemplate = new HttpResourceTemplate(serviceUriStrName, serviceBasePath, resource,
                                                                        httpEndpoint, context);
            resTemplate.render(serviceOutput);
        }

        //Append to root template
        rendererOutput.setFocusLineAcceptor(testServiceFunctionName, focusLineAcceptor);
        rendererOutput.append(PlaceHolder.DECLARATIONS, getServiceUriDeclaration() + LINE_SEPARATOR);
        rendererOutput.append(PlaceHolder.CONTENT, LINE_SEPARATOR + serviceOutput.getRenderedContent());
    }

    private String getServiceUriDeclaration() {
        return "string " + serviceUriStrName + " = \"" + serviceUri + "\";";
    }
}
