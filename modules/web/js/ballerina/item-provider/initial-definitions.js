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
define(['log', 'jquery', './../ast/ballerina-ast-factory', './../tool-palette/tool-group', './../ast/defaults-added-ballerina-ast-factory'],
    function (log, $, BallerinaASTFactory, ToolGroup, DefaultsAddedBallerinaASTFactory) {

        var ToolPalette = [];

        var createResourceDefTool = {
            id: "resource",
            name: "Resource",
            icon: "images/tool-icons/resource.svg",
            title: "Resource",
            nodeFactoryMethod: DefaultsAddedBallerinaASTFactory.createResourceDefinition
        };

        var createServiceDefTool = {
            id: "service",
            name: "Service",
            icon: "images/tool-icons/service.svg",
            title: "Service",
            nodeFactoryMethod: DefaultsAddedBallerinaASTFactory.createServiceDefinition
        };

        var createFunctionDefTool = {
            id: "function",
            name: "Function",
            icon: "images/tool-icons/function.svg",
            title: "Function",
            nodeFactoryMethod:  BallerinaASTFactory.createFunctionDefinition
        };

        var createMainFunctionDefTool = {
            id: "function",
            name: "Main Function",
            meta: {
                functionName: "main",
                functionArgs: [
                    {
                        type: "message",
                        identifier: "m"
                    }
                ]
            },
            icon: "images/tool-icons/main-function.svg",
            title: "Main Function",
            nodeFactoryMethod:  BallerinaASTFactory.createMainFunctionDefinition
        };

        var createConnectorDefTool = {
            id: "connectorDefinition",
            name: "Connector Definition",
            icon: "images/tool-icons/connector.svg",
            title: "Connector Definition",
            nodeFactoryMethod:  BallerinaASTFactory.createConnectorDefinition
        };

        var createConnectorActionTool = {
            id: "connectorAction",
            name: "Connector Action",
            icon: "images/tool-icons/action.svg",
            title: "Connector Action",
            nodeFactoryMethod:  BallerinaASTFactory.createConnectorAction
        };

        var createStructsDefTool = {
            id: "struct",
            name: "Struct",
            icon: "images/tool-icons/struct.svg",
            title: "Struct",
            nodeFactoryMethod: BallerinaASTFactory.createStructDefinition
        };

        var createTypeMapperDefTool = {
            id: "typeMapper",
            name: "Type Mapper",
            icon: "images/tool-icons/type-converter.svg",
            title: "Type Mapper",
            nodeFactoryMethod: BallerinaASTFactory.createTypeMapperDefinition
        };

        var createWorkerDecTool = {
            id: "worker",
            name: "Worker",
            icon: "images/tool-icons/worker.svg",
            title: "Worker",
            nodeFactoryMethod: BallerinaASTFactory.createWorkerDeclaration
        };

        var mainToolDefArray = [createServiceDefTool, createResourceDefTool, createFunctionDefTool,
            createMainFunctionDefTool, createConnectorDefTool, createConnectorActionTool, createStructsDefTool,
            createTypeMapperDefTool];

        var elements = new ToolGroup({
            toolGroupName: "Elements",
            toolOrder: "horizontal",
            toolGroupID: "main-tool-group",
            toolDefinitions: mainToolDefArray
        });
        ToolPalette.push(elements);

        var createIfStatementTool = {
            id: "if",
            name: "If",
            icon: "images/tool-icons/dgm-if-else.svg",
            title: "If",
            nodeFactoryMethod: BallerinaASTFactory.createIfElseStatement
        };

        var createWhileStatementTool = {
            id: "while",
            name: "While",
            icon: "images/tool-icons/dgm-while.svg",
            title: "While",
            nodeFactoryMethod: BallerinaASTFactory.createWhileStatement
        };

        var createAssignmentExpressionTool = {
            id: "Assignment",
            name: "Assignment",
            icon: "images/tool-icons/assign.svg",
            title: "Assignment",
            nodeFactoryMethod: BallerinaASTFactory.createAggregatedAssignmentStatement
        };

        // TODO: change the icon with the new one
        var createVariableDefinitionStatementTool = {
            id: "VariableDefinition",
            name: "VariableDefinition",
            icon: "images/variable.svg",
            title: "Variable Definition",
            nodeFactoryMethod: DefaultsAddedBallerinaASTFactory.createVariableDefinitionStatement
        };

        var createFunctionInvocationTool = {
            id: "FunctionInvocation",
            name: "FunctionInvocation",
            icon: "images/tool-icons/function-invoke.svg",
            title: "Function Invocation",
            nodeFactoryMethod: BallerinaASTFactory.createAggregatedFunctionInvocationStatement
        };

        var createReplyStatementTool = {
            id: "Reply",
            name: "Reply",
            icon: "images/tool-icons/reply.svg",
            title: "Reply",
            nodeFactoryMethod: BallerinaASTFactory.createReplyStatement
        };

        var createReturnStatementTool = {
            id: "Return",
            name: "Return",
            icon: "images/tool-icons/return.svg",
            title: "Return",
            nodeFactoryMethod: BallerinaASTFactory.createReturnStatement
        };

        var createWorkerInvocationStatementTool = {
            id: "WorkerInvoke",
            name: "Worker Invoke",
            icon: "images/tool-icons/return.svg",
            title: "Worker Invoke",
            nodeFactoryMethod: BallerinaASTFactory.createWorkerInvokeStatement
        };

        var statementToolDefArray = [createIfStatementTool, createAssignmentExpressionTool,
            createVariableDefinitionStatementTool,  createFunctionInvocationTool, createReturnStatementTool,
            createReplyStatementTool, createWhileStatementTool];

        // Create statements tool group
        var statements = new ToolGroup({
            toolGroupName: "Statements",
            toolGroupID: "statements-tool-group",
            toolOrder: "horizontal",
            toolDefinitions: statementToolDefArray
        });
        ToolPalette.push(statements);

        return ToolPalette;
});
