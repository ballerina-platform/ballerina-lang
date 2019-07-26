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

package org.ballerinalang.ballerina.openapi.convertor.service;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.converter.SwaggerConverter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.ballerina.openapi.convertor.Constants;
import org.ballerinalang.ballerina.openapi.convertor.OpenApiConverterException;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSCompilerException;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.tool.LauncherUtils;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;

/**
 * OpenApi related utility classes.
 */

public class OpenApiConverterUtils {

    private static final PrintStream outStream = System.err;

    /**
     * This method will generate ballerina string from openapi definition. Since ballerina service definition is super
     * set of openapi definition we will take both openapi and ballerina definition and merge openapi changes to
     * ballerina definition selectively to prevent data loss
     *
     * @param ballerinaSource ballerina definition to be process as ballerina definition
     * @param serviceName     service name
     * @return String representation of converted ballerina source
     * @throws IOException when error occur while processing input openapi and ballerina definitions.
     */
    public static String generateOpenApiDefinitions(String ballerinaSource, String serviceName) throws IOException {
        try {
            //Create empty openapi object.
            BallerinaFile ballerinaFile = LSCompiler.compileContent(ballerinaSource, CompilerPhase.DEFINE);
            BLangCompilationUnit topCompilationUnit = ballerinaFile.getBLangPackage()
                    .map(bLangPackage -> bLangPackage.getCompilationUnits().get(0))
                    .orElse(null);

            if (topCompilationUnit == null) {
                return "Error";
            }
            String httpAlias = getAlias(topCompilationUnit, Constants.BALLERINA_HTTP_PACKAGE_NAME);
            String openApiAlias = getAlias(topCompilationUnit, Constants.OPENAPI_PACKAGE_NAME);
            OpenApiServiceMapper openApiServiceMapper = new OpenApiServiceMapper(httpAlias, openApiAlias);
            List<BLangSimpleVariable> endpoints = new ArrayList<>();

            Swagger openapi = getOpenApiDefinition(new Swagger(), openApiServiceMapper, serviceName, topCompilationUnit,
                    endpoints);
            return openApiServiceMapper.generateOpenApiString(openapi);
        } catch (LSCompilerException e) {
            return "Error";
        }
    }

    /**
     * This method will generate open API 3.X specification for given ballerina service. Since we will need to
     * support both OAS 2.0 and OAS 3.0 it was implemented to convert to openapi by default and convert it
     * to OAS on demand.
     *
     * @param ballerinaSource ballerina source to be converted to OpenApi definition
     * @param serviceName     specific service name within ballerina source that need to map OAS
     * @return Generated OAS3 string output.
     * @throws OpenApiConverterException when error occurs while converting, parsing generated openapi source.
     */
    public static String generateOAS3Definitions(String ballerinaSource, String serviceName)
            throws OpenApiConverterException {
        try {
            BallerinaFile ballerinaFile = LSCompiler.compileContent(ballerinaSource, CompilerPhase.DEFINE);
            BLangCompilationUnit topCompilationUnit = ballerinaFile.getBLangPackage()
                    .map(bLangPackage -> bLangPackage.getCompilationUnits().get(0))
                    .orElse(null);

            if (topCompilationUnit == null) {
                return "Error";
            }

            final TopLevelNode serviceNode = topCompilationUnit.getTopLevelNodes().stream().filter(topLevelNode -> {
                return topLevelNode instanceof BLangService;
            }).findAny().orElse(null);

            if (serviceNode == null) {
                return "Error";
            }

            final List<BLangAnnotationAttachment> annotationAttachments =
                    ((BLangService) serviceNode).getAnnotationAttachments();
            final Iterator<BLangAnnotationAttachment> annoIterator = annotationAttachments.iterator();
            String openApiContractPath = "";

            //If no annotations are defined, assume it's not generated by any command and proceed with
            //just compile to get OpenApi JSON
            if (annoIterator.hasNext()) {
                while (annoIterator.hasNext()) {
                    BLangAnnotationAttachment annotation = annoIterator.next();

                    if (annotation.getAnnotationName().getValue().equals("ServiceInfo")) {
                        BLangRecordLiteral expression = (BLangRecordLiteral) annotation.getExpression();
                        Iterator<BLangRecordLiteral.BLangRecordKeyValue> keyValueIterator = expression
                                .getKeyValuePairs().iterator();
                        while (keyValueIterator.hasNext()) {
                            BLangRecordLiteral.BLangRecordKeyValue keyValuePair = keyValueIterator.next();
                            BLangExpression key = keyValuePair.getKey();
                            if (key instanceof BLangSimpleVarRef) {
                                BLangSimpleVarRef varRef = (BLangSimpleVarRef) key;
                                if ("contract".equals(varRef.variableName.value)) {
                                    openApiContractPath = ((BLangLiteral) keyValuePair.getValue()).value.toString();
                                }
                            }
                        }
                    }
                }

                OpenAPI api = new OpenAPIV3Parser().read(openApiContractPath);

                if (api == null) {
                    throw new OpenApiConverterException("Please check if input source is valid and complete");
                }
                return Yaml.pretty(api);
            } else {
                String httpAlias = getAlias(topCompilationUnit, Constants.BALLERINA_HTTP_PACKAGE_NAME);
                String openApiAlias = getAlias(topCompilationUnit, Constants.OPENAPI_PACKAGE_NAME);
                OpenApiServiceMapper openApiServiceMapper = new OpenApiServiceMapper(httpAlias, openApiAlias);
                List<BLangSimpleVariable> endpoints = new ArrayList<>();
                Swagger openapi = getOpenApiDefinition(new Swagger(), openApiServiceMapper, serviceName,
                        topCompilationUnit, endpoints);
                String openApiSource = openApiServiceMapper.generateOpenApiString(openapi);
                SwaggerConverter converter = new SwaggerConverter();
                SwaggerDeserializationResult result = new SwaggerParser().readWithInfo(openApiSource);

                if (result.getMessages().size() > 0) {
                    throw new OpenApiConverterException("Please check the mentioned service is available " +
                            "in the ballerina source, or thee content is valid");
                }

                return Yaml.pretty(converter.convert(result).getOpenAPI());
            }

        } catch (LSCompilerException e) {
            return "Error";
        }
    }

