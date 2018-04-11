/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.composer.service.ballerina.parser.service.util;

import com.google.common.io.Files;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.composer.service.ballerina.parser.service.model.BuiltInType;
import org.ballerinalang.composer.service.ballerina.parser.service.model.SymbolInformation;
import org.ballerinalang.composer.service.ballerina.parser.service.model.lang.Action;
import org.ballerinalang.composer.service.ballerina.parser.service.model.lang.AnnotationAttachment;
import org.ballerinalang.composer.service.ballerina.parser.service.model.lang.AnnotationDef;
import org.ballerinalang.composer.service.ballerina.parser.service.model.lang.Connector;
import org.ballerinalang.composer.service.ballerina.parser.service.model.lang.Endpoint;
import org.ballerinalang.composer.service.ballerina.parser.service.model.lang.Enum;
import org.ballerinalang.composer.service.ballerina.parser.service.model.lang.Enumerator;
import org.ballerinalang.composer.service.ballerina.parser.service.model.lang.Function;
import org.ballerinalang.composer.service.ballerina.parser.service.model.lang.ModelPackage;
import org.ballerinalang.composer.service.ballerina.parser.service.model.lang.ObjectField;
import org.ballerinalang.composer.service.ballerina.parser.service.model.lang.ObjectModel;
import org.ballerinalang.composer.service.ballerina.parser.service.model.lang.Parameter;
import org.ballerinalang.composer.service.ballerina.parser.service.model.lang.RecordModel;
import org.ballerinalang.composer.service.ballerina.parser.service.model.lang.Struct;
import org.ballerinalang.composer.service.ballerina.parser.service.model.lang.StructField;
import org.ballerinalang.langserver.LSPackageLoader;
import org.ballerinalang.langserver.common.modal.BallerinaFile;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.workspace.WorkspaceDocumentManagerImpl;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.EnumNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.ballerinalang.model.tree.types.UserDefinedTypeNode;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.PackageLoader;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.desugar.Desugar;
import org.wso2.ballerinalang.compiler.semantics.analyzer.CodeAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SemanticAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BConnectorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangObject;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecord;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnostic;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

/**
 * Parser Utils.
 */
public class ParserUtils {

    private static final Logger logger = LoggerFactory.getLogger(ParserUtils.class);

    private static final WorkspaceDocumentManagerImpl documentManager =
            WorkspaceDocumentManagerImpl.getInstance();

    private static Path untitledProject;

    private static final String UNTITLED_BAL = "untitled.bal";

    static {
        // Here we will create a tmp directory as the untitled project repo.
        File untitledDir = Files.createTempDir();
        untitledProject = untitledDir.toPath();
        // Now lets create a empty untitled.bal to fool compiler.
        File untitledBal = new File(Paths.get(untitledProject.toString(),
                                              UNTITLED_BAL).toString());
        try {
            untitledBal.createNewFile();
        } catch (IOException e) {
            logger.error("Unable to create untitled project directory, " +
                                 "unsaved files might not work properly.");
        }
    }

    /**
     * @param directoryCount - packagePath
     * @param filePath       - file path to parent directory of the .bal file
     * @return parent dir
     */
    public static java.nio.file.Path getProgramDirectory(int directoryCount, java.nio.file.Path filePath) {
        // find program directory
        java.nio.file.Path parentDir = filePath.getParent();
        for (int i = 0; i < directoryCount; ++i) {
            parentDir = parentDir.getParent();
        }
        return parentDir;
    }

