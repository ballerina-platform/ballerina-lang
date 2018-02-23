/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.ballerina.swagger.convertor.service;

import io.swagger.models.Swagger;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.composer.service.ballerina.parser.service.model.BFile;
import org.ballerinalang.composer.service.ballerina.parser.service.model.BallerinaFile;
import org.ballerinalang.composer.service.ballerina.parser.service.model.lang.ModelPackage;
import org.ballerinalang.composer.service.ballerina.parser.service.util.ParserUtils;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Swagger related utility classes.
 */

public class SwaggerConverterUtils {
    
    /**
     * Generate ballerina fine from the String definition.
     *
     * @param bFile ballerina string definition
     * @return ballerina file created from ballerina string definition
     * @throws IOException IO exception
     */
    public static BLangCompilationUnit getTopLevelNodeFromBallerinaFile(BFile bFile) throws IOException {
    
        String filePath = bFile.getFilePath();
        String fileName = bFile.getFileName();
        String content = bFile.getContent();
    
        org.wso2.ballerinalang.compiler.tree.BLangPackage model;
    
        // Sometimes we are getting Ballerina content without a file in the file-system.
        if (!Files.exists(Paths.get(filePath, fileName))) {
            BallerinaFile ballerinaFile = ParserUtils.getBallerinaFileForContent(fileName, content,
                    CompilerPhase.CODE_ANALYZE);
            model = ballerinaFile.getBLangPackage();
        
        } else {
            BallerinaFile ballerinaFile = ParserUtils.getBallerinaFile(filePath, fileName);
            model = ballerinaFile.getBLangPackage();
        }
    
        final Map<String, ModelPackage> modelPackage = new HashMap<>();
        ParserUtils.loadPackageMap("Current Package", model, modelPackage);
    
        Optional<BLangCompilationUnit> compilationUnit = model.getCompilationUnits().stream()
                .filter(compUnit -> fileName.equals(compUnit.getName()))
                .findFirst();
        return compilationUnit.orElse(null);
    }
    
    /**
     * This method will generate ballerina string from swagger definition. Since ballerina service definition is super
     * set of swagger definition we will take both swagger and ballerina definition and merge swagger changes to
     * ballerina definition selectively to prevent data loss
     *
     * @param ballerinaSource ballerina definition to be process as ballerina definition
     * @param serviceName service name
     * @return String representation of converted ballerina source
     * @throws IOException when error occur while processing input swagger and ballerina definitions.
     */
    public static String generateSwaggerDefinitions(String ballerinaSource, String serviceName) throws IOException {
        // Get the ballerina model using the ballerina source code.
        BFile balFile = new BFile();
        balFile.setContent(ballerinaSource);
        BLangCompilationUnit topCompilationUnit = SwaggerConverterUtils.getTopLevelNodeFromBallerinaFile(balFile);
        String httpAlias = getAlias(topCompilationUnit, "ballerina.net.http");
        String swaggerAlias = getAlias(topCompilationUnit, "ballerina.net.http.swagger");
        SwaggerServiceMapper swaggerServiceMapper = new SwaggerServiceMapper(httpAlias, swaggerAlias);
        String swaggerSource = StringUtils.EMPTY;
        for (TopLevelNode topLevelNode : topCompilationUnit.getTopLevelNodes()) {
            if (topLevelNode instanceof BLangService) {
                ServiceNode serviceDefinition = (ServiceNode) topLevelNode;
                // Generate swagger string for the mentioned service name.
                if (StringUtils.isNotBlank(serviceName)) {
                    if (serviceDefinition.getName().getValue().equals(serviceName)) {
                        Swagger swaggerDefinition = swaggerServiceMapper.convertServiceToSwagger(serviceDefinition);
                        swaggerSource = swaggerServiceMapper.generateSwaggerString(swaggerDefinition);
                        break;
                    }
                } else {
                    // If no service name mentioned, then generate swagger definition for the first service.
                    Swagger swaggerDefinition = swaggerServiceMapper.convertServiceToSwagger(serviceDefinition);
                    swaggerSource = swaggerServiceMapper.generateSwaggerString(swaggerDefinition);
                    break;
                }
            }
        }
    
        return swaggerSource;
    }
    
    /**
     * Gets the alias for a given package from a bLang file root node.
     * @param topCompilationUnit The root node.
     * @param packageName The package name.
     * @return The alias.
     */
    private static String getAlias(BLangCompilationUnit topCompilationUnit, String packageName) {
        for (TopLevelNode topLevelNode : topCompilationUnit.getTopLevelNodes()) {
            if (topLevelNode instanceof BLangImportPackage) {
                BLangImportPackage importPackage = (BLangImportPackage) topLevelNode;
                String packagePath = importPackage.getPackageName().stream().map(BLangIdentifier::getValue).collect
                        (Collectors.joining("."));
                if (packageName.equals(packagePath)) {
                    return importPackage.getAlias().getValue();
                }
            }
        }
        
        return null;
    }
}
