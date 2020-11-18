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
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.command.testgen.TestGenerator;
import org.ballerinalang.langserver.command.testgen.TestGeneratorException;
import org.ballerinalang.langserver.command.testgen.renderer.RendererOutput;
import org.ballerinalang.langserver.command.testgen.renderer.TemplateBasedRendererOutput;
import org.ballerinalang.langserver.command.testgen.template.type.FunctionTemplate;
import org.ballerinalang.langserver.command.testgen.template.type.HttpServiceTemplate;
import org.ballerinalang.langserver.command.testgen.template.type.WSServiceTemplate;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.commons.LSContext;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * To represent a Service template.
 *
 * @since 0.985.0
 */
public class RootTemplate extends AbstractTestTemplate {
    public static final String LINE_FEED = System.lineSeparator();
    private final List<Pair<BLangService, BLangTypeInit>> httpServices = new ArrayList<>();
    private final List<Pair<BLangService, BLangTypeInit>> httpWSServices = new ArrayList<>();
    private final List<String[]> httpWSClientServices = new ArrayList<>();
    private final List<BLangFunction> functions = new ArrayList<>();

    private RootTemplate(BLangPackage bLangPackage, BiConsumer<Integer, Integer> focusLineAcceptor, LSContext context) {
        super(bLangPackage, focusLineAcceptor, context);
    }

    public RootTemplate(String fileName, BLangPackage builtTestFile,
                        BiConsumer<Integer, Integer> focusLineAcceptor, LSContext context) {
        super(builtTestFile, focusLineAcceptor, context);
        builtTestFile.getServices().forEach(service -> {
            String owner = service.listenerType.tsymbol.owner.name.value;
            String serviceTypeName = service.listenerType.tsymbol.name.value;

            Optional<BLangTypeInit> optionalServiceInit = TestGenerator.getServiceInit(builtTestFile, service);
            optionalServiceInit.ifPresent(init -> {
                if ("http".equals(owner)) {
                    switch (serviceTypeName) {
                        case "Listener":
                            httpServices.add(new ImmutablePair<>(service, init));
                            break;
                        case "WebSocketListener":
                            httpWSServices.add(new ImmutablePair<>(service, init));
                            break;
                        default:
                            // do nothing
                    }
                }
            });
        });
        builtTestFile.getFunctions().stream()
                .filter(func -> fileName.equals(func.pos.src.cUnitName))
                .forEach(functions::add);
    }

    /**
     * Create root template for a function.
     *
     * @param function          function
     * @param builtTestFile     built test file package
     * @param focusLineAcceptor focus line acceptor
     * @return root template
     */
    public static RootTemplate fromFunction(BLangFunction function, BLangPackage builtTestFile,
                                            BiConsumer<Integer, Integer> focusLineAcceptor, LSContext context) {
        RootTemplate rootTemplate = new RootTemplate(builtTestFile, focusLineAcceptor, context);
        rootTemplate.functions.add(function);
        return rootTemplate;
    }

    /**
     * Create root template for a http service.
     *
     * @param service           service
     * @param init              {@link BLangTypeInit}
     * @param builtTestFile     built test file package
     * @param focusLineAcceptor focus line acceptor
     * @return root template
     */
    public static RootTemplate fromHttpService(BLangService service, BLangTypeInit init, BLangPackage builtTestFile,
                                               BiConsumer<Integer, Integer> focusLineAcceptor, LSContext context) {
        RootTemplate rootTemplate = new RootTemplate(builtTestFile, focusLineAcceptor, context);
        rootTemplate.httpServices.add(new ImmutablePair<>(service, init));
        return rootTemplate;
    }

    /**
     * Create root template for a websocket service.
     *
     * @param service           service
     * @param init              {@link BLangTypeInit}
     * @param builtTestFile     built test file package
     * @param focusLineAcceptor focus line acceptor
     * @return root template
     */
    public static RootTemplate fromHttpWSService(BLangService service,
                                                 BLangTypeInit init,
                                                 BLangPackage builtTestFile,
                                                 BiConsumer<Integer, Integer> focusLineAcceptor, LSContext context) {
        RootTemplate rootTemplate = new RootTemplate(builtTestFile, focusLineAcceptor, context);
        rootTemplate.httpWSServices.add(new ImmutablePair<>(service, init));
        return rootTemplate;
    }

    /**
     * Renders content into this file template.
     *
     * @param rendererOutput root {@link TemplateBasedRendererOutput}
     * @throws TestGeneratorException when template population process fails
     */
    public void render(RendererOutput rendererOutput) throws TestGeneratorException {
        BiConsumer<String, String> ifExistCallback = (orgName, alias) -> {
            if (isNonExistImport(orgName, alias)) {
                rendererOutput.append(PlaceHolder.IMPORTS, "import " + orgName + "/" + alias + ";" + LINE_FEED);
                focusLineAcceptor.accept(null, 1); //Increment focus line by one
            }
        };
        ImportsAcceptor importsAcceptor = new ImportsAcceptor(context, ifExistCallback);

        // Add imports
        importsAcceptor.getAcceptor().accept("ballerina", "test");
        importsAcceptor.getAcceptor().accept("ballerina", "log");
        if (httpServices.size() > 0 || httpWSServices.size() > 0 || httpWSClientServices.size() > 0) {
            importsAcceptor.getAcceptor().accept("ballerina", "http");
        }

        // Render test functions
        for (BLangFunction func : functions) {
            //TODO: Fix this
//            TestFunctionGenerator generator = new TestFunctionGenerator(importsAcceptor, builtTestFile.packageID,
//                                                                          func, context);
            new FunctionTemplate(builtTestFile, func, focusLineAcceptor, null, context).render(rendererOutput);
        }

        // Render httpService tests
        for (Pair<BLangService, BLangTypeInit> pair : httpServices) {
            BLangService service = pair.getLeft();
            BLangTypeInit init = pair.getRight();
            new HttpServiceTemplate(builtTestFile, service, init, focusLineAcceptor, context).render(rendererOutput);
        }

        // Render WS-Service tests
        for (Pair<BLangService, BLangTypeInit> pair : httpWSServices) {
            BLangService service = pair.getLeft();
            BLangTypeInit init = pair.getRight();
            new WSServiceTemplate(builtTestFile, service, init, focusLineAcceptor, context).render(rendererOutput);
        }
        importsAcceptor.getNewImports().forEach(i -> {
            String[] importParts = i.split("/");
            imports.add(new ImmutablePair<>(importParts[0], importParts[1]));
        });
    }
}
