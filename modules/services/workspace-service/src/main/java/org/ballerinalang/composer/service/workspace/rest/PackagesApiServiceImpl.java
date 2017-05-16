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

package org.ballerinalang.composer.service.workspace.rest;

import org.ballerinalang.composer.service.workspace.api.NotFoundException;
import org.ballerinalang.composer.service.workspace.api.PackagesApiService;
import org.ballerinalang.composer.service.workspace.model.Action;
import org.ballerinalang.composer.service.workspace.model.Annotation;
import org.ballerinalang.composer.service.workspace.model.Connector;
import org.ballerinalang.composer.service.workspace.model.Function;
import org.ballerinalang.composer.service.workspace.model.ModelPackage;
import org.ballerinalang.composer.service.workspace.model.Parameter;
import org.ballerinalang.model.AnnotationDef;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.BallerinaAction;
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.model.NativeScope;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.natives.NativeConstructLoader;
import org.ballerinalang.util.exceptions.NativeException;
import org.ballerinalang.util.program.BLangPackages;
import org.ballerinalang.util.repository.BuiltinPackageRepository;
import org.ballerinalang.util.repository.FileSystemPackageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import javax.ws.rs.core.Response;

/**
 * This is the service implementation class for Packages list related operations
 *
 * @since 0.8.0
 */
public class PackagesApiServiceImpl extends PackagesApiService {

    private static final Logger logger = LoggerFactory.getLogger(WorkspaceService.class);

    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_NAME = "Access-Control-Allow-Origin";
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_VALUE = "*";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS_NAME = "Access-Control-Allow-Headers";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS_VALUE = "content-type";
    private static final String ACCESS_CONTROL_ALLOW_METHODS_NAME = "Access-Control-Allow-Methods";
    private static final String ACCESS_CONTROL_ALLOW_METHODS_VALUE = "OPTIONS, GET, POST";

    public PackagesApiServiceImpl() {
    }

    @Override
    public Response packagesGet(Integer limit, Integer offset, String query, String accept, String ifNoneMatch)
            throws NotFoundException {
        return setCORSHeaders(Response.ok(getAllPackages().values())).build();
    }

    @Override
    public Response packagesPackageNameGet(String packageName, String accept, String ifNoneMatch,
                                           String ifModifiedSince) throws NotFoundException {
        Map<String, ModelPackage> allPackages = getAllPackages();
        ModelPackage modelPackage = allPackages.get(packageName);
        if (modelPackage == null) {
            return Response.status(Response.Status.NOT_FOUND).header("Access-Control-Allow-Origin", '*').build();
        }
        return setCORSHeaders(Response.ok(modelPackage)).build();
    }

    @Override
    public Response packagesPost(String contentType) throws NotFoundException {
        return null;
    }

    @Override
    public Response packagesSendCORS() {
        return setCORSHeaders(Response.ok()).build();
    }

    /**
     * Get All Native Packages
     * @return {Map} <Package name, package functions and connectors>
     * */
    private Map<String, ModelPackage> getAllPackages() {
        final Map<String, ModelPackage> packages = new HashMap<>();
        // Getting full list of builtin package names
        String[] packagesArray =  BLangPackages.getBuiltinPackageNames();
        // Load all natives to globalscope
        GlobalScope globalScope = GlobalScope.getInstance();
        NativeScope nativeScope = NativeScope.getInstance();
        loadConstructs(globalScope, nativeScope);

        BuiltinPackageRepository[] pkgRepos = loadPackageRepositories();
        // this is just a dummy FileSystemPackageRepository instance. Paths.get(".") has no meaning here
        FileSystemPackageRepository fileRepo = new FileSystemPackageRepository(Paths.get("."), pkgRepos);
        // create program
        BLangProgram bLangProgram = new BLangProgram(globalScope, nativeScope, BLangProgram.Category.LIBRARY_PROGRAM);
        // turn off skipping native function parsing
        System.setProperty("skipNatives", "false");

        // process each package separately
        for (String builtInPkg : packagesArray) {
            Path packagePath = Paths.get(builtInPkg.replace(".", File.separator));
            if (bLangProgram.resolve(new SymbolName(builtInPkg)) == null) {
                // load package
                BLangPackage pkg = BLangPackages.loadPackage(packagePath, fileRepo,
                        bLangProgram);
                Stream.of(pkg.getAnnotationDefs()).forEach((annotationDef) -> extractAnnotationDefs(packages,
                        annotationDef));
                Stream.of(pkg.getConnectors()).forEach((connector) -> extractConnector(packages, pkg.getPackagePath(),
                        connector));
                Stream.of(pkg.getFunctions()).forEach((function) -> extractFunction(packages, pkg.getPackagePath(),
                        function));
            }
        }
        return packages;
    }


    /**
     * Extract annotations from ballerina lang
     * @param packages packages to send
     * @param annotationDef annotationDef
     * */
    private void extractAnnotationDefs(Map<String, ModelPackage> packages, AnnotationDef annotationDef) {

    }

