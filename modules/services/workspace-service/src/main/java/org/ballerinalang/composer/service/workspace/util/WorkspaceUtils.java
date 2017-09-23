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
package org.ballerinalang.composer.service.workspace.util;

import org.ballerinalang.composer.service.workspace.langserver.model.Action;
import org.ballerinalang.composer.service.workspace.langserver.model.AnnotationAttachment;
import org.ballerinalang.composer.service.workspace.langserver.model.AnnotationDef;
import org.ballerinalang.composer.service.workspace.langserver.model.Connector;
import org.ballerinalang.composer.service.workspace.langserver.model.Function;
import org.ballerinalang.composer.service.workspace.langserver.model.ModelPackage;
import org.ballerinalang.composer.service.workspace.langserver.model.Parameter;
import org.ballerinalang.composer.service.workspace.langserver.model.Struct;
import org.ballerinalang.composer.service.workspace.langserver.model.StructField;
import org.ballerinalang.composer.service.workspace.rest.datamodel.InMemoryPackageRepository;
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.model.NativeScope;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.expressions.BasicLiteral;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.natives.NativeConstructLoader;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.util.exceptions.NativeException;
import org.wso2.ballerinalang.compiler.PackageLoader;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility methods for workspace service
 */
public class WorkspaceUtils {

    /**
     * Get All Native Packages
     * @return {@link Map} Package name, package functions and connectors
     */
    public static Map<String, ModelPackage> getAllPackages() {
        final Map<String, ModelPackage> modelPackage = new HashMap<>();

        CompilerContext context = new CompilerContext();

        List<org.wso2.ballerinalang.compiler.util.Name> names = new ArrayList<>();
        names.add(new org.wso2.ballerinalang.compiler.util.Name("."));
        // Registering custom PackageRepository to provide ballerina content without a file in file-system
        context.put(PackageRepository.class, new InMemoryPackageRepository(
                new PackageID(names, new org.wso2.ballerinalang.compiler.util.Name("0.0.0")),
                "", "", "".getBytes(StandardCharsets.UTF_8)));

        PackageLoader packageLoader = PackageLoader.getInstance(context);
        Set<PackageID> packages = packageLoader.listPackages();
        packages.stream().forEach(pkg -> {
            Name version = pkg.getPackageVersion();
            BLangIdentifier bLangIdentifier = new BLangIdentifier();
            bLangIdentifier.setValue(version.getValue());

            List<BLangIdentifier> pkgNameComps = pkg.getNameComps().stream().map(nameToBLangIdentifier)
                    .collect(Collectors.<BLangIdentifier>toList());

            //TODO Remove this if check once other packages are available to load.
            if ("[ballerina, utils, logger]".equals(pkgNameComps.toString())
                    || "[ballerina, utils]".equals(pkgNameComps.toString())) {
                org.wso2.ballerinalang.compiler.tree.BLangPackage bLangPackage = packageLoader
                        .loadPackage(pkgNameComps, bLangIdentifier);
                loadPackageMap(pkg.getName().getValue(), bLangPackage, modelPackage);
            }
        });
        return modelPackage;
    }

    /**
     * Function to convert org.wso2.ballerinalang.compiler.util.Name instance to
     * org.wso2.ballerinalang.compiler.tree.BLangIdentifier instance.
     */
    static java.util.function.Function<Name, BLangIdentifier> nameToBLangIdentifier =
            new java.util.function.Function<Name, BLangIdentifier>() {

        public BLangIdentifier apply(Name name) {
            BLangIdentifier bLangIdentifier = new BLangIdentifier();
            bLangIdentifier.setValue(name.getValue());
            return bLangIdentifier;
        }
    };

    /**
     * Get a resolved package map for a given package names array
     * @param bLangProgram to get package map
     * @param packagesArray packages array
     * @return packages map
     */
//    public static Map<String, ModelPackage> getResolvedPackagesMap(BLangProgram bLangProgram, String[] packagesArray) {
//        final Map<String, ModelPackage> packages = new HashMap<>();
//        ProgramDirRepository fileRepo = BLangPrograms.initProgramDirRepository(Paths.get("."));
//        // this is just a dummy FileSystemPackageRepository instance. Paths.get(".") has no meaning here
//        // turn off skipping native function parsing
//        System.setProperty("skipNatives", "false");
//
//        // process each package separately
//        for (String builtInPkg : packagesArray) {
//            Path packagePath = Paths.get(builtInPkg.replace(".", File.separator));
//            org.wso2.ballerinalang.compiler.tree.BLangPackage pkg = null;
//            BLangSymbol bLangSymbol = bLangProgram.resolve(new SymbolName(builtInPkg));
//            if (bLangSymbol == null) {
//                // load package
//                pkg = BLangPackages.loadPackage(packagePath, fileRepo, bLangProgram);
//                loadPackageMap(pkg, packages);
//            } else {
//                if (bLangSymbol instanceof BLangPackage) {
//                    pkg = (BLangPackage) bLangSymbol;
//                    loadPackageMap(pkg, packages);
//                }
//            }
//        }
//        return packages;
//    }


