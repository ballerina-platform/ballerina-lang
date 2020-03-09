package org.ballerinalang.langserver.command.executors.openAPI.BallerinToOpenAPI;

import com.google.gson.JsonObject;
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
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.ballerina.openapi.convertor.Constants;
import org.ballerinalang.ballerina.openapi.convertor.service.OpenApiEndpointMapper;
import org.ballerinalang.ballerina.openapi.convertor.service.OpenApiServiceMapper;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.commons.command.LSCommandExecutorException;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.ExtendedLSCompiler;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.wso2.ballerinalang.compiler.SourceDirectoryManager;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.codeaction.providers.openAPI.OpenApiCodeActionUtil.getBLangFunction;
import static org.ballerinalang.langserver.codeaction.providers.openAPI.OpenApiCodeActionUtil.getBLangPkg;

/**
 * Represents the command executor for creating a openAPI service resource in contract file.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor")
public class CreateBallerinaServiceResourceExecutor implements LSCommandExecutor {

    public static final String COMMAND = "CREATE_SERVICE_RESOURCE_IN_OPENAPI";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(LSContext context) throws LSCommandExecutorException {
        String documentUri = null;
        VersionedTextDocumentIdentifier textDocumentIdentifier = new VersionedTextDocumentIdentifier();
        String resourcePath = null;
        int line = -1;
        int column = -1;

        for (Object arg : context.get(ExecuteCommandKeys.COMMAND_ARGUMENTS_KEY)) {
            String argKey = ((JsonObject) arg).get(ARG_KEY).getAsString();
            String argVal = ((JsonObject) arg).get(ARG_VALUE).getAsString();
            switch (argKey) {
                case CommandConstants.ARG_KEY_DOC_URI:
                    documentUri = argVal;
                    textDocumentIdentifier.setUri(documentUri);
                    context.put(DocumentServiceKeys.FILE_URI_KEY, documentUri);
                    break;
                case CommandConstants.ARG_KEY_NODE_LINE:
                    line = Integer.parseInt(argVal);
                    break;
                case CommandConstants.ARG_KEY_NODE_COLUMN:
                    column = Integer.parseInt(argVal);
                    break;
                case CommandConstants.ARG_KEY_PATH:
                    resourcePath = argVal;
                default:
            }
        }

        if (line == -1 || column == -1 || documentUri == null) {
            throw new LSCommandExecutorException("Invalid parameters received for the create function command!");
        }

        WorkspaceDocumentManager documentManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);

        BLangFunction functionNode = null;
        BLangService serviceNode = null;
        try {
            LSDocumentIdentifier lsDocument = documentManager.getLSDocument(
                    CommonUtil.getPathFromURI(documentUri).get());
            Position pos = new Position(line, column + 1);

            List<BLangPackage> pkg = getBLangPkg(context, lsDocument, pos);
            functionNode = getBLangFunction(pkg, pos);

            if (functionNode != null && functionNode.parent != null) {
                BLangObjectTypeNode serviceObj = (BLangObjectTypeNode) functionNode.parent;
                if (serviceObj.flagSet.contains(Flag.SERVICE)) {
                    if (serviceObj.parent != null) {
                        BLangNode typeDef = serviceObj.parent;
                        if (typeDef.parent != null) {
                            BLangPackage bLangPackage = (BLangPackage) typeDef.parent;
                            if (bLangPackage.services != null && bLangPackage.services.size() > 0) {
                                List<BLangService> services = bLangPackage.services;
                                for (BLangService service : services) {
                                    if (service.symbol.type.tsymbol.name == functionNode.symbol.owner.name) {
                                        serviceNode = service;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            String contractURI = null;

            if (serviceNode != null) {
                List<BLangAnnotationAttachment> annotations = serviceNode.annAttachments;
                for (BLangAnnotationAttachment annotation : annotations) {
                    if (annotation.getExpression() instanceof BLangRecordLiteral) {
                        BLangRecordLiteral recordLiteral = (BLangRecordLiteral) annotation.getExpression();
                        for (BLangRecordLiteral.RecordField field : recordLiteral.getFields()) {
                            BLangExpression keyExpr;
                            BLangExpression valueExpr;

                            if (field.isKeyValueField()) {
                                BLangRecordLiteral.BLangRecordKeyValueField keyValue =
                                        (BLangRecordLiteral.BLangRecordKeyValueField) field;
                                keyExpr = keyValue.getKey();
                                valueExpr = keyValue.getValue();
                            } else {
                                BLangRecordLiteral.BLangRecordVarNameField varNameField =
                                        (BLangRecordLiteral.BLangRecordVarNameField) field;
                                keyExpr = varNameField;
                                valueExpr = varNameField;
                            }

                            if (keyExpr instanceof BLangSimpleVarRef) {
                                BLangSimpleVarRef contract = (BLangSimpleVarRef) keyExpr;
                                String key = contract.getVariableName().getValue();
                                if (key.equals("contract")) {
                                    if (valueExpr instanceof BLangLiteral) {
                                        BLangLiteral value = (BLangLiteral) valueExpr;
                                        if (value.getValue() instanceof String) {
                                            contractURI = (String) value.getValue();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (contractURI != null) {
                String separator = File.separator;
                SourceDirectoryManager sourceDirectoryManager = SourceDirectoryManager.getInstance(
                        context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY));
                String sourceDir = sourceDirectoryManager.getSourceDirectory().getPath().toString();
                String filePath = serviceNode.getPosition().getSource().getPackageName() + separator +
                        serviceNode.getPosition().getSource().getCompilationUnitName().replaceAll(
                                "\\w*\\.bal", "");
                String projectDir = filePath.contains(sourceDir) ? sourceDir :
                        (sourceDir + separator + "src" + separator + filePath);

                File file = null;
                if (contractURI.contains(projectDir)) {
                    file = new File(contractURI);
                } else {
                    try {
                        file = new File(Paths.get(projectDir, contractURI).toRealPath().toString());
                    } catch (IOException e) {
                        contractURI = Paths.get(contractURI).toString();
                    }
                }
                if (file != null && file.exists()) {
                    contractURI = file.getAbsolutePath();
                }

                OpenAPI openAPI = parseOpenAPIFile(contractURI);
                Path ballerinaPath = Paths.get(documentManager.getAllFilePaths().toArray()[0].toString());
                String balSource = readFromFile(ballerinaPath);

                String openAPIDefinitions = generateOpenApiDefinitions(balSource, serviceNode.name.value, resourcePath);
                SwaggerConverter converter = new SwaggerConverter();
                SwaggerDeserializationResult result = new SwaggerParser().readWithInfo(openAPIDefinitions);
                String openApiResource = Yaml.pretty(converter.convert(result).getOpenAPI());
                OpenAPI openAPINew = new OpenAPIV3Parser().readContents(openApiResource).getOpenAPI();

                openAPINew.getPaths().forEach((s, path) -> {
                    openAPI.getPaths().addPathItem(s, path);
                });

                String openApiResourceNew = Yaml.pretty(openAPI);
                writeFile(Paths.get(contractURI), openApiResourceNew);

            }
        } catch (CompilationFailedException | IOException | WorkspaceDocumentException e) {
            throw new LSCommandExecutorException("Error while compiling the source!");
        }
        return null;
    }

    /**
     * Parse and get the {@link OpenAPI} for the given OpenAPI contract.
     *
     * @param definitionURI URI for the OpenAPI contract
     * @return {@link OpenAPI} OpenAPI model
     */
    static OpenAPI parseOpenAPIFile(String definitionURI) {
        Path contractPath = Paths.get(definitionURI);
        if (Files.exists(contractPath) && (definitionURI.endsWith(".yaml") || definitionURI.endsWith(".json"))) {
            return new OpenAPIV3Parser().read(definitionURI);
        }
        return null;
    }

    /**
     * This method will read the contents of ballerina service in {@code servicePath} and write output to {@code
     * outPath} in OAS3 format.
     *
     * @param servicePath path to ballerina service
     */
    private static String readFromFile(Path servicePath) throws IOException {
        String source = FileUtils.readFileToString(servicePath.toFile(), "UTF-8");
        return source;
    }

    /**
     * This method will read the contents of ballerina service in and returns in String format.
     *
     * @param ballerinaSource ballerina source code
     * @param resourcePath    output path to write generated openapi file
     * @param serviceName     if bal file contain multiple services, name of a specific service to build
     */
    public static String generateOpenApiDefinitions(String ballerinaSource, String serviceName, String resourcePath) {
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
                                                   endpoints, resourcePath);
            return openApiServiceMapper.generateOpenApiString(openapi);
        } catch (CompilationFailedException e) {
            return "Error";
        }
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

    private static Swagger getOpenApiDefinition(Swagger openapi, OpenApiServiceMapper openApiServiceMapper,
                                                String serviceName, BLangCompilationUnit topCompilationUnit,
                                                List<BLangSimpleVariable> endpoints, String resourcePath) {
        Map<String, Model> definitions = new HashMap<>();

        for (TopLevelNode topLevelNode : topCompilationUnit.getTopLevelNodes()) {
            if (topLevelNode instanceof BLangSimpleVariable
                    && ((BLangSimpleVariable) topLevelNode).getFlags().contains(Flag.LISTENER)) {
                endpoints.add((BLangSimpleVariable) topLevelNode);
            }

            // find the path
            if (topLevelNode instanceof BLangService && openapi.getBasePath() == null) {
                BLangService serviceDefinition = (BLangService) topLevelNode;
                List<BLangFunction> unwantedFunctions = new ArrayList<>();
                for (BLangFunction resource : serviceDefinition.getResources()) {
                    for (RecordLiteralNode.RecordField field : ((BLangRecordLiteral) resource.annAttachments.get(
                            0).expr).fields) {
                        if (((BLangSimpleVarRef) ((BLangRecordLiteral.BLangRecordKeyValueField) field).key.expr).variableName.value
                                .equals("path")) {
                            if (!(((BLangLiteral) ((BLangRecordLiteral.BLangRecordKeyValueField) field).valueExpr).value
                                    .equals(resourcePath))) {
                                unwantedFunctions.add(resource);
                            }
                        }
                    }
                }
                serviceDefinition.getResources().removeAll(unwantedFunctions);
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
                    for (SimpleVariableNode field : fields) {

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
                property = mapBallerinaTypes(((BErrorType) bErrorType)
                                                     .getReasonType().getKind().typeName(), false);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }

}
