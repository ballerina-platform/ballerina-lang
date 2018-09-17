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
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * To represent a Service template.
 */
public class RootTemplate implements BallerinaTestTemplate {

    public static final String PLACEHOLDER_ATTR_DECLARATIONS = "declarations";
    public static final String PLACEHOLDER_ATTR_CONTENT = "content";
    private static final String PLACEHOLDER_ATTR_IMPORTS = "imports";
    private static final String LINE_FEED = System.lineSeparator();
    private final List<BLangService> httpServices;
    private final List<BLangService> httpWSServices;
    private final List<BLangService> httpWSClientServices;
    private final List<BLangFunction> functions;
    private final BLangPackage bLangPackage;

    public RootTemplate(String fileName, BLangPackage bLangPackage) {
        this.bLangPackage = bLangPackage;
        httpServices = bLangPackage.getServices().stream()
                .filter(service -> fileName.equals(service.pos.src.cUnitName) &&
                        service.serviceTypeStruct.toString().equals("httpService"))
                .collect(Collectors.toList());
        httpWSServices = bLangPackage.getServices().stream()
                .filter(service -> fileName.equals(service.pos.src.cUnitName) &&
                        service.serviceTypeStruct.toString().equals("httpWebSocketService"))
                .collect(Collectors.toList());
        httpWSClientServices = bLangPackage.getServices().stream()
                .filter(service -> fileName.equals(service.pos.src.cUnitName) &&
                        service.serviceTypeStruct.toString().equals("httpWebSocketClientService"))
                .collect(Collectors.toList());
        functions = bLangPackage.getFunctions().stream()
                .filter(func -> fileName.equals(func.pos.src.cUnitName))
                .collect(Collectors.toList());
    }

    /**
     * Renders content into this file template.
     *
     * @param rootFileTemplate root {@link FileTemplate}
     * @throws TestGeneratorException when template population process fails
     */
    public void render(FileTemplate rootFileTemplate) throws TestGeneratorException {
        rootFileTemplate.append(PLACEHOLDER_ATTR_IMPORTS, "import ballerina/test;" + LINE_FEED);
        rootFileTemplate.append(PLACEHOLDER_ATTR_IMPORTS, "import ballerina/log;" + LINE_FEED);

        // Add imports
        if (httpServices.size() > 0 || httpWSServices.size() > 0 || httpWSClientServices.size() > 0) {
            rootFileTemplate.append(PLACEHOLDER_ATTR_IMPORTS, "import ballerina/http;" + LINE_FEED);
        }

        // Render test functions
        for (BLangFunction func : functions) {
            new FunctionTemplate(func).render(rootFileTemplate);
        }

        // Render httpService tests
        for (BLangService service : httpServices) {
            new HttpServiceTemplate(bLangPackage, service).render(rootFileTemplate);
        }

        // Render WS-Service tests
        for (BLangService service : httpWSServices) {
            new WSServiceTemplate(bLangPackage, service).render(rootFileTemplate);
        }

        // Render WS-ClientService tests
        for (BLangService service : httpWSClientServices) {
            new WSClientServiceTemplate(bLangPackage, service).render(rootFileTemplate);
        }
    }
}