    /**
     * Add connectors, functions, annotations etc. to packages.
     * @param packageName package name
     * @param pkg BLangPackage instance
     * @param packages packages map
     */
    private static void loadPackageMap(String packageName, final org.wso2.ballerinalang.compiler.tree.BLangPackage pkg,
                                       Map<String, ModelPackage> packages) {
        if (pkg != null) {
           // Stream.of(pkg.getAnnotations()).forEach((annotationDef) -> extractAnnotationDefs(packages,
           //         "", annotationDef));
           // Stream.of(pkg.getConnectors()).forEach((connector) -> extractConnector(packages, "",
           //         connector));
            pkg.getFunctions().forEach((function) -> extractFunction(packages, packageName, function));
           // Stream.of(pkg.getStructDefs()).forEach((structDef) -> extractStructDefs(packages, pkg.getPackagePath(),
           //         structDef));
        }
    }

//    /**
//     * Add connectors, functions, annotations etc. to packages.
//     * @param pkg BLangPackage instance
//     * @param packages packages map
//     */
//    private static void loadPackageMap(final BLangPackage pkg, Map<String, ModelPackage> packages) {
//        if (pkg != null) {
//            Stream.of(pkg.getAnnotationDefs()).forEach((annotationDef) -> extractAnnotationDefs(packages,
//                    pkg.getPackagePath(), annotationDef));
//            Stream.of(pkg.getConnectors()).forEach((connector) -> extractConnector(packages, pkg.getPackagePath(),
//                    connector));
//            pkg.getFunctions()
//            Stream.of(pkg.getFunctions()).forEach((function) -> extractFunction(packages, pkg.getPackagePath(),
//                    function));
//            Stream.of(pkg.getStructDefs()).forEach((structDef) -> extractStructDefs(packages, pkg.getPackagePath(),
//                    structDef));
//        }
//    }

    /**
     * Extract annotations from ballerina lang
     * @param packages packages to send
     * @param annotationDef annotationDef
     * */
    private static void extractAnnotationDefs(Map<String, ModelPackage> packages, String packagePath,
                                              org.ballerinalang.model.AnnotationDef annotationDef) {
        if (packages.containsKey(packagePath)) {
            ModelPackage modelPackage = packages.get(packagePath);
        
            modelPackage.addAnnotationsItem(AnnotationDef.convertToPackageModel(annotationDef));
        } else {
            ModelPackage modelPackage = new ModelPackage();
            modelPackage.setName(packagePath);
        
            modelPackage.addAnnotationsItem(AnnotationDef.convertToPackageModel(annotationDef));
            packages.put(packagePath, modelPackage);
        }
    }

    /**
     * Extract connectors from ballerina lang
     * @param packages packages to send
     * @param connector connector
     * */
//    private static void extractConnector(Map<String, ModelPackage> packages, String packagePath,
//                                  org.ballerinalang.model.BallerinaConnectorDef connector) {
//        if (packages.containsKey(packagePath)) {
//            ModelPackage modelPackage = packages.get(packagePath);
//            List<Parameter> parameters = new ArrayList<>();
//            addParameters(parameters, connector.getParameterDefs());
//
//            List<AnnotationAttachment> annotations = new ArrayList<>();
//            addAnnotations(annotations, connector.getAnnotations());
//
//            List<Action> actions = new ArrayList<>();
//            addActions(actions, connector.getActions());
//
//            modelPackage.addConnectorsItem(createNewConnector(connector.getName(),
//                    annotations, actions, parameters, null));
//        } else {
//            ModelPackage modelPackage = new ModelPackage();
//            modelPackage.setName(packagePath);
//
//            List<Parameter> parameters = new ArrayList<>();
//            addParameters(parameters, connector.getParameterDefs());
//
//            List<AnnotationAttachment> annotations = new ArrayList<>();
//            addAnnotations(annotations, connector.getAnnotations());
//
//            List<Action> actions = new ArrayList<>();
//            addActions(actions, connector.getActions());
//
//            modelPackage.addConnectorsItem(createNewConnector(connector.getName(),
//                    annotations, actions, parameters, null));
//            packages.put(packagePath, modelPackage);
//        }
//    }

