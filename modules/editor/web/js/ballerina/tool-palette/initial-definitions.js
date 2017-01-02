/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
define(['log', 'jquery', './../ast/ballerina-ast-factory', './tool-group'],
    function (log, $, BallerinaASTFactory, ToolGroup) {

        var initialToolGroups = [];

        var createResourceDefTool = {
            id: "resource",
            name: "Resource",
            icon: "images/tool-icons/dgm-resource.svg",
            title: "Resource",
            nodeFactoryMethod: BallerinaASTFactory.createResourceDefinition
        };

        var createServiceDefTool = {
            id: "service",
            name: "Service",
            icon: "images/tool-icons/rest-service.svg",
            title: "Service",
            nodeFactoryMethod: BallerinaASTFactory.createServiceDefinition
        };

        var createFunctionDefTool = {
            id: "function",
            name: "Function",
            icon: "images/tool-icons/function.svg",
            title: "Function",
            nodeFactoryMethod:  BallerinaASTFactory.createFunctionDefinition
        };

        var mainToolDefArray = [createServiceDefTool, createResourceDefTool, createFunctionDefTool];

        var mainToolGroup = new ToolGroup({
            toolGroupName: "Elements",
            toolGroupID: "main-tool-group",
            toolDefinitions: mainToolDefArray
        });

        initialToolGroups.push(mainToolGroup);


        var createIfStatementTool = {
            id: "if",
            name: "If",
            icon: "images/tool-icons/dgm-if-else.svg",
            title: "If",
            nodeFactoryMethod: BallerinaASTFactory.createIfElseStatement
        };

        //var createTryCatchStatementTool = {
        //    id: "TryCatch",
        //    name: "TryCatch",
        //    icon: "images/tool-icons/trycatch.svg",
        //    title: "TryCatch",
        //    nodeFactoryMethod: BallerinaASTFactory.createTryCatchStatement
        //};

        var createAssignmentExpressionTool = {
            id: "Assignment",
            name: "Assignment",
            icon: "images/tool-icons/assign.svg",
            title: "Assignment",
            nodeFactoryMethod: BallerinaASTFactory.createAssignmentStatement
        };

        var createFunctionInvocationTool = {
            id: "FunctionInvocation",
            name: "FunctionInvocation",
            icon: "images/tool-icons/actioninvoke.svg",
            title: "FunctionInvocation",
            nodeFactoryMethod: BallerinaASTFactory.createFunctionInvocationStatement
        };

        var createReplyStatementTool = {
            id: "Reply",
            name: "Reply",
            icon: "images/tool-icons/left-arrow.svg",
            title: "Reply",
            nodeFactoryMethod: BallerinaASTFactory.createReplyStatement
        };

        var statementToolDefArray = [createIfStatementTool, createAssignmentExpressionTool,
            createFunctionInvocationTool, createReplyStatementTool];

        // Create statements tool group
        var statementsToolGroup = new ToolGroup({
            toolGroupName: "Statements",
            toolGroupID: "statements-tool-group",
            toolDefinitions: statementToolDefArray
        });

        initialToolGroups.push(statementsToolGroup);

        // Create functions tool group
        var functionsToolGroup = new ToolGroup({
            toolGroupName: "Functions",
            toolGroupID: "functions-tool-group",
            toolDefinitions: []
        });
        initialToolGroups.push(functionsToolGroup);

        // Create connectors tool group
        var connectorsToolGroup = new ToolGroup({
            toolGroupName: "Connectors",
            toolGroupID: "connectors-tool-group",
            toolDefinitions: []
        });
        initialToolGroups.push(connectorsToolGroup);

        var createGetActionTool = {
            id: "get",
            name: "Get",
            icon: "images/tool-icons/dgm-resource.svg",
            title: "Get",
            meta: {
                action: "Get"
            },
            nodeFactoryMethod: BallerinaASTFactory.createActionInvocationExpression
        };

        var createPostActionTool = {
            id: "post",
            name: "Post",
            icon: "images/tool-icons/rest-service.svg",
            title: "Post",
            meta: {
                action: "Post"
            },
            nodeFactoryMethod: BallerinaASTFactory.createActionInvocationExpression
        };

        var createPutActionTool = {
            id: "put",
            name: "Put",
            icon: "images/tool-icons/rest-service.svg",
            title: "Put",
            meta: {
                action: "Put"
            },
            nodeFactoryMethod: BallerinaASTFactory.createActionInvocationExpression
        };

        var createDeleteActionTool = {
            id: "delete",
            name: "Delete",
            icon: "images/tool-icons/rest-service.svg",
            title: "Delete",
            meta: {
                action: "Delete"
            },
            nodeFactoryMethod: BallerinaASTFactory.createActionInvocationExpression
        };

        var createPatchActionTool = {
            id: "patch",
            name: "Patch",
            icon: "images/tool-icons/rest-service.svg",
            title: "Patch",
            meta: {
                action: "Patch"
            },
            nodeFactoryMethod: BallerinaASTFactory.createActionInvocationExpression
        };

        var httpConnectorToolArray = [createGetActionTool, createPostActionTool, createPutActionTool, createDeleteActionTool, createPatchActionTool];

        // Create http-connectors tool group
        var httpConnectorsToolGroup = new ToolGroup({
            toolGroupName: "HTTP-Connector Actions",
            toolGroupID: "http-connector-tool-group",
            toolDefinitions: httpConnectorToolArray
        });
        initialToolGroups.push(httpConnectorsToolGroup);

        return initialToolGroups;
});