    private static Swagger getOpenApiDefinition(Swagger openapi, OpenApiServiceMapper openApiServiceMapper,
                                                String serviceName, BLangCompilationUnit topCompilationUnit,
                                                List<BLangSimpleVariable> endpoints) {
        for (TopLevelNode topLevelNode : topCompilationUnit.getTopLevelNodes()) {
            if (topLevelNode instanceof BLangSimpleVariable
                    && ((BLangSimpleVariable) topLevelNode).getFlags().contains(Flag.LISTENER)) {
                endpoints.add((BLangSimpleVariable) topLevelNode);
            }

            if (topLevelNode instanceof BLangService) {
                BLangService serviceDefinition = (BLangService) topLevelNode;
                openapi = new OpenApiEndpointMapper()
                        .convertBoundEndpointsToOpenApi(endpoints, serviceDefinition, openapi);

                // Generate openApi string for the mentioned service name.
                if (StringUtils.isNotBlank(serviceName)) {
                    if (serviceDefinition.getName().getValue().equals(serviceName)) {
                        openapi = openApiServiceMapper.convertServiceToOpenApi(serviceDefinition, openapi);
                        break;
                    }
                } else {
                    // If no service name mentioned, then generate openApi definition for the first service.
                    openapi = openApiServiceMapper.convertServiceToOpenApi(serviceDefinition, openapi);
                    break;
                }
            }
        }
        return openapi;
    }

    /**
     * This method will read the contents of ballerina service in {@code servicePath} and write output to
     * {@code outPath} in OAS3 format.
     *
     * @param servicePath path to ballerina service
     * @param outPath     output path to write generated openapi file
     * @param serviceName if bal file contain multiple services, name of a specific service to build
     * @throws IOException               when file operations fail
     * @throws OpenApiConverterException when converting openapi definition fails
     * @see #generateOAS3Definitions(String, String)
     */
    public static void generateOAS3Definitions(Path servicePath, Path outPath, String serviceName)
            throws IOException, OpenApiConverterException {
        String balSource = readFromFile(servicePath);
        String openApiName = getOpenApiFileName(servicePath, serviceName);

        String openApiSource = generateOAS3Definitions(balSource, serviceName);
        writeFile(outPath.resolve(openApiName), openApiSource);
    }