    /**
     * Extract Functions from ballerina lang.
     * @param packages packages to send.
     * @param packagePath package path
     * @param function function.
     * */
    private static void extractFunction(Map<String, ModelPackage> packages, String packagePath,
                                        BLangFunction function) {
        if (packages.containsKey(packagePath)) {
            ModelPackage modelPackage = packages.get(packagePath);
            List<Parameter> parameters = new ArrayList<>();
            addParameters(parameters, function.getParameters());

            List<Parameter> returnParameters = new ArrayList<>();
            addParameters(returnParameters, function.getReturnParameters());

            List<AnnotationAttachment> annotations = new ArrayList<>();
            addAnnotations(annotations, function.getAnnotationAttachments());

            modelPackage.addFunctionsItem(createNewFunction(function.getName().getValue(),
                    annotations, parameters, returnParameters));
        } else {
            ModelPackage modelPackage = new ModelPackage();
            modelPackage.setName(packagePath);
            List<Parameter> parameters = new ArrayList<>();
            addParameters(parameters, function.getParameters());

            List<Parameter> returnParameters = new ArrayList<>();
            addParameters(returnParameters, function.getReturnParameters());

            List<AnnotationAttachment> annotations = new ArrayList<>();
            addAnnotations(annotations, function.getAnnotationAttachments());

            modelPackage.addFunctionsItem(createNewFunction(function.getName().getValue(),
                    annotations, parameters, returnParameters));
            packages.put(packagePath, modelPackage);
        }
    }

    /**
     * Extract Structs from ballerina lang.
     * @param packages packages to send.
     * @param packagePath packagePath.
     * @param structDef structDef.
     * */
    private static void extractStructDefs(Map<String, ModelPackage> packages, String packagePath,
                                          org.ballerinalang.model.StructDef structDef) {
        if (packages.containsKey(packagePath)) {
            ModelPackage modelPackage = packages.get(packagePath);
            modelPackage.addStructsItem(createNewStruct(structDef.getName(), structDef.getFieldDefStmts()));
        } else {
            ModelPackage modelPackage = new ModelPackage();
            modelPackage.setName(packagePath);
            modelPackage.addStructsItem(createNewStruct(structDef.getName(), structDef.getFieldDefStmts()));
            packages.put(packagePath, modelPackage);
        }
    }

    /**
     * Add parameters to a list from ballerina lang param list.
     * @param params params to send.
     * @param argumentTypeNames argument types
     * */
    private static void addParameters(List<Parameter> params, List<BLangVariable> argumentTypeNames) {
        if (argumentTypeNames != null) {
            argumentTypeNames.forEach(item -> params.add(createNewParameter(item.getName().getValue(),
                    item.getTypeNode().getKind())));
        }
    }

    /**
     * Add annotations to a list from ballerina lang annotation list
     * @param annotations annotations list to be sent
     * @param bLangAnnotationAttachment annotations
     * */
    private static void addAnnotations(List<AnnotationAttachment> annotations,
                                       List<BLangAnnotationAttachment> bLangAnnotationAttachment) {
        bLangAnnotationAttachment.forEach(annotation -> annotations.add(AnnotationAttachment.
                convertToPackageModel(annotation)));
    }

    /**
     * Add Actions to the connector.
     * @param actionsList action list to be sent
     * @param actions native actions retrieve from the connector
     * */
//    private static void addActions(List<Action> actionsList, BallerinaAction[] actions) {
//        Stream.of(actions)
//                .forEach(action -> actionsList.add(extractAction(action)));
//    }

    /**
     * Extract action details from a connector.
     * @param action action.
     * @return {Action} action
     * */
//    private static Action extractAction(BallerinaAction action) {
//        List<Parameter> parameters = new ArrayList<>();
//        addParameters(parameters, action.getParameterDefs());
//
//        List<AnnotationAttachment> annotations = new ArrayList<>();
//        addAnnotations(annotations, action.getAnnotations());
//
//        List<Parameter> returnParameters = new ArrayList<>();
//        addParameters(returnParameters, action.getReturnParameters());
//        return createNewAction(action.getName(), parameters, returnParameters, annotations);
//    }

    /**
     * Create new action
     * @param name action name
     * @param params list of params
     * @param returnParams list of return params
     * @param annotations list of annotations
     * @return {Action} action
     * */
    private static Action createNewAction(String name, List<Parameter> params, List<Parameter> returnParams,
                                   List<AnnotationAttachment> annotations) {
        Action action = new Action();
        action.setName(name);
        action.setParameters(params);
        action.setReturnParams(returnParams);
        action.setAnnotations(annotations);
        return action;
    }

    /**
     * Create new parameter
     * @param name parameter name
     * @param nodeKind parameter node kind
     * @return {Parameter} parameter
     * */
    private static Parameter createNewParameter(String name, NodeKind nodeKind) {
        Parameter parameter = new Parameter();
        parameter.setType(nodeKind.name());
        parameter.setName(name);
        return parameter;
    }

