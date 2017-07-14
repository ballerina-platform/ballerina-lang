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

import org.ballerinalang.composer.service.workspace.model.Action;
import org.ballerinalang.composer.service.workspace.model.AnnotationAttachment;
import org.ballerinalang.composer.service.workspace.model.AnnotationDef;
import org.ballerinalang.composer.service.workspace.model.Connector;
import org.ballerinalang.composer.service.workspace.model.Function;
import org.ballerinalang.composer.service.workspace.model.ModelPackage;
import org.ballerinalang.composer.service.workspace.model.Parameter;
import org.ballerinalang.composer.service.workspace.model.Struct;
import org.ballerinalang.composer.service.workspace.model.StructField;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.BallerinaAction;
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.model.NativeScope;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.expressions.BasicLiteral;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.natives.NativeConstructLoader;
import org.ballerinalang.util.exceptions.NativeException;
import org.ballerinalang.util.program.BLangPackages;
import org.ballerinalang.util.repository.BuiltinPackageRepository;
import org.ballerinalang.util.repository.FileSystemPackageRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

/**
 * Utility methods for workspace service
 */
public class WorkspaceUtils {

    /**
     * Get All Native Packages
     * @return {@link Map} Package name, package functions and connectors
     */
    public static Map<String, ModelPackage> getAllPackages() {
        // Getting full list of builtin package names
        String[] packagesArray = BLangPackages.getBuiltinPackageNames();

        // Load all natives to globalscope
        GlobalScope globalScope = GlobalScope.getInstance();
        NativeScope nativeScope = NativeScope.getInstance();
        loadConstructs(globalScope, nativeScope);

        // create program
        BLangProgram bLangProgram = new BLangProgram(globalScope, nativeScope, BLangProgram.Category.LIBRARY_PROGRAM);
        return getResolvedPackagesMap(bLangProgram, packagesArray);
    }

    /**
     * Get a resolved package map for a given package names array
     * @param bLangProgram to get package map
     * @param packagesArray packages array
     * @return packages map
     */
    public static Map<String, ModelPackage> getResolvedPackagesMap(BLangProgram bLangProgram, String[] packagesArray) {
        final Map<String, ModelPackage> packages = new HashMap<>();
        BuiltinPackageRepository[] pkgRepos = loadPackageRepositories();
        // this is just a dummy FileSystemPackageRepository instance. Paths.get(".") has no meaning here
        FileSystemPackageRepository fileRepo = new FileSystemPackageRepository(Paths.get("."), pkgRepos);
        // turn off skipping native function parsing
        System.setProperty("skipNatives", "false");

        // process each package separately
        for (String builtInPkg : packagesArray) {
            Path packagePath = Paths.get(builtInPkg.replace(".", File.separator));
            BLangPackage pkg = null;
            BLangSymbol bLangSymbol = bLangProgram.resolve(new SymbolName(builtInPkg));
            if (bLangSymbol == null) {
                // load package
                pkg = BLangPackages.loadPackage(packagePath, fileRepo, bLangProgram);
                loadPackageMap(pkg, packages);
            } else {
                if (bLangSymbol instanceof BLangPackage) {
                    pkg = (BLangPackage) bLangSymbol;
                    loadPackageMap(pkg, packages);
                }
            }
        }
        return packages;
    }

    /**
     * Add connectors, functions, annotations etc. to packages.
     * @param pkg BLangPackage instance
     * @param packages packages map
     */
    private static void loadPackageMap(final BLangPackage pkg, Map<String, ModelPackage> packages) {
        if (pkg != null) {
            Stream.of(pkg.getAnnotationDefs()).forEach((annotationDef) -> extractAnnotationDefs(packages,
                    pkg.getPackagePath(), annotationDef));
            Stream.of(pkg.getConnectors()).forEach((connector) -> extractConnector(packages, pkg.getPackagePath(),
                    connector));
            Stream.of(pkg.getFunctions()).forEach((function) -> extractFunction(packages, pkg.getPackagePath(),
                    function));
            Stream.of(pkg.getStructDefs()).forEach((structDef) -> extractStructDefs(packages, pkg.getPackagePath(),
                    structDef));
        }
    }

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
    private static void extractConnector(Map<String, ModelPackage> packages, String packagePath,
                                  org.ballerinalang.model.BallerinaConnectorDef connector) {
        if (packages.containsKey(packagePath)) {
            ModelPackage modelPackage = packages.get(packagePath);
            List<Parameter> parameters = new ArrayList<>();
            addParameters(parameters, connector.getParameterDefs());

            List<AnnotationAttachment> annotations = new ArrayList<>();
            addAnnotations(annotations, connector.getAnnotations());

            List<Action> actions = new ArrayList<>();
            addActions(actions, connector.getActions());

            modelPackage.addConnectorsItem(createNewConnector(connector.getName(),
                    annotations, actions, parameters, null));
        } else {
            ModelPackage modelPackage = new ModelPackage();
            modelPackage.setName(packagePath);

            List<Parameter> parameters = new ArrayList<>();
            addParameters(parameters, connector.getParameterDefs());

            List<AnnotationAttachment> annotations = new ArrayList<>();
            addAnnotations(annotations, connector.getAnnotations());

            List<Action> actions = new ArrayList<>();
            addActions(actions, connector.getActions());

            modelPackage.addConnectorsItem(createNewConnector(connector.getName(),
                    annotations, actions, parameters, null));
            packages.put(packagePath, modelPackage);
        }
    }

