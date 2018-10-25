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
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.parser.converter.SwaggerConverter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.ballerina.swagger.convertor.Constants;
import org.ballerinalang.ballerina.swagger.convertor.SwaggerConverterException;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSCompilerException;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Swagger related utility classes.
 */

public class SwaggerConverterUtils {
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
        try {
            //Create empty swagger object.
            BallerinaFile ballerinaFile = LSCompiler.compileContent(ballerinaSource, CompilerPhase.DEFINE);
            BLangCompilationUnit topCompilationUnit = ballerinaFile.getBLangPackage()
                    .map(bLangPackage -> bLangPackage.getCompilationUnits().get(0))
                    .orElse(null);

            if (topCompilationUnit == null) {
                return "Error";
            }
            String httpAlias = getAlias(topCompilationUnit, Constants.BALLERINA_HTTP_PACKAGE_NAME);
            String swaggerAlias = getAlias(topCompilationUnit, Constants.SWAGGER_PACKAGE_NAME);
            SwaggerServiceMapper swaggerServiceMapper = new SwaggerServiceMapper(httpAlias, swaggerAlias);
            List<EndpointNode> endpoints = new ArrayList<>();

            Swagger swagger = getSwagger(new Swagger(), swaggerServiceMapper, serviceName, topCompilationUnit,
                                         endpoints);
            return swaggerServiceMapper.generateSwaggerString(swagger);
        } catch (LSCompilerException e) {
            return "Error";
        }
    }

    /**
     * This method will generate open API 3.X specification for given ballerina service. Since we will need to
     * support both swagger 2.0 and OAS 3.0 it was implemented to convert to swagger by default and convert it
     * to OAS on demand.
     *
     * @param ballerinaSource ballerina source to be converted to swagger/OAS definition
     * @param serviceName specific service name within ballerina source that need to map OAS
     * @return Generated OAS3 string output.
     * @throws SwaggerConverterException when error occurs while converting, parsing generated swagger source.
     */
    public static String generateOAS3Definitions(String ballerinaSource, String serviceName)
            throws SwaggerConverterException {
        try {
        BallerinaFile ballerinaFile = LSCompiler.compileContent(ballerinaSource, CompilerPhase.DEFINE);
        BLangCompilationUnit topCompilationUnit = ballerinaFile.getBLangPackage()
                .map(bLangPackage -> bLangPackage.getCompilationUnits().get(0))
                .orElse(null);

        if (topCompilationUnit == null) {
            return "Error";
        }
        String httpAlias = getAlias(topCompilationUnit, Constants.BALLERINA_HTTP_PACKAGE_NAME);
        String swaggerAlias = getAlias(topCompilationUnit, Constants.SWAGGER_PACKAGE_NAME);
        SwaggerServiceMapper swaggerServiceMapper = new SwaggerServiceMapper(httpAlias, swaggerAlias);
        List<EndpointNode> endpoints = new ArrayList<>();
            Swagger swagger = getSwagger(new Swagger(), swaggerServiceMapper, serviceName, topCompilationUnit,
                                         endpoints);
            String swaggerSource = swaggerServiceMapper.generateSwaggerString(swagger);
            SwaggerConverter converter = new SwaggerConverter();
            SwaggerDeserializationResult result = new SwaggerParser().readWithInfo(swaggerSource);

            if (result.getMessages().size() > 0) {
                throw new SwaggerConverterException("Please check if input source is valid and complete");
            }

            return Yaml.pretty(converter.convert(result).getOpenAPI());
        } catch (LSCompilerException e) {
            return "Error";
        }
    }

    private static Swagger getSwagger(Swagger swagger, SwaggerServiceMapper swaggerServiceMapper, String serviceName,
                                      BLangCompilationUnit topCompilationUnit, List<EndpointNode> endpoints) {
        for (TopLevelNode topLevelNode : topCompilationUnit.getTopLevelNodes()) {
            if (topLevelNode instanceof BLangEndpoint) {
                endpoints.add((EndpointNode) topLevelNode);
            }

            if (topLevelNode instanceof BLangService) {
                ServiceNode serviceDefinition = (ServiceNode) topLevelNode;
                swagger = new SwaggerEndpointMapper()
                        .convertBoundEndpointsToSwagger(endpoints, serviceDefinition, swagger);

                // Generate swagger string for the mentioned service name.
                if (StringUtils.isNotBlank(serviceName)) {
                    if (serviceDefinition.getName().getValue().equals(serviceName)) {
                        swagger = swaggerServiceMapper.convertServiceToSwagger(serviceDefinition, swagger);
                        break;
                    }
                } else {
                    // If no service name mentioned, then generate swagger definition for the first service.
                    swagger = swaggerServiceMapper.convertServiceToSwagger(serviceDefinition, swagger);
                    break;
                }
            }
        }
        return swagger;
    }

    /**
     * This method will read the contents of ballerina service in {@code servicePath} and write output to
     * {@code outPath} in OAS3 format.
     * @see #generateOAS3Definitions(String, String)
     *
     * @param servicePath path to ballerina service
     * @param outPath output path to write generated swagger file
     * @param serviceName if bal file contain multiple services, name of a specific service to build
     * @throws IOException when file operations fail
     * @throws SwaggerConverterException when converting swagger definition fails
     */
    public static void generateOAS3Definitions(Path servicePath, Path outPath, String serviceName)
            throws IOException, SwaggerConverterException {
        String balSource = readFromFile(servicePath);
        String swaggerName = getSwaggerFileName(servicePath, serviceName);

        String swaggerSource = generateOAS3Definitions(balSource, serviceName);
        writeFile(outPath.resolve(swaggerName), swaggerSource);
    }

    /**
     * This method will read the contents of ballerina service in {@code servicePath} and write output to
     * {@code outPath} in Swagger (OAS2) format.
     * @see #generateSwaggerDefinitions(String, String)
     *
     * @param servicePath path to ballerina service
     * @param outPath output path to write generated swagger file
     * @param serviceName if bal file contain multiple services, name of a specific service to build
     * @throws IOException when file operations fail
     */
    public static void generateSwaggerDefinitions(Path servicePath, Path outPath, String serviceName)
            throws IOException {
        String balSource = readFromFile(servicePath);
        String swaggerName = getSwaggerFileName(servicePath, serviceName);

        String swaggerSource = generateSwaggerDefinitions(balSource, serviceName);
        writeFile(outPath.resolve(swaggerName), swaggerSource);
    }

    private static String readFromFile(Path servicePath) throws IOException {
        String source = FileUtils.readFileToString(servicePath.toFile(), "UTF-8");
        return source;
    }
    
    /**
     * Write content to a file.
     * @param path Path of the file.
     * @param content The content.
     * @throws IOException Error when creating or writing the file.
     */
    private static void writeFile(Path path, String content) throws IOException {
        Path parentPath = path.getParent();
        if (null != parentPath && Files.exists(parentPath)) {
            Files.createDirectories(parentPath);
        }
        Files.deleteIfExists(path);
        Files.createFile(path);
        try (PrintWriter writer = new PrintWriter(path.toString(), "UTF-8")) {
            writer.print(content);
        }
    }

    private static String getSwaggerFileName(Path servicePath, String serviceName) {
        Path file = servicePath.getFileName();
        String swaggerFile;

        if (StringUtils.isNotBlank(serviceName)) {
            swaggerFile = serviceName + ConverterConstants.SWAGGER_SUFFIX;
        } else {
            swaggerFile = file != null ?
                    FilenameUtils.removeExtension(file.toString()) + ConverterConstants.SWAGGER_SUFFIX :
                    null;
        }

        return swaggerFile + ConverterConstants.YAML_EXTENSION;
    }

    /**
     * Gets the alias for a given module from a bLang file root node.
     * @param topCompilationUnit The root node.
     * @param packageName The module name.
     * @return The alias.
     */
    private static String getAlias(BLangCompilationUnit topCompilationUnit, String packageName) {
        for (TopLevelNode topLevelNode : topCompilationUnit.getTopLevelNodes()) {
            if (topLevelNode instanceof BLangImportPackage) {
                BLangImportPackage importPackage = (BLangImportPackage) topLevelNode;
                String packagePath = importPackage.getPackageName().stream().map(BLangIdentifier::getValue).collect
                        (Collectors.joining("."));
                packagePath = importPackage.getOrgName().toString() + '/' + packagePath;
                if (packageName.equals(packagePath)) {
                    return importPackage.getAlias().getValue();
                }
            }
        }

        return null;
    }
}