    /**
     * Create new function
     * @param name name of the function
     * @param annotations list of annotations
     * @param params list of parameters
     * @param returnParams list of return params
     * @return {Function} function
     * */
    private static Function createNewFunction(String name, List<AnnotationAttachment> annotations,
                                              List<Parameter> params, List<Parameter> returnParams) {
        Function function = new Function();
        function.setName(name);
        function.setAnnotations(annotations);
        function.setParameters(params);
        function.setReturnParams(returnParams);
        return function;
    }

    /**
     * Create new struct
     * @param name name of the struct
     * @param fieldDefStmts field definiton statements
     * @return {Function} function
     * */
    private static Struct createNewStruct(String name, VariableDefStmt[] fieldDefStmts) {
        Struct struct = new Struct(name);
        Arrays.stream(fieldDefStmts).forEach(
                fieldStmt -> {
                    StructField structField = createNewStructField(fieldStmt.getVariableDef().getName(),
                                                     fieldStmt.getVariableDef().getTypeName().getName());
                    structField.setPackageName(fieldStmt.getVariableDef().getTypeName().getPackageName());
                    if (fieldStmt.getRExpr() != null && fieldStmt.getRExpr() instanceof BasicLiteral) {
                        structField.setDefaultValue(((BasicLiteral) fieldStmt.getRExpr()).getBValue().stringValue());
                    }
                    struct.addStructField(structField);
                });

        return struct;
    }

    /**
     * create a new struct field
     * @param name name of the field
     * @param type type of the field
     * @return
     */
    private static StructField createNewStructField(String name, String type) {
        StructField structField = new StructField(name, type);
        return structField;
    }

    /**
     * Create new connector
     * @param name name of the connector
     * @param annotations list of annotation
     * @param actions list of actions
     * @param params list of params
     * @param returnParams list of return params
     * @return {Connector} connector
     * */
    private static Connector createNewConnector(String name, List<AnnotationAttachment> annotations,
                                                List<Action> actions, List<Parameter> params,
                                                List<Parameter> returnParams) {
        Connector connector = new Connector();
        connector.setName(name);
        connector.setActions(actions);
        connector.setParameters(params);
        connector.setAnnotations(annotations);
        connector.setReturnParameters(returnParams);
        return connector;
    }

    /**
     * Load constructs
     * @param globalScope globalScope
     * @param nativeScope nativeScope
     * */
    private static void loadConstructs(GlobalScope globalScope, NativeScope nativeScope) {
        BTypes.loadBuiltInTypes(globalScope);
        Iterator<NativeConstructLoader> nativeConstructLoaders =
                ServiceLoader.load(NativeConstructLoader.class).iterator();
        while (nativeConstructLoaders.hasNext()) {
            NativeConstructLoader constructLoader = nativeConstructLoaders.next();
            try {
                constructLoader.load(nativeScope);
            } catch (NativeException e) {
                throw e;
            } catch (Throwable t) {
                throw new NativeException("internal error occured", t);
            }
        }
    }
    
    /**
     * Get program directory
     * @param filePath    - file path to parent directory of the .bal file
     * @param packageName - package name
     * @return
     */
    public static java.nio.file.Path gerProgramDirectory(java.nio.file.Path filePath, String packageName) {
        java.nio.file.Path parentDir = null;
        if (!".".equals(packageName)) {
            // find nested directory count using package name
            int directoryCount = (packageName.contains(".")) ? packageName.split("\\.").length
                    : 1;
            // find program directory
            parentDir = filePath;
            for (int i = 0; i < directoryCount; ++i) {
                if (parentDir != null) {
                    parentDir = parentDir.getParent();
                }
            }
        }
        return parentDir;
    }


    /**
     * Recursive method to search for .bal files and add their parent directory paths to the provided List
     * @param programDirPath - program directory path
     * @param filePaths - file path list
     * @param depth - depth of the directory hierarchy which we should search from the program directory
     */
    public static void searchFilePathsForBalFiles(java.nio.file.Path programDirPath,
                                            List<java.nio.file.Path> filePaths, int depth) {
        // this method is a recursive method. depth is the iteration count and we should return based on the depth count
        if (depth < 0) {
            return;
        }
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(programDirPath);
            depth = depth - 1;
            for (java.nio.file.Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    searchFilePathsForBalFiles(entry, filePaths, depth);
                }
                java.nio.file.Path file = entry.getFileName();
                if (file != null) {
                    String fileName = file.toString();
                    if (fileName.endsWith(".bal")) {
                        filePaths.add(entry.getParent());
                    }
                }
            }
            stream.close();
        } catch (IOException e) {
            // we are ignoring any exception and proceed.
            return;
        }
    }
}
