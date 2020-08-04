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
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;

import java.util.Optional;
import java.util.function.BiConsumer;

import static io.netty.util.internal.StringUtil.LINE_FEED;
import static org.ballerinalang.langserver.common.utils.CommonUtil.LINE_SEPARATOR;

/**
 * To represent a Service template.
 *
 * @since 0.985.0
 */
@Deprecated
public class WSServiceTemplate extends AbstractTestTemplate {
    private final String serviceUri;
    private final boolean isSecure;
    private final String serviceUriStrName;
    private final String serviceChannelName;
    private final String testServiceFunctionName;
    private final String callbackServiceName;

    public WSServiceTemplate(BLangPackage builtTestFile, BLangService service,
                             BLangTypeInit init,
                             BiConsumer<Integer, Integer> focusLineAcceptor,
                             LSContext context) {
        super(builtTestFile, focusLineAcceptor, context);
        String tempServiceUri = WS + DEFAULT_IP + ":" + DEFAULT_PORT;
        boolean isSecureTemp = isSecureService(init);
        String protocol = ((isSecureTemp) ? WSS : WS);
        Optional<String> optionalPort = findServicePort(init);
        tempServiceUri = optionalPort.map(port -> protocol + DEFAULT_IP + ":" + port).orElse(tempServiceUri);

        // Service base path
        String serviceBasePath = "/" + service.name.value;

        // If service base path overridden by annotations
//        for (BLangAnnotationAttachment annotation : service.annAttachments) {
//            Optional<String> optionalPath = searchStringField(WebSocketConstants.ANNOTATION_ATTR_PATH.getValue(),
//                                                              annotation);
//            Optional<String> optionalPath = Optional.empty();
//            serviceBasePath = optionalPath.orElse("");
//        }
        String serviceName = upperCaseFirstLetter(service.name.value);
        this.serviceUriStrName = getSafeName(lowerCaseFirstLetter(service.name.value) + "Uri");
        this.serviceChannelName = getSafeName(lowerCaseFirstLetter(service.name.value) + "Reply");
        this.testServiceFunctionName = getSafeName("test" + serviceName);
        this.callbackServiceName = getSafeName("callback" + serviceName + "Service");
        this.serviceUri = tempServiceUri + serviceBasePath;
        this.isSecure = isSecureTemp;
    }

    /**
     * Renders content into this file template.
     *
     * @param rendererOutput root {@link RendererOutput}
     * @throws TestGeneratorException when template population process fails
     */
    @Override
    public void render(RendererOutput rendererOutput) throws TestGeneratorException {
        String filename = (isSecure) ? "wssService.bal" : "wsService.bal";
        RendererOutput serviceOutput = new TemplateBasedRendererOutput(filename);
        serviceOutput.put(PlaceHolder.OTHER.get("testServiceFunctionName"), testServiceFunctionName);
        serviceOutput.put(PlaceHolder.OTHER.get("serviceUriStrName"), serviceUriStrName);
        serviceOutput.put(PlaceHolder.OTHER.get("callbackServiceName"), callbackServiceName);
        serviceOutput.put(PlaceHolder.OTHER.get("endpointName"), getSafeName("wsClient"));
        serviceOutput.put(PlaceHolder.OTHER.get("wsReplyChannel"), serviceChannelName);

        //Append to root template
        rendererOutput.setFocusLineAcceptor(testServiceFunctionName, focusLineAcceptor);
        rendererOutput.append(PlaceHolder.DECLARATIONS, getServiceUriDeclaration() + LINE_FEED);
        rendererOutput.append(PlaceHolder.DECLARATIONS, getChannelDeclaration() + LINE_FEED);
        rendererOutput.append(PlaceHolder.CONTENT, LINE_SEPARATOR + serviceOutput.getRenderedContent());
    }

    private String getServiceUriDeclaration() {
        return "string " + serviceUriStrName + " = \"" + serviceUri + "\";";
    }

    private String getChannelDeclaration() {
        return "channel<string> " + serviceChannelName + " = new;";
    }
}
