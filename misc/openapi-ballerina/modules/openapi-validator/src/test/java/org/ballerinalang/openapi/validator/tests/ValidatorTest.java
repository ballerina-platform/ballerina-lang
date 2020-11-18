/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.openapi.validator.tests;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.ballerinalang.openapi.validator.OpenApiValidatorUtil;
import org.ballerinalang.openapi.validator.ResourceMethod;
import org.ballerinalang.openapi.validator.ResourcePathSummary;
import org.ballerinalang.openapi.validator.ResourceWithOperationId;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 *  command and function to handle common input and outputs.
 */
public class ValidatorTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();

    /**
     * Return bLangPackage for given bal file.
     * @param fileName      String type Of file name
     * @return BlangPackage
     * @throws UnsupportedEncodingException
     */
    public static BLangPackage getBlangPackage(String fileName) throws UnsupportedEncodingException {
        Path sourceRoot = RES_DIR.resolve("project-based-tests/src");
        String balfile = sourceRoot.resolve(fileName).toString();
        Path balFpath = Paths.get(balfile);
        Path programDir = balFpath.toAbsolutePath().getParent();
        String filename = balFpath.toAbsolutePath().getFileName().toString();
        BLangPackage bLangPackage = OpenApiValidatorUtil.compileFile(programDir, filename);
        return bLangPackage;
    }

    public static Schema getComponet(OpenAPI api, String componentName) {
        return api.getComponents().getSchemas().get(componentName);
    }

    public static BVarSymbol getBVarSymbol(BLangPackage bLangPackage) {
        return bLangPackage.getServices().get(0).resourceFunctions.get(0).requiredParams.get(2).symbol;
    }

    public static Schema getSchema(OpenAPI api, String path) {
        return  api.getPaths().get(path).getGet().getParameters().get(0).getSchema();
    }

    // Get the service node from bLangPackage
    public static BLangService getServiceNode(BLangPackage bLangPackage) {
        return bLangPackage.getServices().get(0);
    }

    // Get the Function Node
    public static ResourceMethod getFunction(BLangService bLangService, String method) {

        List<ResourcePathSummary> resourcePathSummaryList =
                ResourceWithOperationId.summarizeResources(bLangService);
        ResourceMethod resourceMethod = resourcePathSummaryList.get(0).getMethods().get(method);
        return resourceMethod;
    }

//    Get diagnostic log
    public static DiagnosticLog getDiagnostic(String fileName) {
        Path sourceRoot = RES_DIR.resolve("project-based-tests/src");
        String balfile = sourceRoot.resolve(fileName).toString();
        Path balFpath = Paths.get(balfile);
        Path programDir = balFpath.toAbsolutePath().getParent();
        CompilerContext context =  OpenApiValidatorUtil.getCompilerContext(programDir);
        return BLangDiagnosticLog.getInstance(context);
    }
}
