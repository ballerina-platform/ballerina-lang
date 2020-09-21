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

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Swagger;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.ByteArrayProperty;
import io.swagger.models.properties.DecimalProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
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
import org.ballerinalang.langserver.compiler.ExtendedLSCompiler;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.ballerinalang.tool.LauncherUtils;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
            BallerinaFile ballerinaFile = ExtendedLSCompiler.compileContent(ballerinaSource, CompilerPhase.DEFINE);
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
        } catch (CompilationFailedException e) {
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
            BallerinaFile ballerinaFile = ExtendedLSCompiler.compileContent(ballerinaSource, CompilerPhase.DEFINE);
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

            final BLangAnnotationAttachment isServiceAnnotationAvailable = annotationAttachments.stream()
                    .filter(bLangAnnotationAttachment -> {
                        return bLangAnnotationAttachment.getAnnotationName().getValue().equals("ServiceInfo");
            }).findAny().orElse(null);

            //If no annotations are defined, assume it's not generated by any command and proceed with
            //just compile to get OpenApi JSON
            if (annoIterator.hasNext() && isServiceAnnotationAvailable != null) {
                while (annoIterator.hasNext()) {
                    BLangAnnotationAttachment annotation = annoIterator.next();

                    if (annotation.getAnnotationName().getValue().equals("ServiceInfo")) {
                        BLangRecordLiteral expression = (BLangRecordLiteral) annotation.getExpression();
                        Iterator<RecordLiteralNode.RecordField> keyValueIterator = expression.getFields().iterator();
                        while (keyValueIterator.hasNext()) {
                            BLangRecordLiteral.BLangRecordKeyValueField keyValuePair =
                                    (BLangRecordLiteral.BLangRecordKeyValueField) keyValueIterator.next();
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

        } catch (CompilationFailedException e) {
            return "Error";
        }
    }

    private static Swagger getOpenApiDefinition(Swagger openapi, OpenApiServiceMapper openApiServiceMapper,
                                                String serviceName, BLangCompilationUnit topCompilationUnit,
                                                List<BLangSimpleVariable> endpoints) {
        Map<String, Model> definitions = new HashMap<>();

        for (TopLevelNode topLevelNode : topCompilationUnit.getTopLevelNodes()) {
            if (topLevelNode instanceof BLangSimpleVariable
                    && ((BLangSimpleVariable) topLevelNode).getFlags().contains(Flag.LISTENER)) {
                endpoints.add((BLangSimpleVariable) topLevelNode);
            }

            if (topLevelNode instanceof BLangService && openapi.getBasePath() == null) {
                BLangService serviceDefinition = (BLangService) topLevelNode;
                openapi = new OpenApiEndpointMapper()
                        .convertBoundEndpointsToOpenApi(endpoints, serviceDefinition, openapi);

                // Generate openApi string for the mentioned service name.
                if (StringUtils.isNotBlank(serviceName)) {
                    if (serviceDefinition.getName().getValue().equals(serviceName)) {
                        openapi = openApiServiceMapper.convertServiceToOpenApi(serviceDefinition, openapi);
                    }
                } else {
                    // If no service name mentioned, then generate openApi definition for the first service.
                    openapi = openApiServiceMapper.convertServiceToOpenApi(serviceDefinition, openapi);
                }
            }

            //Map records into swagger definitions
            if (topLevelNode instanceof BLangTypeDefinition) {
                BLangTypeDefinition typeNode = (BLangTypeDefinition) topLevelNode;

                if (typeNode.typeNode instanceof BLangRecordTypeNode) {
                    Model model = new ModelImpl();

                    List<? extends SimpleVariableNode> fields = ((BLangRecordTypeNode) typeNode.typeNode).getFields();

                    Map<String, Property> propertyMap = new HashMap<>();
                    for (SimpleVariableNode field :fields) {

                        final Property openApiPropertyForBallerinaField =
                                createOpenApiPropertyForBallerinaField(field.getTypeNode());

                        if (openApiPropertyForBallerinaField != null) {
                            propertyMap.put(field.getName().getValue(), openApiPropertyForBallerinaField);
                        }
                    }

                    model.setProperties(propertyMap);
                    definitions.put(typeNode.getName().getValue(), model);
                    openapi.setDefinitions(definitions);
                }
            }
        }

        return openapi;
    }

    public static Property createOpenApiPropertyForBallerinaField(TypeNode node) {
        Property property = null;
        if (node instanceof BLangArrayType) {
            final BLangArrayType fieldTypeNode = (BLangArrayType) node;
            ArrayProperty arr = new ArrayProperty();
            arr.setItems(mapBallerinaTypes(fieldTypeNode.getElementType()
                    .type.getKind().typeName(), true));
            property = arr;
        } else if (node instanceof BLangBuiltInRefTypeNode) {
            final BLangBuiltInRefTypeNode fieldTypeNode = (BLangBuiltInRefTypeNode) node;
            property = mapBallerinaTypes(fieldTypeNode.typeKind.typeName(), false);
        } else if (node instanceof BLangConstrainedType) {
            //TODO handle constrained types
        } else if (node instanceof BLangErrorType) {
            //TODO Error type is handled as string variables. Need to discuss
            final BLangErrorType fieldTypeNode = (BLangErrorType) node;
            final BType bErrorType = fieldTypeNode.type;
            if (bErrorType instanceof BErrorType) {
                assert false;
//                property = mapBallerinaTypes(((BErrorType) bErrorType)
//                        .getReasonType().getKind().typeName(), false);
            }
        } else if (node instanceof BLangFiniteTypeNode) {
            //TODO handle finite types
        } else if (node instanceof BLangFunctionTypeNode) {
            //TODO handle function types
        } else if (node instanceof BLangObjectTypeNode) {
            //TODO handle object types
        } else if (node instanceof BLangRecordTypeNode) {
            //TODO handle record types
        } else if (node instanceof BLangStructureTypeNode) {
            //TODO handle structure types
        } else if (node instanceof BLangTupleTypeNode) {
            //TODO handle tuple types
        } else if (node instanceof BLangUnionTypeNode) {
            //TODO handle union types
        } else if (node instanceof BLangUserDefinedType) {
            final BLangUserDefinedType fieldTypeNode = (BLangUserDefinedType) node;
            property = mapBallerinaTypes(fieldTypeNode.getTypeName().value, false);
        } else if (node instanceof BLangValueType) {
            final BLangValueType fieldTypeNode = (BLangValueType) node;
            property = mapBallerinaTypes(fieldTypeNode.getTypeKind().typeName(), false);
        }
        return property;
    }

    public static Property mapBallerinaTypes(String type, boolean isArray) {
        switch (type) {
            case "any":
                //TODO handle any type to OpenApi
                return null;
            case "int":
                return new IntegerProperty();
            case "string":
                return new StringProperty();
            case "boolean":
                return new BooleanProperty();
            case "decimal":
                return new DecimalProperty();
            case "byte":
                return new ByteArrayProperty();
            case "float":
                return new FloatProperty();
            case "json":
                //TODO json is mapped to Object property. Will need to handle it properly.
                return new ObjectProperty();
            default:
                //TODO handle unmatched type
                return null;
        }
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
        openApiName = checkDuplicateFiles(outPath, openApiName);
        writeFile(outPath.resolve(openApiName), openApiSource);
    }

    /**
     * This util for generating files when not available with specific service name.

     * @param servicePath               resource path
     * @param outPath                   target path
     * @throws IOException              exception for throwing when generating file failing
     * @throws CompilationFailedException exception for throwing when compilation failing
     * @throws OpenApiConverterException  exception for throwing when generating file failing
     */
    public static void generateOAS3DefinitionsAllService(Path servicePath, Path outPath)
            throws IOException, CompilationFailedException, OpenApiConverterException {

        String ballerinaSource = readFromFile(servicePath);
        BallerinaFile ballerinaFile = ExtendedLSCompiler.compileContent(ballerinaSource, CompilerPhase.DEFINE);
        BLangCompilationUnit topCompilationUnit = ballerinaFile.getBLangPackage()
                .map(bLangPackage -> bLangPackage.getCompilationUnits().get(0))
                .orElse(null);
        for (TopLevelNode topLevelNode : topCompilationUnit.getTopLevelNodes()) {
            if (topLevelNode instanceof BLangService) {
                BLangService serviceDefinition = (BLangService) topLevelNode;
                // Generate openApi string for the mentioned service name.
                String serviceName = serviceDefinition.getName().getValue();
                String openApiName = getOpenApiFileName(servicePath, serviceName);
                String openApiSource = generateOAS3Definitions(ballerinaSource, serviceName);
                //  Checked old generated file with same name
                openApiName = checkDuplicateFiles(outPath, openApiName);
                writeFile(outPath.resolve(openApiName), openApiSource);
            }
        }
    }

    /**
     * This method use for checking the duplicate files.
     * @param outPath       output path for file generated
     * @param openApiName   given file name
     * @return              file name with duplicate number tag
     */
    private static String checkDuplicateFiles(Path outPath, String openApiName) {

        if (Files.exists(outPath)) {
            final File[] listFiles = new File(String.valueOf(outPath)).listFiles();
            if (listFiles != null) {
                for (File file : listFiles) {
                    if (file.getName().equals(openApiName)) {
                        String userInput = System.console().readLine("There is already a/an " + file.getName() +
                                " in the location. Do you want to override the file [Y/N]? ");
                        if (!Objects.equals(userInput.toLowerCase(Locale.ENGLISH), "y")) {
                            int duplicateCount = 0;
                            openApiName = setGeneratedFileName(listFiles, openApiName, duplicateCount);
                        }
                    }
                }
            }
        }
        return openApiName;
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
            throw new OpenApiConverterException(e.getLocalizedMessage());
        }

        if (pkg == null) {
            throw new OpenApiConverterException("The provided Ballerina module is not valid. Please provide a " +
                    "valid module.");
        }

        service = pkg.services.stream()
                    .filter(bLangService -> serviceName.equals(bLangService.getName().getValue()))
                    .findAny()
                    .orElse(null);

        if (service == null) {
            throw new OpenApiConverterException("Couldn't find " + serviceName + " service in the " +
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
            throw new OpenApiConverterException("The output location already contains " +
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
            outStream.println("Successfully generated the ballerina contract at location \n" + path.toAbsolutePath());
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

        try {
            compiler.setOutStream(new EmptyPrintStream());
            compiler.setErrorStream(new EmptyPrintStream());
        } catch (UnsupportedEncodingException e) {
            // Ignore the exception as not setting OutStream won't break the functionality.
        }

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

    static class EmptyPrintStream extends PrintStream {
        EmptyPrintStream() throws UnsupportedEncodingException {
            super(new OutputStream() {
                @Override
                public void write(int b) {
                }
            }, true, "UTF-8");
        }
    }

    /**
     *  This method for setting the file name for generated file.
     * @param listFiles         generated files
     * @param fileName          File name
     * @param duplicateCount    add the tag with duplicate number if file already exist
     */
    private static String setGeneratedFileName(File[] listFiles, String fileName, int duplicateCount) {

        for (File listFile : listFiles) {
            String listFileName = listFile.getName();
            if (listFileName.contains(".") && ((listFileName.split("\\.")).length >= 2)
                    && (listFileName.split("\\.")[0]
                    .equals(fileName.split("\\.")[0]))) {
                duplicateCount = 1 + duplicateCount;
            }
        }
        return fileName.split("\\.")[0] + "." + (duplicateCount) + ".yaml";
    }
}
