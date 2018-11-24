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
package org.ballerinalang.langserver.command.testgen.template;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.ballerinalang.langserver.command.testgen.TestGeneratorException;
import org.ballerinalang.langserver.command.testgen.renderer.RendererOutput;
import org.ballerinalang.langserver.command.testgen.renderer.TemplateBasedRendererOutput;
import org.ballerinalang.langserver.command.testgen.template.type.FunctionTemplate;
import org.ballerinalang.langserver.command.testgen.template.type.HttpServiceTemplate;
import org.ballerinalang.langserver.command.testgen.template.type.WSClientServiceTemplate;
import org.ballerinalang.langserver.command.testgen.template.type.WSServiceTemplate;
import org.ballerinalang.model.tree.EndpointNode;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * To represent a Service template.
 *
 * @since 0.985.0
 */
public class RootTemplate extends AbstractTestTemplate {
    public static final String LINE_FEED = System.lineSeparator();
    private final List<BLangService> httpServices = new ArrayList<>();
    private final List<BLangService> httpWSServices = new ArrayList<>();
    private final List<BLangService> httpWSClientServices = new ArrayList<>();
    private final List<BLangFunction> functions = new ArrayList<>();

    private final List<EndpointNode> globalEndpoints = new ArrayList<>();
    private final List<BLangFunction> globalFunctions = new ArrayList<>();

    private RootTemplate(BLangPackage bLangPackage) {
        super(bLangPackage);
    }

    public RootTemplate(String fileName, BLangPackage builtTestFile) {
        super(builtTestFile);
        builtTestFile.getServices().stream()
                .filter(service -> fileName.equals(service.pos.src.cUnitName) &&
                        service.serviceTypeStruct.toString().equals("httpService"))
                .forEach(httpServices::add);
        builtTestFile.getServices().stream()
                .filter(service -> fileName.equals(service.pos.src.cUnitName) &&
                        service.serviceTypeStruct.toString().equals("httpWebSocketService"))
                .forEach(httpWSServices::add);
        builtTestFile.getServices().stream()
                .filter(service -> fileName.equals(service.pos.src.cUnitName) &&
                        service.serviceTypeStruct.toString().equals("httpWebSocketClientService"))
                .forEach(httpWSClientServices::add);
        builtTestFile.getFunctions().stream()
                .filter(func -> fileName.equals(func.pos.src.cUnitName))
                .forEach(functions::add);
        globalEndpoints.addAll(builtTestFile.getGlobalEndpoints());
        globalFunctions.addAll(builtTestFile.getFunctions());
    }

    /**
     * Create root template for a function.
     *
     * @param function function
     * @param builtTestFile built test file package
     * @return root template
     */
    public static RootTemplate fromFunction(BLangFunction function, BLangPackage builtTestFile) {
        RootTemplate rootTemplate = new RootTemplate(builtTestFile);
        rootTemplate.functions.add(function);
        return rootTemplate;
    }

    /**
     * Create root template for a http service.
     *
     * @param service service
     * @param builtTestFile built test file package
     * @return root template
     */
    public static RootTemplate fromHttpService(BLangService service, BLangPackage builtTestFile) {
        RootTemplate rootTemplate = new RootTemplate(builtTestFile);
        rootTemplate.globalEndpoints.addAll(builtTestFile.getGlobalEndpoints());
        rootTemplate.httpServices.add(service);
        return rootTemplate;
    }

    /**
     * Create root template for a websocket service.
     *
     * @param service service
     * @param builtTestFile built test file package
     * @return root template
     */
    public static RootTemplate fromHttpWSService(BLangService service, BLangPackage builtTestFile) {
        RootTemplate rootTemplate = new RootTemplate(builtTestFile);
        rootTemplate.globalEndpoints.addAll(builtTestFile.getGlobalEndpoints());
        rootTemplate.httpWSServices.add(service);
        return rootTemplate;
    }

    /**
     * Create root template for a client websocket service.
     *
     * @param service service
     * @param builtTestFile built test file package
     * @return root template
     */
    public static RootTemplate fromHttpClientWSService(BLangService service, BLangPackage builtTestFile) {
        RootTemplate rootTemplate = new RootTemplate(builtTestFile);
        rootTemplate.globalFunctions.addAll(builtTestFile.getFunctions());
        rootTemplate.httpWSServices.add(service);
        return rootTemplate;
    }

    /**
     * Renders content into this file template.
     *
     * @param rendererOutput root {@link TemplateBasedRendererOutput}
     * @throws TestGeneratorException when template population process fails
     */
    public void render(RendererOutput rendererOutput) throws TestGeneratorException {
        // Add imports
        if (rendererOutput.isNewTestFile() || isNonExistImport("ballerina", "test")) {
            rendererOutput.append(PlaceHolder.IMPORTS, "import ballerina/test;" + LINE_FEED);
            imports.add(new ImmutablePair<>("ballerina", "test"));
        }
        if (rendererOutput.isNewTestFile() || isNonExistImport("ballerina", "log")) {
            rendererOutput.append(PlaceHolder.IMPORTS, "import ballerina/log;" + LINE_FEED);
            imports.add(new ImmutablePair<>("ballerina", "log"));
        }
        if (httpServices.size() > 0 || httpWSServices.size() > 0 || httpWSClientServices.size() > 0) {
            if (rendererOutput.isNewTestFile() || isNonExistImport("ballerina", "http")) {
                rendererOutput.append(PlaceHolder.IMPORTS, "import ballerina/http;" + LINE_FEED);
                imports.add(new ImmutablePair<>("ballerina", "http"));
            }
        }

        BiConsumer<String, String> importsConsumer = (orgName, alias) -> {
            if (isNonExistImport(orgName, alias)) {
                rendererOutput.append(PlaceHolder.IMPORTS, "import " + orgName + "/" + alias + ";" + LINE_FEED);
                imports.add(new ImmutablePair<>(orgName, alias));
            }
        };

        // Render test functions
        for (BLangFunction func : functions) {
            new FunctionTemplate(importsConsumer, builtTestFile, func).render(rendererOutput);
        }

        // Render httpService tests
        for (BLangService service : httpServices) {
            new HttpServiceTemplate(builtTestFile, globalEndpoints, service).render(rendererOutput);
        }

        // Render WS-Service tests
        for (BLangService service : httpWSServices) {
            new WSServiceTemplate(builtTestFile, globalEndpoints, service).render(rendererOutput);
        }

        // Render WS-ClientService tests
        for (BLangService service : httpWSClientServices) {
            new WSClientServiceTemplate(builtTestFile, globalFunctions, service).render(rendererOutput);
        }
    }
}
