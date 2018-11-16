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

import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.langserver.command.testgen.TestGeneratorException;
import org.ballerinalang.langserver.command.testgen.renderer.RendererOutput;
import org.ballerinalang.langserver.command.testgen.renderer.TemplateBasedRendererOutput;
import org.ballerinalang.langserver.command.testgen.template.AbstractTestTemplate;
import org.ballerinalang.langserver.command.testgen.template.PlaceHolder;
import org.ballerinalang.net.http.WebSocketConstants;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ballerinalang.langserver.command.testgen.AnnotationConfigsProcessor.searchArrayField;
import static org.ballerinalang.langserver.command.testgen.AnnotationConfigsProcessor.searchStringField;

/**
 * To represent a Service template.
 *
 * @since 0.985.0
 */
public class WSClientServiceTemplate extends AbstractTestTemplate {
    private static final Pattern WS_PATTERN = Pattern.compile("^(wss?):\\/\\/([A-Z\\d\\.-]{2,})[:]*(\\d{2,4})?(.*)");
    private final List<String[]> servicesList;

    public WSClientServiceTemplate(BLangPackage builtTestFile,
                                   List<BLangFunction> functions, BLangService service) {
        super(builtTestFile);
        String serviceName = service.name.value;
        this.servicesList = new ArrayList<>();
        // Iterate functions to find urls of the endpoints which has callbackService as current WSClientService
        functions.forEach(func -> func.endpoints.stream()
                .filter(ep -> {
                    Optional<String> optionalConfig = searchStringField(WebSocketConstants.CLIENT_SERVICE_CONFIG,
                                                                        (BLangRecordLiteral) ep.configurationExpr);
                    return "WebSocketClient".equals(ep.endpointTypeNode.typeName.value) &&
                            optionalConfig.isPresent() && serviceName.equals(optionalConfig.get());
                })
                .forEach(ep -> {
                             Object value = searchArrayField(WebSocketConstants.CLIENT_URL_CONFIG,
                                                             (BLangRecordLiteral) ep.configurationExpr);
                             String testFunctionName =
                                     getSafeFunctionName("test" + upperCaseFirstLetter(ep.name.value));
                             String mockServiceName =
                                     getSafeServiceName("mock" + upperCaseFirstLetter(ep.name.value) + "Service");
                             String url = (value instanceof BLangLiteral) ? "\"" + value + "\"" : value.toString();
                             servicesList.add(new String[]{testFunctionName, mockServiceName, url});
                         }
                )
        );
    }

    /**
     * Renders content into this file template.
     *
     * @param rendererOutput root {@link RendererOutput}
     * @throws TestGeneratorException when template population process fails
     */
    @Override
    public void render(RendererOutput rendererOutput) throws TestGeneratorException {
        StringBuilder content = new StringBuilder();
        Iterator<String[]> servicesIterator = servicesList.iterator();
        while (servicesIterator.hasNext()) {
            String[] service = servicesIterator.next();
            String funcName = service[0];
            String mckServiceName = service[1];
            String uri = service[2];

            //Set default path and port
            String servicePath = "\"/\"";
            String servicePort = "9090";

            //If service uri is string literal, fetch path and port from it
            if (uri.startsWith("\"")) {
                Matcher matcher = WS_PATTERN.matcher(uri.substring(1, uri.length() - 1));
                if (matcher.find()) {
                    String port = matcher.group(3);
                    String path = matcher.group(4);
                    servicePath = (path != null) ? "\"" + path + "\"" : servicePath;
                    servicePort = (port != null) ? port : servicePort;
                }
            }
            String filename = (uri.startsWith("wss:")) ? "wssClientService.bal" : "wsClientService.bal";
            RendererOutput serviceOutput = new TemplateBasedRendererOutput(filename);
            serviceOutput.put(PlaceHolder.OTHER.get("testServiceFunctionName"), funcName);
            serviceOutput.put(PlaceHolder.OTHER.get("mockServiceName"), mckServiceName);
            serviceOutput.put(PlaceHolder.OTHER.get("serviceUriStrName"), uri);
            serviceOutput.put(PlaceHolder.OTHER.get("mockServicePath"), servicePath);
            serviceOutput.put(PlaceHolder.OTHER.get("mockServicePort"), servicePort);
            serviceOutput.put(PlaceHolder.OTHER.get("request"), "hey");
            serviceOutput.put(PlaceHolder.OTHER.get("mockResponse"), "hey");
            String renderedContent = serviceOutput.getRenderedContent();
            if (!servicesIterator.hasNext()) {
                // If last, trim right-side
                renderedContent = StringUtils.stripEnd(renderedContent, null);
            }
            content.append(renderedContent);
        }
        //Append to root template
        rendererOutput.append(PlaceHolder.CONTENT, content.toString());
    }
}
