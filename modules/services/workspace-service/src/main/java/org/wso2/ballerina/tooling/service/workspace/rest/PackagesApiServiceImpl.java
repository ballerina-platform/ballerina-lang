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

package org.wso2.ballerina.tooling.service.workspace.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.symbols.BLangSymbol;
import org.wso2.ballerina.core.model.types.SimpleTypeName;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.NativeUnitProxy;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeAction;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.tooling.service.workspace.api.NotFoundException;
import org.wso2.ballerina.tooling.service.workspace.api.PackagesApiService;
import org.wso2.ballerina.tooling.service.workspace.model.Action;
import org.wso2.ballerina.tooling.service.workspace.model.Annotation;
import org.wso2.ballerina.tooling.service.workspace.model.Connector;
import org.wso2.ballerina.tooling.service.workspace.model.Function;
import org.wso2.ballerina.tooling.service.workspace.model.ModelPackage;
import org.wso2.ballerina.tooling.service.workspace.model.Parameter;
import org.wso2.ballerina.tooling.service.workspace.scope.APIScope;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private APIScope apiScope;

    public PackagesApiServiceImpl() {
        this.apiScope = new APIScope();
        BuiltInNativeConstructLoader.loadConstructs(this.apiScope);
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

    private Map<String, ModelPackage> getAllPackages() {
        Map<String, ModelPackage> packages = new HashMap<>();

        for (Map.Entry<SymbolName, BLangSymbol> entry : apiScope.getSymbolMap().entrySet()) {
            SymbolName key = entry.getKey();
            BLangSymbol symbol = entry.getValue();
            if (((NativeUnitProxy) symbol).load() instanceof AbstractNativeFunction) {
                AbstractNativeFunction abstractNativeFunction = (AbstractNativeFunction) ((NativeUnitProxy) symbol).load();
                if (packages.containsKey(key.getPkgPath())) {
                    ModelPackage modelPackage = packages.get(key.getPkgPath());
                    List<Parameter> parameters = new ArrayList<>();
                    addParameters(parameters, abstractNativeFunction.getArgumentTypeNames());

                    List<Parameter> returnParameters = new ArrayList<>();
                    addParameters(returnParameters, abstractNativeFunction.getReturnParamTypeNames());

                    List<Annotation> annotations = new ArrayList<>();
                    addAnnotations(annotations, abstractNativeFunction.getAnnotations());

                    modelPackage.addFunctionsItem(createNewFunction(abstractNativeFunction.getName(),
                            annotations, parameters, returnParameters));
                } else {
                    ModelPackage modelPackage = new ModelPackage();
                    modelPackage.setName(key.getPkgPath());
                    List<Parameter> parameters = new ArrayList<>();
                    addParameters(parameters, abstractNativeFunction.getArgumentTypeNames());

                    List<Parameter> returnParameters = new ArrayList<>();
                    addParameters(returnParameters, abstractNativeFunction.getReturnParamTypeNames());

                    List<Annotation> annotations = new ArrayList<>();
                    addAnnotations(annotations, abstractNativeFunction.getAnnotations());

                    modelPackage.addFunctionsItem(createNewFunction(abstractNativeFunction.getName(),
                            annotations, parameters, returnParameters));
                    packages.put(key.getPkgPath(), modelPackage);
                }
            } else if (((NativeUnitProxy) symbol).load() instanceof AbstractNativeConnector) {
                AbstractNativeConnector abstractNativeConnector = (AbstractNativeConnector) ((NativeUnitProxy) symbol).load();
                if (packages.containsKey(key.getPkgPath())) {
                    ModelPackage modelPackage = packages.get(key.getPkgPath());
                    List<Parameter> parameters = new ArrayList<>();
                    addParameters(parameters, abstractNativeConnector.getArgumentTypeNames());

                    List<Parameter> returnParameters = new ArrayList<>();
                    addParameters(returnParameters, abstractNativeConnector.getReturnParamTypeNames());

                    List<Annotation> annotations = new ArrayList<>();
                    List<Action> actions = new ArrayList<>();
                    addActions(actions, abstractNativeConnector);

                    modelPackage.addConnectorsItem(createNewConnector(abstractNativeConnector.getName(),
                            annotations, actions, parameters, returnParameters));
                } else {
                    ModelPackage modelPackage = new ModelPackage();
                    modelPackage.setName(key.getPkgPath());

                    List<Parameter> parameters = new ArrayList<>();
                    addParameters(parameters, abstractNativeConnector.getArgumentTypeNames());

                    List<Parameter> returnParameters = new ArrayList<>();
                    addParameters(returnParameters, abstractNativeConnector.getReturnParamTypeNames());

                    List<Annotation> annotations = new ArrayList<>();
                    List<Action> actions = new ArrayList<>();
                    addActions(actions, abstractNativeConnector);

                    modelPackage.addConnectorsItem(createNewConnector(abstractNativeConnector.getName(),
                            annotations, actions, parameters, returnParameters));
                    packages.put(key.getPkgPath(), modelPackage);
                }
            }
        }
        return packages;
    }

    private void addParameters(List<Parameter> params, SimpleTypeName[] argumentTypeNames) {
        for (int i = 0; i < argumentTypeNames.length; i++) {
            params.add(createNewParameter(argumentTypeNames[i].getName(), argumentTypeNames[i].getSymbolName().getName()));
        }
    }

    private void addAnnotations(List<Annotation> annotations, org.wso2.ballerina.core.model.Annotation[] annotationsFromModel) {
        for (int i = 0; i < annotationsFromModel.length; i++) {
            annotations.add(createNewAnnotation(annotationsFromModel[i].getName(), annotationsFromModel[i].getValue()));
        }
    }

    private void addActions(List<Action> actions, AbstractNativeConnector connector) {
        SymbolName execute = new SymbolName("ClientConnector.execute.ballerina.net.http:ClientConnector.string.string.message",
                "ballerina.net.http");
        SymbolName head = new SymbolName("ClientConnector.head.ballerina.net.http:ClientConnector.string.message",
                "ballerina.net.http");
        SymbolName delete = new SymbolName("ClientConnector.delete.ballerina.net.http:ClientConnector.string.message",
                "ballerina.net.http");
        SymbolName put = new SymbolName("ClientConnector.put.ballerina.net.http:ClientConnector.string.message",
                "ballerina.net.http");
        SymbolName post = new SymbolName("ClientConnector.post.ballerina.net.http:ClientConnector.string.message",
                "ballerina.net.http");
        SymbolName patch = new SymbolName("ClientConnector.patch.ballerina.net.http:ClientConnector.string.message",
                "ballerina.net.http");
        SymbolName get = new SymbolName("ClientConnector.get.ballerina.net.http:ClientConnector.string.message",
                "ballerina.net.http");

        AbstractNativeAction executeAction = getAction(execute, connector);
        List<Parameter> parameters = new ArrayList<>();
        addParameters(parameters, executeAction.getArgumentTypeNames());
        List<Annotation> annotations = new ArrayList<>();
        addAnnotations(annotations, executeAction.getAnnotations());
        List<Parameter> returnParameters = new ArrayList<>();
        addParameters(returnParameters, executeAction.getReturnParamTypeNames());

        actions.add(createNewAction(executeAction.getName(),parameters, returnParameters,annotations));

        AbstractNativeAction headAction = getAction(head, connector);
        parameters = new ArrayList<>();
        addParameters(parameters, headAction.getArgumentTypeNames());
        annotations = new ArrayList<>();
        addAnnotations(annotations, headAction.getAnnotations());
        returnParameters = new ArrayList<>();
        addParameters(returnParameters, headAction.getReturnParamTypeNames());

        actions.add(createNewAction(headAction.getName(),parameters, returnParameters, annotations));

        AbstractNativeAction deleteAction = getAction(delete, connector);
        parameters = new ArrayList<>();
        addParameters(parameters, deleteAction.getArgumentTypeNames());
        annotations = new ArrayList<>();
        addAnnotations(annotations, deleteAction.getAnnotations());
        returnParameters = new ArrayList<>();
        addParameters(returnParameters, deleteAction.getReturnParamTypeNames());

        actions.add(createNewAction(deleteAction.getName(),parameters, returnParameters, annotations));

        AbstractNativeAction putAction = getAction(put, connector);
        parameters = new ArrayList<>();
        addParameters(parameters, putAction.getArgumentTypeNames());
        annotations = new ArrayList<>();
        addAnnotations(annotations, putAction.getAnnotations());
        returnParameters = new ArrayList<>();
        addParameters(returnParameters, putAction.getReturnParamTypeNames());

        actions.add(createNewAction(putAction.getName(),parameters, returnParameters, annotations));

        AbstractNativeAction postAction = getAction(post, connector);
        parameters = new ArrayList<>();
        addParameters(parameters, postAction.getArgumentTypeNames());
        annotations = new ArrayList<>();
        addAnnotations(annotations, postAction.getAnnotations());
        returnParameters = new ArrayList<>();
        addParameters(returnParameters, postAction.getReturnParamTypeNames());

        actions.add(createNewAction(postAction.getName(),parameters, returnParameters, annotations));

        AbstractNativeAction patchAction = getAction(patch, connector);
        parameters = new ArrayList<>();
        addParameters(parameters, patchAction.getArgumentTypeNames());
        annotations = new ArrayList<>();
        addAnnotations(annotations, patchAction.getAnnotations());
        returnParameters = new ArrayList<>();
        addParameters(returnParameters, patchAction.getReturnParamTypeNames());

        actions.add(createNewAction(patchAction.getName(),parameters, returnParameters, annotations));

        AbstractNativeAction getAction = getAction(get,connector);
        parameters = new ArrayList<>();
        addParameters(parameters, getAction.getArgumentTypeNames());
        annotations = new ArrayList<>();
        addAnnotations(annotations, getAction.getAnnotations());
        returnParameters = new ArrayList<>();
        addParameters(returnParameters, getAction.getReturnParamTypeNames());

        actions.add(createNewAction(getAction.getName(),parameters, returnParameters, annotations));
    }

    private AbstractNativeAction getAction(SymbolName symbolName, AbstractNativeConnector connector){
        return (AbstractNativeAction)((NativeUnitProxy)connector.resolve(symbolName)).load();
    }

    private Action createNewAction(String name, List<Parameter> params, List<Parameter> returnParams,
                                   List<Annotation> annotations) {
        Action action = new Action();
        action.setName(name);
        action.setParameters(params);
        action.setReturnParams(returnParams);
        action.setAnnotations(annotations);
        return action;
    }

    private Parameter createNewParameter(String type, String name) {
        Parameter parameter = new Parameter();
        parameter.setType(type);
        parameter.setName(name);
        return parameter;
    }

    private Annotation createNewAnnotation(String name, String value) {
        Annotation annotation = new Annotation();
        annotation.setName(name);
        annotation.setValue(value);
        return annotation;
    }

    private Function createNewFunction(String name, List<Annotation> annotations, List<Parameter> params,
                                       List<Parameter> returnParams) {
        Function function = new Function();
        function.setName(name);
        function.setAnnotations(annotations);
        function.setParameters(params);
        function.setReturnParams(returnParams);
        return function;
    }

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

    private static Response.ResponseBuilder setCORSHeaders(Response.ResponseBuilder responseBuilder) {
        return responseBuilder
                .header(ACCESS_CONTROL_ALLOW_ORIGIN_NAME, ACCESS_CONTROL_ALLOW_ORIGIN_VALUE)
                .header(ACCESS_CONTROL_ALLOW_HEADERS_NAME, ACCESS_CONTROL_ALLOW_HEADERS_VALUE)
                .header(ACCESS_CONTROL_ALLOW_METHODS_NAME, ACCESS_CONTROL_ALLOW_METHODS_VALUE);
    }
}