    /**
     * This method is designed to generate the Ballerina model and Diagnostic information for a given Ballerina file.
     * saved in the file-system.
     *
     * @param programDir          - Path of the program directory.
     * @param compilationUnitName - compilationUnitName name.
     * @return BallerinaFile - Object which contains Ballerina model and Diagnostic information
     */
    public static BallerinaFile getBallerinaFile(String programDir, String compilationUnitName) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, programDir);
        options.put(COMPILER_PHASE, CompilerPhase.CODE_ANALYZE.toString());
        options.put(PRESERVE_WHITESPACE, Boolean.TRUE.toString());
        return getBallerinaFile(Paths.get(programDir), compilationUnitName, context);
    }

    /**
     * This method is designed to generate the Ballerina model and Diagnostic information for a given Ballerina content.
     * Ideal use case is generating Ballerina model and Diagnostic information for unsaved Ballerina files.
     *
     * @param fileName      - File name. This can be any arbitrary name as as we haven't save the file yet.
     * @param source        - Ballerina source content that needs to be parsed.
     * @param compilerPhase - This will tell up to which point(compiler phase) we should process the model
     * @return BallerinaFile - Object which contains Ballerina model and Diagnostic information
     */
    public static BallerinaFile getBallerinaFileForContent(Path filePath, String fileName, String source,
                                                           CompilerPhase compilerPhase) {
        CompilerContext context = prepareCompilerContext(fileName, source);
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(PRESERVE_WHITESPACE, Boolean.TRUE.toString());

        return getBallerinaFile(filePath, fileName, context);
    }

    /**
     * Returns a CompilerContext for the provided fileName and Ballerina source content.
     *
     * @param fileName - File name. This can be any arbitrary name as as we haven't save the file yet.
     * @param source   - Ballerina source content that needs to be parsed.
     * @return CompilerContext
     */
    private static CompilerContext prepareCompilerContext(String fileName, String source) {
        CompilerContext context = new CompilerContext();
        List<Name> names = new ArrayList<>();
        names.add(new org.wso2.ballerinalang.compiler.util.Name("."));
        // Registering custom PackageRepository to provide ballerina content without a file in file-system
        context.put(PackageRepository.class, new InMemoryPackageRepository(
                PackageID.DEFAULT,
                "", fileName, source.getBytes(StandardCharsets.UTF_8)));
        return context;
    }

    /**
     * Returns an object which contains Ballerina model and Diagnostic information.
     *
     * @param fileName - File name
     * @param context  - CompilerContext
     * @return BallerinaFile - Object which contains Ballerina model and Diagnostic information
     */
    private static BallerinaFile getBallerinaFile(Path packagePath, String fileName, CompilerContext context) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        ComposerDiagnosticListener composerDiagnosticListener = new ComposerDiagnosticListener(diagnostics);
        context.put(DiagnosticListener.class, composerDiagnosticListener);


        BallerinaFile ballerinaFile = new BallerinaFile();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, packagePath.toString());
        options.put(COMPILER_PHASE, CompilerPhase.DEFINE.toString());
        options.put(PRESERVE_WHITESPACE, "true");
        context.put(SourceDirectory.class, new FileSystemProjectDirectory(packagePath));

        Compiler compiler = Compiler.getInstance(context);
        // compile
        try {
            ballerinaFile.setBLangPackage(compiler.compile(fileName));
        } catch (Exception ex) {
            BDiagnostic catastrophic = new BDiagnostic();
            catastrophic.msg = "Failed in the runtime parse/analyze. " + ex.getMessage();
            diagnostics.add(catastrophic);
        }

        ballerinaFile.setDiagnostics(diagnostics);
        return ballerinaFile;
    }

    /**
     * Get All Native Packages.
     *
     * @return {@link Map} Package name, package functions and connectors
     */
    public static Map<String, ModelPackage> getAllPackages() {
        final Map<String, ModelPackage> modelPackage = new HashMap<>();
        // TODO: remove once the packerina api for package listing is available
        final String[] packageNames = {"http", "http.swagger", "net.uri", "mime", "auth", "auth.authz",
                "auth.authz.permissionstore", "auth.basic", "auth.jwtAuth", "auth.userstore", "auth.utils", "caching",
                "collections", "config", "data.sql", "file", "internal", "io", "jwt", "jwt.signature", "log", "math",
                "os", "reflect", "runtime", "security.crypto", "task", "time", "transactions.coordinator", "user",
                "util"};
        try {
            List<BLangPackage> builtInPackages = LSPackageLoader.getBuiltinPackages();
            for (BLangPackage bLangPackage : builtInPackages) {
                loadPackageMap(bLangPackage.packageID.getName().getValue(), bLangPackage, modelPackage);
            }

            CompilerContext context = CommonUtil.prepareTempCompilerContext();
            for (String packageName : packageNames) {
                PackageID packageID = new PackageID(new Name("ballerina"),
                        new Name(packageName), new Name("0.0.0"));
                BLangPackage bLangPackage = LSPackageLoader.getPackageById(context, packageID);
                loadPackageMap(bLangPackage.packageID.getName().getValue(), bLangPackage, modelPackage);
            }
        } catch (Exception e) {
            // Above catch is to fail safe composer front end due to core errors.
            logger.warn("Error while loading package: " + e.getMessage());
        }
        return modelPackage;
    }

    /**
     * Get the builtin types.
     *
     * @return {@link List} list of builtin types
     */
    public static List<SymbolInformation> getBuiltinTypes() {
        CompilerContext context = prepareCompilerContext("", "");
        SymbolTable symbolTable = SymbolTable.getInstance(context);
        List<SymbolInformation> symbolInformationList = new ArrayList<>();

        // TODO: Need to fill the default values
        symbolTable.rootScope.entries.forEach((key, value) -> {
            if (value.symbol instanceof BTypeSymbol) {
                SymbolInformation symbolInfo = new SymbolInformation();
                String symbolName = value.symbol.getName().getValue();
                if (!symbolName.equals(BuiltInType.INVALID_TYPE)) {
                    symbolInfo.setName(symbolName);
                    setDefaultValuesForType(symbolName, symbolInfo);
                    symbolInformationList.add(symbolInfo);
                }
            }
        });

        return symbolInformationList;
    }

    private static void setDefaultValuesForType(String type, SymbolInformation symbolInfo) {
        switch (type) {
            case BuiltInType.INT:
                symbolInfo.setDefaultValue(BuiltInType.INT_DEFAULT);
                break;
            case BuiltInType.FLOAT:
                symbolInfo.setDefaultValue(BuiltInType.FLOAT_DEFAULT);
                break;
            case BuiltInType.STRING:
                symbolInfo.setDefaultValue(BuiltInType.STRING_DEFAULT);
                break;
            case BuiltInType.BOOLEAN:
                symbolInfo.setDefaultValue(BuiltInType.BOOLEAN_DEFAULT);
                break;
            default:
                // TODO: Here we are setting the null for blob as well
                symbolInfo.setDefaultValue(BuiltInType.NULL_DEFAULT);
                break;
        }
    }

    /**
     * Function to convert org.wso2.ballerinalang.compiler.util.Name instance to
     * org.wso2.ballerinalang.compiler.tree.BLangIdentifier instance.
     */
    static java.util.function.Function<Name, BLangIdentifier> nameToBLangIdentifier =
            name -> {
                BLangIdentifier bLangIdentifier = new BLangIdentifier();
                bLangIdentifier.setValue(name.getValue());
                return bLangIdentifier;
            };

    /**
     * Function to convert org.wso2.ballerinalang.compiler.tree.BLangIdentifier instance to
     * java.lang.String instance.
     */
    public static final java.util.function.Function<BLangIdentifier, String> B_LANG_IDENTIFIER_TO_STRING =
            name -> name.getValue();

    /**
     * Add connectors, functions, annotations etc. to packages.
     *
     * @param packageName package name
     * @param pkg         BLangPackage instance
     * @param packages    packages map
     */
    public static void loadPackageMap(String packageName, final org.wso2.ballerinalang.compiler.tree.BLangPackage pkg,
                                      Map<String, ModelPackage> packages) {
        if (pkg != null) {
            pkg.getFunctions().forEach((function) -> extractFunction(packages, packageName, function));
            pkg.getStructs().forEach((struct) -> extractStruct(packages, packageName, struct));
            pkg.getAnnotations().forEach((annotation) -> extractAnnotation(packages, packageName, annotation));
            pkg.getConnectors().forEach((connector) -> extractConnector(packages, packageName, connector));
            pkg.getEnums().forEach((enumerator) -> extractEnums(packages, packageName, enumerator));
            pkg.objects.forEach((object) -> extractObjects(packages, packageName, object));
            pkg.records.forEach((record) -> extractRecords(packages, packageName, record));
            extractEndpoints(packages, packageName, pkg.structs, pkg.functions);
        }
    }

    /**
     * Extract annotations from ballerina lang.
     *
     * @param packages   packages to send
     * @param annotation annotation
     */
    private static void extractAnnotation(Map<String, ModelPackage> packages, String packagePath,
                                          BLangAnnotation annotation) {
        if (packages.containsKey(packagePath)) {
            ModelPackage modelPackage = packages.get(packagePath);

            modelPackage.addAnnotationsItem(AnnotationDef.convertToPackageModel(annotation));
        } else {
            ModelPackage modelPackage = new ModelPackage();
            modelPackage.setName(packagePath);

            modelPackage.addAnnotationsItem(AnnotationDef.convertToPackageModel(annotation));
            packages.put(packagePath, modelPackage);
        }
    }

    /**
     * Extract connectors from ballerina lang.
     *
     * @param packages  packages to send
     * @param connector connector
     */
    private static void extractConnector(Map<String, ModelPackage> packages, String packagePath,
                                         BLangConnector connector) {
        String fileName = connector.getPosition().getSource().getCompilationUnitName();
        if (packages.containsKey(packagePath)) {
            ModelPackage modelPackage = packages.get(packagePath);
            List<Parameter> parameters = new ArrayList<>();
            addParameters(parameters, connector.getParameters());

            List<AnnotationAttachment> annotations = new ArrayList<>();
            addAnnotations(annotations, connector.getAnnotationAttachments());

            List<Action> actions = new ArrayList<>();
            addActions(actions, connector.getActions());

            modelPackage.addConnectorsItem(createNewConnector(connector.getName().getValue(),
                                                              annotations, actions, parameters, null, fileName));
        } else {
            ModelPackage modelPackage = new ModelPackage();
            modelPackage.setName(packagePath);

            List<Parameter> parameters = new ArrayList<>();
            addParameters(parameters, connector.getParameters());

            List<AnnotationAttachment> annotations = new ArrayList<>();
            addAnnotations(annotations, connector.getAnnotationAttachments());

            List<Action> actions = new ArrayList<>();
            addActions(actions, connector.getActions());

            modelPackage.addConnectorsItem(createNewConnector(connector.getName().getValue(),
                                                              annotations, actions, parameters, null, fileName));
            packages.put(packagePath, modelPackage);
        }
    }

    /**
     * Extract Enums from ballerina lang.
     *
     * @param packages    packages to send.
     * @param packagePath packagePath.
     * @param bLangEnum   enum.
     */
    private static void extractEnums(Map<String, ModelPackage> packages, String packagePath,
                                     EnumNode bLangEnum) {
        String fileName = bLangEnum.getPosition().getSource().getCompilationUnitName();
        if (packages.containsKey(packagePath)) {
            ModelPackage modelPackage = packages.get(packagePath);
            modelPackage.addEnumItem(createNewEnum(bLangEnum.getName().getValue(), bLangEnum.getEnumerators(),
                    fileName));
        } else {
            ModelPackage modelPackage = new ModelPackage();
            modelPackage.setName(packagePath);
            modelPackage.addEnumItem(createNewEnum(bLangEnum.getName().getValue(), bLangEnum.getEnumerators(),
                    fileName));
            packages.put(packagePath, modelPackage);
        }
    }

    /**
     * Extract Functions from ballerina lang.
     *
     * @param packages    packages to send.
     * @param packagePath package path
     * @param function    function.
     */
    private static void extractFunction(Map<String, ModelPackage> packages, String packagePath,
                                        BLangFunction function) {
        String fileName = function.getPosition().getSource().getCompilationUnitName();
        if (packages.containsKey(packagePath)) {
            ModelPackage modelPackage = packages.get(packagePath);
            List<Parameter> parameters = new ArrayList<>();
            addParameters(parameters, function.getParameters());

            List<Parameter> returnParameters = new ArrayList<>();
            //addParameters(returnParameters, function.getReturnTypeNode());

            List<AnnotationAttachment> annotations = new ArrayList<>();
            addAnnotations(annotations, function.getAnnotationAttachments());

            String receiverType = getReceiverType(function.getReceiver());

            // Check if the function is public or not
            boolean isPublic = function.getFlags().contains(Flag.PUBLIC);

            modelPackage.addFunctionsItem(createNewFunction(function.getName().getValue(),
                    annotations, parameters, returnParameters, receiverType, isPublic, fileName));
        } else {
            ModelPackage modelPackage = new ModelPackage();
            modelPackage.setName(packagePath);
            List<Parameter> parameters = new ArrayList<>();
            addParameters(parameters, function.getParameters());

            List<Parameter> returnParameters = new ArrayList<>();
            //addParameters(returnParameters, function.getReturnTypeNode());

            List<AnnotationAttachment> annotations = new ArrayList<>();
            addAnnotations(annotations, function.getAnnotationAttachments());

            String receiverType = getReceiverType(function.getReceiver());

            // Check if the function is public or not
            boolean isPublic = function.getFlags().contains(Flag.PUBLIC);

            modelPackage.addFunctionsItem(createNewFunction(function.getName().getValue(),
                    annotations, parameters, returnParameters, receiverType, isPublic, fileName));
            packages.put(packagePath, modelPackage);
        }
    }

    /**
     * Extract receiverType from the function receiver.
     *
     * @param receiver receiver
     * @return receiverType
     */
    private static String getReceiverType(VariableNode receiver) {
        if (receiver == null) {
            return null;
        }
        TypeNode typeNode = receiver.getTypeNode();
        String receiverType = null;
        if (typeNode instanceof BLangUserDefinedType) {
            receiverType = ((BLangUserDefinedType) typeNode).getTypeName().getValue();
        } else if (typeNode instanceof BLangBuiltInRefTypeNode) {
            receiverType = ((BLangBuiltInRefTypeNode) typeNode).getTypeKind().typeName();
        } else if (typeNode instanceof BLangValueType) {
            receiverType = ((BLangValueType) typeNode).getTypeKind().typeName();
        } else {
            return null;
        }

        return receiverType;
    }

    /**
     * Extract Structs from ballerina lang.
     *
     * @param packages    packages to send.
     * @param packagePath packagePath.
     * @param struct      struct.
     */
    private static void extractStruct(Map<String, ModelPackage> packages, String packagePath,
                                      BLangStruct struct) {
        String fileName = struct.getPosition().getSource().getCompilationUnitName();
        if (packages.containsKey(packagePath)) {
            ModelPackage modelPackage = packages.get(packagePath);
            modelPackage.addStructsItem(createNewStruct(struct.getName().getValue(), struct.getFields(), fileName));
        } else {
            ModelPackage modelPackage = new ModelPackage();
            modelPackage.setName(packagePath);
            modelPackage.addStructsItem(createNewStruct(struct.getName().getValue(), struct.getFields(), fileName));
            packages.put(packagePath, modelPackage);
        }
    }

    private static void extractObjects(Map<String, ModelPackage> packages, String packagePath, BLangObject object) {
        String fileName = object.getPosition().getSource().getCompilationUnitName();
        if (packages.containsKey(packagePath)) {
            ModelPackage modelPackage = packages.get(packagePath);
            modelPackage.addObjectItem(
                    createNewObject(object.name.getValue(), object.fields, object.functions, fileName));
        } else {
            ModelPackage modelPackage = new ModelPackage();
            modelPackage.setName(packagePath);
            modelPackage.addObjectItem(
                    createNewObject(object.name.getValue(), object.fields, object.functions, fileName));
            packages.put(packagePath, modelPackage);
        }
    }

    private static void extractRecords(Map<String, ModelPackage> packages, String packagePath, BLangRecord record) {
        String fileName = record.getPosition().getSource().getCompilationUnitName();
        if (packages.containsKey(packagePath)) {
            ModelPackage modelPackage = packages.get(packagePath);
            modelPackage.addRecord(createNewRecord(record.name.getValue(), record.fields, fileName));
        } else {
            ModelPackage modelPackage = new ModelPackage();
            modelPackage.setName(packagePath);
            modelPackage.addRecord(createNewRecord(record.name.getValue(), record.fields, fileName));
            packages.put(packagePath, modelPackage);
        }
    }

    private static void extractEndpoints(Map<String, ModelPackage> packages, String packagePath,
                                         List<BLangStruct> structs, List<BLangFunction> functions) {
        functions.forEach((function) -> {
            if (function.getName().getValue().equals("register") && function.receiver != null &&
                    function.receiver.getTypeNode() instanceof UserDefinedTypeNode) {
                String structName = function.receiver.getTypeNode().type.tsymbol.name.getValue();
                structs.forEach((struct) -> {
                    if (struct.name.getValue().equals(structName)) {
                        String fileName = struct.getPosition().getSource().getCompilationUnitName();
                        if (packages.containsKey(packagePath)) {
                            ModelPackage modelPackage = packages.get(packagePath);
                            modelPackage.addEndpointItem(createNewEndpoint(struct.name.getValue(),
                                    new ArrayList<>(), struct.fields, packagePath, fileName));
                        } else {
                            ModelPackage modelPackage = new ModelPackage();
                            modelPackage.setName(packagePath);
                            modelPackage.addEndpointItem(createNewEndpoint(struct.name.getValue(), new ArrayList<>(),
                                    struct.fields, packagePath, fileName));
                            packages.put(packagePath, modelPackage);
                        }
                    }
                });
            }
        });
    }

    /**
     * Add parameters to a list from ballerina lang param list.
     *
     * @param params            params to send.
     * @param argumentTypeNames argument types
     */
    private static void addParameters(List<Parameter> params, List<BLangVariable> argumentTypeNames) {
        if (argumentTypeNames != null) {
            argumentTypeNames.forEach(item -> params.add(createNewParameter(item.getName().getValue(),
                    item.getTypeNode().type.toString(), item.getTypeNode())));
        }
    }

    /**
     * Add annotations to a list from ballerina lang annotation list.
     *
     * @param annotations               annotations list to be sent
     * @param bLangAnnotationAttachment annotations
     */
    private static void addAnnotations(List<AnnotationAttachment> annotations,
                                       List<BLangAnnotationAttachment> bLangAnnotationAttachment) {
        bLangAnnotationAttachment.forEach(annotation -> annotations.add(AnnotationAttachment.
                convertToPackageModel(annotation)));
    }

    /**
     * Add Actions to the connector.
     *
     * @param actionsList action list to be sent
     * @param actions     native actions retrieve from the connector
     */
    private static void addActions(List<Action> actionsList, List<BLangAction> actions) {
        actions.forEach(action -> actionsList.add(extractAction(action)));
    }

    /**
     * Extract action details from a connector.
     *
     * @param action action.
     * @return {Action} action
     */
    private static Action extractAction(BLangAction action) {
        List<Parameter> parameters = new ArrayList<>();
        addParameters(parameters, action.getParameters());

        List<AnnotationAttachment> annotations = new ArrayList<>();
        addAnnotations(annotations, action.getAnnotationAttachments());

        List<Parameter> returnParameters = new ArrayList<>();
        //addParameters(returnParameters, action.getReturnParameters());

        String fileName = action.getPosition().getSource().getCompilationUnitName();
        return createNewAction(action.getName().getValue(), parameters, returnParameters, annotations, fileName);
    }

    /**
     * Create new action.
     *
     * @param name         action name
     * @param params       list of params
     * @param returnParams list of return params
     * @param annotations  list of annotations
     * @return {Action} action
     */
    private static Action createNewAction(String name, List<Parameter> params, List<Parameter> returnParams,
                                          List<AnnotationAttachment> annotations, String fileName) {
        Action action = new Action();
        action.setName(name);
        action.setParameters(params);
        action.setReturnParams(returnParams);
        action.setAnnotations(annotations);
        action.setFileName(fileName);
        return action;
    }

    /**
     * Create new parameter.
     *
     * @param name parameter name
     * @param type parameter type
     * @return {Parameter} parameter
     */
    private static Parameter createNewParameter(String name, String type, BLangType typeNode) {
        Parameter parameter = new Parameter();
        parameter.setType(type);
        parameter.setName(name);
        BType bType = typeNode.type;
        if (bType instanceof BConnectorType) {
            parameter.setPkgAlias(((BLangUserDefinedType) typeNode).pkgAlias.toString());
            parameter.setConnector(true);
        }
        return parameter;
    }

    /**
     * Create new function.
     *
     * @param name         name of the function
     * @param annotations  list of annotations
     * @param params       list of parameters
     * @param returnParams list of return params
     * @return {Function} function
     */
    private static Function createNewFunction(String name, List<AnnotationAttachment> annotations,
                                              List<Parameter> params, List<Parameter> returnParams,
                                              String receiverType, boolean isPublic, String fileName) {
        Function function = new Function();
        function.setName(name);
        function.setAnnotations(annotations);
        function.setParameters(params);
        function.setReturnParams(returnParams);
        function.setReceiverType(receiverType);
        function.setPublic(isPublic);
        function.setFileName(fileName);
        return function;
    }

    /**
     * Create new struct.
     *
     * @param name   name of the struct
     * @param fields field definiton statements
     * @return {Function} function
     */
    private static Struct createNewStruct(String name, List<BLangVariable> fields, String fileName) {
        Struct struct = new Struct(name);
        fields.forEach((field) -> {
            String defaultValue = null;
            if (field.getInitialExpression() != null) {
                defaultValue = ((BLangLiteral) field.getInitialExpression()).getValue().toString();
            }
            StructField structField = createNewStructField(field.getName().getValue(),
                    field.getTypeNode().type.toString(), defaultValue);
            struct.addStructField(structField);
        });
        struct.setFileName(fileName);
        return struct;
    }

    /**
     * Create new object model.
     *
     * @param name      name of the object
     * @param fields    object fields
     * @param functions object bound functions
     * @param fileName  fileName
     * @return {@link ObjectModel}
     */
    private static ObjectModel createNewObject(String name, List<BLangVariable> fields, List<BLangFunction> functions,
                                               String fileName) {
        ObjectModel objectModel = new ObjectModel(name);
        fields.forEach((field) -> {
            ObjectField objectField = new ObjectField(field.name.getValue(), field.getTypeNode().type.toString());
            objectModel.addField(objectField);
        });

        functions.forEach((function) -> {
            List<AnnotationAttachment> annotations = new ArrayList<>();
            addAnnotations(annotations, function.getAnnotationAttachments());
            List<Parameter> parameters = new ArrayList<>();
            addParameters(parameters, function.getParameters());
            objectModel.addFunction(createNewFunction(function.name.getValue(), annotations, parameters,
                    new ArrayList<>(), function.receiver.getTypeNode().type.toString(), false, fileName));
        });

        return objectModel;
    }

    /**
     * Create a new Record Model.
     *
     * @param name     Record Name
     * @param fields   List of Fields
     * @param fileName File name
     * @return {@link RecordModel}  New Record Model
     */
    private static RecordModel createNewRecord(String name, List<BLangVariable> fields, String fileName) {
        RecordModel recordModel = new RecordModel(name);

        fields.forEach((field) -> {
            ObjectField objectField = new ObjectField(field.name.getValue(), field.getTypeNode().type.toString());
            recordModel.addField(objectField);
        });
        recordModel.setFileName(fileName);

        return recordModel;
    }

    private static Endpoint createNewEndpoint(String name, List<BLangFunction> functions, List<BLangVariable> fields,
                                              String packageName, String fileName) {
        Endpoint endpoint = new Endpoint(name);
        endpoint.setFileName(fileName);
        // TODO: find actions for the endpoint
        endpoint.setActions(new ArrayList<>());
        endpoint.setPackageName(packageName);

        fields.forEach((field) -> {
            endpoint.addField(createNewStructField(field.name.getValue(), field.getTypeNode().type.toString(),
                    ""));
        });
        return endpoint;
    }

    /**
     * create a new struct field.
     *
     * @param name name of the field
     * @param type type of the field
     * @return
     */
    private static StructField createNewStructField(String name, String type, String defaultValue) {
        StructField structField = new StructField(name, type, defaultValue);
        return structField;
    }

    /**
     * Create new enum.
     *
     * @param name        name of the enum
     * @param enumerators
     * @return {Enum} enum
     */
    private static Enum createNewEnum(String name, List<? extends EnumNode.Enumerator> enumerators, String fileName) {
        Enum anEnum = new Enum(name);
        enumerators.forEach((enumeratorItem) -> {
            Enumerator enumerator = createNewEnumerator(enumeratorItem.getName().getValue());
            anEnum.addEnumerator(enumerator);
        });
        anEnum.setFileName(fileName);
        return anEnum;
    }

    /**
     * create a new enumerator item for the enum.
     *
     * @param name name of the field
     * @return
     */
    private static Enumerator createNewEnumerator(String name) {
        Enumerator enumerator = new Enumerator(name);
        return enumerator;
    }

    /**
     * Create new connector.
     *
     * @param name         name of the connector
     * @param annotations  list of annotation
     * @param actions      list of actions
     * @param params       list of params
     * @param returnParams list of return params
     * @return {Connector} connector
     */
    private static Connector createNewConnector(String name, List<AnnotationAttachment> annotations,
                                                List<Action> actions, List<Parameter> params,
                                                List<Parameter> returnParams, String fileName) {
        Connector connector = new Connector();
        connector.setName(name);
        connector.setActions(actions);
        connector.setParameters(params);
        connector.setAnnotations(annotations);
        connector.setReturnParameters(returnParams);
        connector.setFileName(fileName);
        return connector;
    }

    /**
     * Loading builtin packages.
     *
     * @param context compiler context
     * @return {BLangPackage} builtIn package
     */
    private static BLangPackage loadBuiltInPackage(CompilerContext context) {
        PackageLoader pkgLoader = PackageLoader.getInstance(context);
        SymbolTable symbolTable = SymbolTable.getInstance(context);
        SemanticAnalyzer semAnalyzer = SemanticAnalyzer.getInstance(context);
        CodeAnalyzer codeAnalyzer = CodeAnalyzer.getInstance(context);
        Desugar desugar = Desugar.getInstance(context);
        BLangPackage builtInPkg = desugar.perform(codeAnalyzer.analyze(semAnalyzer.analyze(
                pkgLoader.loadAndDefinePackage(Names.BUILTIN_ORG.getValue(), Names.BUILTIN_PACKAGE.getValue()))));
        symbolTable.builtInPackageSymbol = builtInPkg.symbol;
        return builtInPkg;
    }
}
