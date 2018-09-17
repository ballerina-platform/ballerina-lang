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

import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.langserver.test.TestGeneratorException;
import org.ballerinalang.langserver.test.template.io.FileTemplate;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.net.http.WebSocketConstants;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.test.TestGeneratorUtil.getRecordValue;
import static org.ballerinalang.langserver.test.TestGeneratorUtil.getRecordValueAsString;
import static org.ballerinalang.langserver.test.TestGeneratorUtil.upperCaseFirstLetter;

/**
 * To represent a Service template.
 */
public class WSClientServiceTemplate implements BallerinaTestTemplate {
    private final List<String[]> servicesList;
    private static final Pattern WS_PATTERN = Pattern.compile("^(wss?):\\/\\/([A-Z\\d\\.-]{2,})[:]*(\\d{2,4})?(.*)");

    public WSClientServiceTemplate(BLangPackage bLangPackage, BLangService service) {
        String serviceName = service.name.value;
        this.servicesList = new ArrayList<>();

        //TODO: Removing test files from functions. Remove this filter once test source files are moved to another API
        Names names = Names.getInstance(new CompilerContext());
        PackageID testPkgID = new PackageID(names.fromString("ballerina"),
                                            names.fromString("test"),
                                            Names.EMPTY);
        List<String> nonTestSourceFiles = bLangPackage.compUnits.stream()
                .filter(unit -> unit.getTopLevelNodes().stream().anyMatch(node -> node instanceof BLangImportPackage
                        && ((BLangImportPackage) node).symbol != null
                        && !(((BLangImportPackage) node).symbol.pkgID.equals(testPkgID))))
                .map(BLangCompilationUnit::getName).collect(Collectors.toList());

        // Iterate functions to find urls of the endpoints which has callbackService as current WSClientService
        bLangPackage.functions.stream().filter(
                func -> nonTestSourceFiles.stream().anyMatch(unit -> unit.equals(func.pos.src.cUnitName))
        ).forEach(func -> func.endpoints.stream()
                .filter(ep -> "WebSocketClient".equals(ep.endpointTypeNode.typeName.value) &&
                        serviceName.equals(getRecordValueAsString(WebSocketConstants.CLIENT_SERVICE_CONFIG,
                                                                  (BLangRecordLiteral) ep.configurationExpr)))
                .forEach(ep -> {
                             Object value = getRecordValue(WebSocketConstants.CLIENT_URL_CONFIG,
                                                           (BLangRecordLiteral) ep.configurationExpr);
                             String testFunctionName = "test" + upperCaseFirstLetter(ep.name.value);
                             String mockServiceName = "mock" + upperCaseFirstLetter(ep.name.value) + "Service";
                             String url = (value instanceof BLangLiteral) ? "\"" + value + "\"" : value.toString();
                             servicesList.add(new String[]{testFunctionName, mockServiceName, url});
                         }
                )
        );
    }

    /**
     * Renders content into this file template.
     *
     * @param rootFileTemplate root {@link FileTemplate}
     * @throws TestGeneratorException when template population process fails
     */
    @Override
    public void render(FileTemplate rootFileTemplate) throws TestGeneratorException {
        StringBuilder content = new StringBuilder();
        Iterator<String[]> servicesIterator = servicesList.iterator();
        while (servicesIterator.hasNext()) {
            String[] service = servicesIterator.next();
            String funcName = service[0];
            String mckServiceName = service[1];
            String uri = service[2];

            String filename = (uri.startsWith("wss:")) ? "wssClientService.bal" : "wsClientService.bal";

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
            FileTemplate template = new FileTemplate(filename);
            template.put("testServiceFunctionName", funcName);
            template.put("mockServiceName", mckServiceName);
            template.put("serviceUriStrName", uri);
            template.put("mockServicePath", servicePath);
            template.put("mockServicePort", servicePort);
            template.put("request", "hey");
            template.put("mockResponse", "hey");
            String renderedContent = template.getRenderedContent();
            if (!servicesIterator.hasNext()) {
                // If last, trim right side
                renderedContent = StringUtils.stripEnd(renderedContent, null);
            }
            content.append(renderedContent);
        }
        //Append to root template
        rootFileTemplate.append(RootTemplate.PLACEHOLDER_ATTR_CONTENT, content.toString());
    }
}