    /**
     * Extract Functions from ballerina lang.
     * @param packages packages to send.
     * @param function function.
     * */
    private static void extractFunction(Map<String, ModelPackage> packages, String packagePath,
                                 org.ballerinalang.model.Function function) {
        if (packages.containsKey(packagePath)) {
            ModelPackage modelPackage = packages.get(packagePath);
            List<Parameter> parameters = new ArrayList<>();
            addParameters(parameters, function.getParameterDefs());

            List<Parameter> returnParameters = new ArrayList<>();
            addParameters(returnParameters, function.getReturnParameters());

            List<AnnotationAttachment> annotations = new ArrayList<>();
            addAnnotations(annotations, function.getAnnotations());

            modelPackage.addFunctionsItem(createNewFunction(function.getName(),
                    annotations, parameters, returnParameters));
        } else {
            ModelPackage modelPackage = new ModelPackage();
            modelPackage.setName(packagePath);
            List<Parameter> parameters = new ArrayList<>();
            addParameters(parameters, function.getParameterDefs());

            List<Parameter> returnParameters = new ArrayList<>();
            addParameters(returnParameters, function.getReturnParameters());

            List<AnnotationAttachment> annotations = new ArrayList<>();
            addAnnotations(annotations, function.getAnnotations());

            modelPackage.addFunctionsItem(createNewFunction(function.getName(),
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
    private static void addParameters(List<Parameter> params, ParameterDef[] argumentTypeNames) {
        if (argumentTypeNames != null) {
            Stream.of(argumentTypeNames)
                    .forEach(item -> params.add(createNewParameter(item.getName(), item.getTypeName())));
        }
    }

    /**
     * Add annotations to a list from ballerina lang annotation list
     * @param annotations annotations list to be sent
     * @param annotationsFromModel annotations
     * */
    private static void addAnnotations(List<AnnotationAttachment> annotations,
                                org.ballerinalang.model.AnnotationAttachment[] annotationsFromModel) {
        Stream.of(annotationsFromModel)
                .forEach(annotation -> annotations.add(AnnotationAttachment.convertToPackageModel(annotation)));
    }

    /**
     * Add Actions to the connector.
     * @param actionsList action list to be sent
     * @param actions native actions retrieve from the connector
     * */
    private static void addActions(List<Action> actionsList, BallerinaAction[] actions) {
        Stream.of(actions)
                .forEach(action -> actionsList.add(extractAction(action)));
    }

    /**
     * Extract action details from a connector.
     * @param action action.
     * @return {Action} action
     * */
    private static Action extractAction(BallerinaAction action) {
        List<Parameter> parameters = new ArrayList<>();
        addParameters(parameters, action.getParameterDefs());
    
        List<AnnotationAttachment> annotations = new ArrayList<>();
        addAnnotations(annotations, action.getAnnotations());
    
        List<Parameter> returnParameters = new ArrayList<>();
        addParameters(returnParameters, action.getReturnParameters());
        return createNewAction(action.getName(), parameters, returnParameters, annotations);
    }

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
     * @param type parameter type
     * @param name parameter name
     * @return {Parameter} parameter
     * */
    private static Parameter createNewParameter(String name, SimpleTypeName type) {
        Parameter parameter = new Parameter();
        parameter.setType(type.getName());
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
     * Load Package Repositories
     * @return {BuiltinPackageRepository[]} BuiltinPackageRepository
     * */
    private static BuiltinPackageRepository[] loadPackageRepositories() {
        Iterator<BuiltinPackageRepository> ballerinaBuiltinPackageRepositories =
                ServiceLoader.load(BuiltinPackageRepository.class).iterator();
        List<BuiltinPackageRepository> pkgRepositories = new ArrayList<>();
        while (ballerinaBuiltinPackageRepositories.hasNext()) {
            BuiltinPackageRepository constructLoader = ballerinaBuiltinPackageRepositories.next();
            pkgRepositories.add(constructLoader);
        }
        return pkgRepositories.toArray(new BuiltinPackageRepository[0]);
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