    /**
     * This method will compile a given ballerina package, find the service name and compile it to generate
     * an OpenApi contract.
     *
     * @param moduleName - Module Name of the service residing in
     * @param serviceName - Service name
     * @param output - Output location
     * @throws OpenApiConverterException - Converter exceptions
     */
    public static void generateOAS3DefinitionFromModule(String moduleName, String serviceName, Path output)
            throws OpenApiConverterException  {
        BLangPackage pkg;
        BLangService service;

        try {
            pkg =  compileModule(Paths.get(System.getProperty("user.dir")), moduleName);
        } catch (Exception e) {
            throw LauncherUtils.createLauncherException(e.getLocalizedMessage());
        }

        if (pkg == null) {
            throw LauncherUtils.createLauncherException("The provided package is not valid.");
        }

        service = pkg.services.stream()
                    .filter(bLangService -> serviceName.equals(bLangService.getName().getValue()))
                    .findAny()
                    .orElse(null);

        if (service == null) {
            throw LauncherUtils.createLauncherException("Couldn't find " + serviceName + " service in the " +
                    "provided module. Please check that there is a service in the module.");
        }


        BLangCompilationUnit topCompilationUnit = pkg.getCompilationUnits().stream()
                .filter(bLangCompilationUnit -> bLangCompilationUnit.getName().equals(service.pos.src.cUnitName))
                .findAny()
                .orElse(null);

        if (topCompilationUnit == null) {
            throw new OpenApiConverterException("Please check if input source is valid and complete.");
        }


        String httpAlias = getAlias(topCompilationUnit, Constants.BALLERINA_HTTP_PACKAGE_NAME);
        String openApiAlias = getAlias(topCompilationUnit, Constants.OPENAPI_PACKAGE_NAME);
        OpenApiServiceMapper openApiServiceMapper = new OpenApiServiceMapper(httpAlias, openApiAlias);
        List<BLangSimpleVariable> endpoints = new ArrayList<>();
        Swagger openapi = getOpenApiDefinition(new Swagger(), openApiServiceMapper, serviceName, topCompilationUnit,
                endpoints);
        String openApiSource = openApiServiceMapper.generateOpenApiString(openapi);
        SwaggerConverter converter = new SwaggerConverter();
        SwaggerDeserializationResult result = new SwaggerParser().readWithInfo(openApiSource);

        if (result.getMessages().size() > 0) {
            throw new OpenApiConverterException("Please check if input source is valid and complete");
        }

        if (checkOASFileExists(serviceName + ConverterConstants.YAML_EXTENSION, output)) {
            throw LauncherUtils.createLauncherException("The output location already contains " +
                    "an OpenApi Contract named " + serviceName + ConverterConstants.YAML_EXTENSION);
        }

        String openApiResource =  Yaml.pretty(converter.convert(result).getOpenAPI());

        try {
            writeFile(output.resolve(serviceName + ConverterConstants.YAML_EXTENSION), openApiResource);

            outStream.println("The OpenApi Contract was successfully generated at " + output.toRealPath());
        } catch (IOException e) {
            throw LauncherUtils.createLauncherException(e.getLocalizedMessage());
        }
    }

    /**
     * This method will read the contents of ballerina service in {@code servicePath} and write output to
     * {@code outPath} in OpenApi format.
     *
     * @param servicePath path to ballerina service
     * @param outPath     output path to write generated openapi file
     * @param serviceName if bal file contain multiple services, name of a specific service to build
     * @throws IOException when file operations fail
     * @see #generateOpenApiDefinitions(String, String)
     */
    public static void generateOpenApiDefinitions(Path servicePath, Path outPath, String serviceName)
            throws IOException {
        String balSource = readFromFile(servicePath);
        String openApiName = getOpenApiFileName(servicePath, serviceName);

        String openApiSource = generateOpenApiDefinitions(balSource, serviceName);
        writeFile(outPath.resolve(openApiName), openApiSource);
    }

    private static boolean checkOASFileExists(String fileName, Path exportLocation) {
        return Files.exists(exportLocation.resolve(fileName));
    }

    private static String readFromFile(Path servicePath) throws IOException {
        String source = FileUtils.readFileToString(servicePath.toFile(), "UTF-8");
        return source;
    }

    /**
     * Write content to a file.
     *
     * @param path    Path of the file.
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

    private static String getOpenApiFileName(Path servicePath, String serviceName) {
        Path file = servicePath.getFileName();
        String openApiFile;

        if (StringUtils.isNotBlank(serviceName)) {
            openApiFile = serviceName + ConverterConstants.OPENAPI_SUFFIX;
        } else {
            openApiFile = file != null ?
                    FilenameUtils.removeExtension(file.toString()) + ConverterConstants.OPENAPI_SUFFIX :
                    null;
        }

        return openApiFile + ConverterConstants.YAML_EXTENSION;
    }

    /**
     * Gets the alias for a given module from a bLang file root node.
     *
     * @param topCompilationUnit The root node.
     * @param packageName        The module name.
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

    /**
     * Compile only a ballerina module.
     *
     * @param sourceRoot source root
     * @param moduleName name of the module to be compiled
     * @return {@link BLangPackage} ballerina package
     */
    private static BLangPackage compileModule(Path sourceRoot, String moduleName) {
        CompilerContext context = getCompilerContext(sourceRoot);
        Compiler compiler = Compiler.getInstance(context);
        return compiler.compile(moduleName);
    }

    /**
     * Get prepared compiler context.
     *
     * @param sourceRootPath ballerina compilable source root path
     * @return {@link CompilerContext} compiler context
     */
    private static CompilerContext getCompilerContext(Path sourceRootPath) {
        CompilerPhase compilerPhase = CompilerPhase.DEFINE;
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRootPath.toString());
        options.put(OFFLINE, Boolean.toString(false));
        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(SKIP_TESTS, Boolean.toString(false));
        options.put(TEST_ENABLED, "true");
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(true));
        options.put(PRESERVE_WHITESPACE, Boolean.toString(true));

        return context;
    }

}