    /**
     * Extract connectors from ballerina lang
     * @param packages packages to send
     * @param connector connector
     * */
    private void extractConnector(Map<String, ModelPackage> packages, String packagePath,
                                  org.ballerinalang.model.BallerinaConnectorDef connector) {
        if (packages.containsKey(packagePath)) {
            ModelPackage modelPackage = packages.get(packagePath);
            List<Parameter> parameters = new ArrayList<>();
            addParameters(parameters, connector.getParameterDefs());

            List<Annotation> annotations = new ArrayList<>();
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

            List<Annotation> annotations = new ArrayList<>();
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
    private void extractFunction(Map<String, ModelPackage> packages, String packagePath,
                                 org.ballerinalang.model.Function function) {
        if (packages.containsKey(packagePath)) {
            ModelPackage modelPackage = packages.get(packagePath);
            List<Parameter> parameters = new ArrayList<>();
            addParameters(parameters, function.getParameterDefs());

            List<Parameter> returnParameters = new ArrayList<>();
            addParameters(returnParameters, function.getReturnParameters());

            List<Annotation> annotations = new ArrayList<>();
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

            List<Annotation> annotations = new ArrayList<>();
            addAnnotations(annotations, function.getAnnotations());

            modelPackage.addFunctionsItem(createNewFunction(function.getName(),
                    annotations, parameters, returnParameters));
            packages.put(packagePath, modelPackage);
        }
    }

    /**
     * Add parameters to a list from ballerina lang param list.
     * @param params params to send.
     * @param argumentTypeNames argument types
     * */
    private void addParameters(List<Parameter> params, ParameterDef[] argumentTypeNames) {
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
    private void addAnnotations(List<Annotation> annotations,
                                org.ballerinalang.model.AnnotationAttachment[] annotationsFromModel) {
        Stream.of(annotationsFromModel)
                .forEach(annotation -> annotations.add(createNewAnnotation(annotation.getName(),
                        annotation.getValue())));
    }

    /**
     * Add Actions to the connector.
     * @param actionsList action list to be sent
     * @param actions native actions retrieve from the connector
     * */
    private void addActions(List<Action> actionsList, BallerinaAction[] actions) {
        Stream.of(actions)
                .forEach(action -> actionsList.add(extractAction(action)));
    }

    /**
     * Extract action details from a connector.
     * @param action action.
     * @return {Action} action
     * */
    private Action extractAction(BallerinaAction action) {
        List<Parameter> parameters = new ArrayList<>();
        addParameters(parameters, action.getParameterDefs());
        List<Annotation> annotations = new ArrayList<>();
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
    private Action createNewAction(String name, List<Parameter> params, List<Parameter> returnParams,
                                   List<Annotation> annotations) {
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
    private Parameter createNewParameter(String name, SimpleTypeName type) {
        Parameter parameter = new Parameter();
        parameter.setType(type.getName());
        parameter.setName(name);
        return parameter;
    }

    /**
     * Create new annotations
     * @param name annotation name
     * @param value annotation value
     * @return {Annotation} annotation
     * */
    private Annotation createNewAnnotation(String name, String value) {
        Annotation annotation = new Annotation();
        annotation.setName(name);
        annotation.setValue(value);
        return annotation;
    }

    /**
     * Create new function
     * @param name name of the function
     * @param annotations list of annotations
     * @param params list of parameters
     * @param returnParams list of return params
     * @return {Function} function
     * */
    private Function createNewFunction(String name, List<Annotation> annotations, List<Parameter> params,
                                       List<Parameter> returnParams) {
        Function function = new Function();
        function.setName(name);
        function.setAnnotations(annotations);
        function.setParameters(params);
        function.setReturnParams(returnParams);
        return function;
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
    private Connector createNewConnector(String name, List<Annotation> annotations, List<Action> actions,
                                         List<Parameter> params, List<Parameter> returnParams) {
        Connector connector = new Connector();
        connector.setName(name);
        connector.setActions(actions);
        connector.setParameters(params);
        connector.setAnnotations(annotations);
        connector.setReturnParameters(returnParams);
        return connector;
    }

    /**
     * Set cross origin header to avoid cross origin issues.
     * @param responseBuilder response builder
     * @return {ResponseBuilder} response builder
     * */
    private static Response.ResponseBuilder setCORSHeaders(Response.ResponseBuilder responseBuilder) {
        return responseBuilder
                .header(ACCESS_CONTROL_ALLOW_ORIGIN_NAME, ACCESS_CONTROL_ALLOW_ORIGIN_VALUE)
                .header(ACCESS_CONTROL_ALLOW_HEADERS_NAME, ACCESS_CONTROL_ALLOW_HEADERS_VALUE)
                .header(ACCESS_CONTROL_ALLOW_METHODS_NAME, ACCESS_CONTROL_ALLOW_METHODS_VALUE);
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
}