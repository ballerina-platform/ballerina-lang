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
import org.wso2.ballerina.tooling.service.workspace.api.NotFoundException;
import org.wso2.ballerina.tooling.service.workspace.api.PackagesApiService;
import org.wso2.ballerina.tooling.service.workspace.model.Action;
import org.wso2.ballerina.tooling.service.workspace.model.Annotation;
import org.wso2.ballerina.tooling.service.workspace.model.Connector;
import org.wso2.ballerina.tooling.service.workspace.model.Function;
import org.wso2.ballerina.tooling.service.workspace.model.ModelPackage;
import org.wso2.ballerina.tooling.service.workspace.model.Parameter;

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

    @Override
    public Response packagesGet(Integer limit, Integer offset, String query, String accept, String ifNoneMatch)
            throws NotFoundException {
        return Response.ok(getAllPackages().values()).header("Access-Control-Allow-Origin", '*').build();
    }

    @Override
    public Response packagesPackageNameGet(String packageName, String accept, String ifNoneMatch,
                                           String ifModifiedSince) throws NotFoundException {
        Map<String, ModelPackage> allPackages = getAllPackages();
        ModelPackage modelPackage = allPackages.get(packageName);
        if (modelPackage == null) {
            return Response.status(Response.Status.NOT_FOUND).header("Access-Control-Allow-Origin", '*').build();
        }
        return Response.ok(modelPackage).header("Access-Control-Allow-Origin", '*').build();
    }

    @Override
    public Response packagesPost(String contentType) throws NotFoundException {
        return null;
    }

    private Map<String, ModelPackage> getAllPackages() {
        Map<String, ModelPackage> packages = new HashMap<>();
        ModelPackage modelPackage = new ModelPackage();

        modelPackage.setName("ballerina.net.http");

        //adding function setStatusCode
        List<Parameter> params = new ArrayList<>();
        params.add(createNewParameter("message", "message"));
        params.add(createNewParameter("int", "statusCode"));
        List<Parameter> returnParams = new ArrayList<>();
        returnParams.add(createNewParameter("int", null));
        List<Annotation> annotations = new ArrayList<>();
        annotations.add(createNewAnnotation("Description", ""));
        annotations.add(createNewAnnotation("Param", "message"));
        annotations.add(createNewAnnotation("Param", "status code"));
        modelPackage.addFunctionsItem(createNewFunction("setStatusCode", annotations, params, returnParams));

        //adding function setStatusCode
        params = new ArrayList<>();
        params.add(createNewParameter("message", "message"));
        returnParams = new ArrayList<>();
        returnParams.add(createNewParameter("int", null));
        annotations = new ArrayList<>();
        annotations.add(createNewAnnotation("Description", ""));
        annotations.add(createNewAnnotation("Param", "message"));
        annotations.add(createNewAnnotation("Return", "status code"));
        modelPackage.addFunctionsItem(createNewFunction("getStatusCode", annotations, params, returnParams));

        //adding function setContentLength
        params = new ArrayList<>();
        params.add(createNewParameter("message", "message"));
        params.add(createNewParameter("int", "contentLength"));
        returnParams = new ArrayList<>();
        returnParams.add(createNewParameter("int", null));
        annotations = new ArrayList<>();
        annotations.add(createNewAnnotation("Description", ""));
        annotations.add(createNewAnnotation("Param", "message"));
        annotations.add(createNewAnnotation("Param", "content length"));
        modelPackage.addFunctionsItem(createNewFunction("setContentLength", annotations, params, returnParams));

        //adding function setContentLength
        params = new ArrayList<>();
        params.add(createNewParameter("message", "message"));
        returnParams = new ArrayList<>();
        returnParams.add(createNewParameter("int", null));
        annotations = new ArrayList<>();
        annotations.add(createNewAnnotation("Description", ""));
        annotations.add(createNewAnnotation("Param", "message"));
        annotations.add(createNewAnnotation("Return", "content length"));
        modelPackage.addFunctionsItem(createNewFunction("getContentLength", annotations, params, returnParams));

        //adding function setReasonPhrase
        params = new ArrayList<>();
        params.add(createNewParameter("message", "message"));
        params.add(createNewParameter("string", "reasonPhrase"));
        returnParams = new ArrayList<>();
        returnParams.add(createNewParameter("int", null));
        annotations = new ArrayList<>();
        annotations.add(createNewAnnotation("Description", ""));
        annotations.add(createNewAnnotation("Param", "message"));
        annotations.add(createNewAnnotation("Param", "reason phrase"));
        modelPackage.addFunctionsItem(createNewFunction("setReasonPhrase", annotations, params, returnParams));

        //adding action GET
        List<Action> actions = new ArrayList<>();
        List<Parameter> actionParams = new ArrayList<>();
        actionParams.add(createNewParameter("HttpConnector", "h"));
        actionParams.add(createNewParameter("string", "path"));
        actionParams.add(createNewParameter("message", "m"));
        returnParams = new ArrayList<>();
        returnParams.add(createNewParameter("message", null));
        annotations = new ArrayList<>();
        annotations.add(createNewAnnotation("Description", ""));
        actions.add(createNewAction("get", actionParams, returnParams, annotations));

        //adding action POST
        actionParams = new ArrayList<>();
        actionParams.add(createNewParameter("HttpConnector", "h"));
        actionParams.add(createNewParameter("string", "path"));
        actionParams.add(createNewParameter("message", "m"));
        returnParams = new ArrayList<>();
        returnParams.add(createNewParameter("message", null));
        annotations = new ArrayList<>();
        annotations.add(createNewAnnotation("Description", ""));
        actions.add(createNewAction("post", actionParams, returnParams, annotations));

        //adding action PUT
        actionParams = new ArrayList<>();
        actionParams.add(createNewParameter("HttpConnector", "h"));
        actionParams.add(createNewParameter("string", "path"));
        actionParams.add(createNewParameter("message", "m"));
        returnParams = new ArrayList<>();
        returnParams.add(createNewParameter("message", null));
        annotations = new ArrayList<>();
        annotations.add(createNewAnnotation("Description", ""));
        actions.add(createNewAction("put", actionParams, returnParams, annotations));

        //adding action DELETE
        actionParams = new ArrayList<>();
        actionParams.add(createNewParameter("HttpConnector", "h"));
        actionParams.add(createNewParameter("string", "path"));
        actionParams.add(createNewParameter("message", "m"));
        returnParams = new ArrayList<>();
        returnParams.add(createNewParameter("message", null));
        annotations = new ArrayList<>();
        annotations.add(createNewAnnotation("Description", ""));
        actions.add(createNewAction("delete", actionParams, returnParams, annotations));

        modelPackage.addConnectorsItem(createNewConnector("HTTPConnector", annotations, actions, params));
        packages.put(modelPackage.getName(), modelPackage);
        return packages;
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
                                         List<Parameter> params) {
        Connector connector = new Connector();
        connector.setName(name);
        connector.setActions(actions);
        connector.setParameters(params);
        connector.setAnnotations(annotations);
        return connector;
    }
}
