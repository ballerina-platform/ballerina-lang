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
import log from 'log';
import $ from 'jquery';
import BallerinaASTFactory from './../ast/ballerina-ast-factory';
import ToolGroup from './../tool-palette/tool-group';
import DefaultBallerinaASTFactory from '../ast/default-ballerina-ast-factory';

var ToolPalette = [];

var createResourceDefTool = {
    id: "resource",
    name: "Resource",
    iconSrc: require("./../../../images/tool-icons/resource.svg"),
    cssClass: "icon fw fw-resource",
    title: "Resource",
    nodeFactoryMethod: DefaultBallerinaASTFactory.createResourceDefinition
};

var createServiceDefTool = {
    id: "service",
    name: "Service",
    iconSrc: require("./../../../images/tool-icons/service.svg"),
    cssClass: "icon fw fw-service",
    title: "Service",
    nodeFactoryMethod: DefaultBallerinaASTFactory.createServiceDefinition
};

var createFunctionDefTool = {
    id: "function",
    name: "Function",
    iconSrc: require("./../../../images/tool-icons/function.svg"),
    cssClass: "icon fw fw-function",
    title: "Function",
    nodeFactoryMethod: BallerinaASTFactory.createFunctionDefinition
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
    iconSrc: require("./../../../images/tool-icons/main-function.svg"),
    cssClass: "icon fw fw-main-function",
    title: "Main Function",
    nodeFactoryMethod: DefaultBallerinaASTFactory.createMainFunctionDefinition
};

var createConnectorDefTool = {
    id: "connectorDefinition",
    name: "Connector",
    iconSrc: require("./../../../images/tool-icons/connector.svg"),
    cssClass: "icon fw fw-connector",
    title: "Connector Definition",
    nodeFactoryMethod: DefaultBallerinaASTFactory.createConnectorDefinition
};

var createConnectorActionTool = {
    id: "connectorAction",
    name: "Action",
    iconSrc: require("./../../../images/tool-icons/action.svg"),
    cssClass: "icon fw fw-dgm-action",
    title: "Connector Action",
    nodeFactoryMethod: DefaultBallerinaASTFactory.createConnectorAction
};

var createStructsDefTool = {
    id: "struct",
    name: "Struct",
    iconSrc: require("./../../../images/tool-icons/struct.svg"),
    cssClass: "icon fw fw-struct",
    title: "Struct",
    nodeFactoryMethod: BallerinaASTFactory.createStructDefinition
};

var createWorkerDecTool = {
    id: "worker",
    name: "Worker",
    iconSrc: require("./../../../images/tool-icons/worker.svg"),
    cssClass: "icon fw fw-worker",
    title: "Worker",
    nodeFactoryMethod: BallerinaASTFactory.createWorkerDeclaration
};

var createAnnotationDefTool = {
    id: "annotation",
    name: "Annotation",
    iconSrc: require("./../../../images/tool-icons/annotation-black.svg"),
    cssClass: "icon fw fw-annotation",
    title: "Annotation Definition",
    nodeFactoryMethod: BallerinaASTFactory.createAnnotationDefinition
};

var mainToolDefArray = [createServiceDefTool, createResourceDefTool, createFunctionDefTool,
    createMainFunctionDefTool, createConnectorDefTool, createConnectorActionTool, createStructsDefTool,
    createWorkerDecTool, createAnnotationDefTool];

var elements = new ToolGroup({
    toolGroupName: "Elements",
    toolOrder: "horizontal",
    toolGroupID: "main-tool-group",
    toolDefinitions: mainToolDefArray
});

var createIfStatementTool = {
    id: "if",
    name: "If",
    iconSrc: require("./../../../images/tool-icons/dgm-if-else.svg"),
    cssClass: "icon fw fw-dgm-if-else",
    title: "If",
    nodeFactoryMethod: BallerinaASTFactory.createIfElseStatement
};

var createWhileStatementTool = {
    id: "while",
    name: "While",
    iconSrc: require("./../../../images/tool-icons/dgm-while.svg"),
    cssClass: "icon fw fw-dgm-while",
    title: "While",
    nodeFactoryMethod: BallerinaASTFactory.createWhileStatement
};

var createBreakStatementTool = {
    id: "break",
    name: "Break",
    iconSrc: require("./../../../images/tool-icons/break.svg"),
    cssClass: "icon fw fw-break",
    title: "Break",
    nodeFactoryMethod: BallerinaASTFactory.createBreakStatement
};

var createTryCatchStatementTool = {
    id: "try-catch",
    name: "Try-Catch",
    iconSrc: require("./../../../images/tool-icons/try-catch.svg"),
    cssClass: "icon fw fw-try-catch",
    title: "Try-Catch",
    nodeFactoryMethod: DefaultBallerinaASTFactory.createTryCatchStatement
};

var createAssignmentExpressionTool = {
    id: "Assignment",
    name: "Assignment",
    iconSrc: require("./../../../images/tool-icons/assign.svg"),
    cssClass: "icon fw fw-assign",
    title: "Assignment",
    nodeFactoryMethod: DefaultBallerinaASTFactory.createAggregatedAssignmentStatement
};

var createTransformStatementTool = {
    id: "Transform",
    name: "Transform",
    iconSrc: require("./../../../images/tool-icons/type-converter.svg"),
    cssClass: "icon fw fw-type-converter",
    title: "Transform",
    nodeFactoryMethod: BallerinaASTFactory.createTransformStatement
};

// TODO: change the icon with the new one
var createVariableDefinitionStatementTool = {
    id: "VariableDefinition",
    name: "Variable",
    iconSrc: require("./../../../images/variable.svg"),
    cssClass: "icon fw fw-variable",
    title: "Variable Definition",
    nodeFactoryMethod: DefaultBallerinaASTFactory.createVariableDefinitionStatement
};

var createFunctionInvocationTool = {
    id: "FunctionInvocation",
    name: "Function Invoke",
    iconSrc: require("./../../../images/tool-icons/function-invoke.svg"),
    cssClass: "icon fw fw-function-invoke",
    title: "Function Invoke",
    nodeFactoryMethod: DefaultBallerinaASTFactory.createAggregatedFunctionInvocationStatement
};

var createReplyStatementTool = {
    id: "Reply",
    name: "Reply",
    iconSrc: require("./../../../images/tool-icons/reply.svg"),
    cssClass: "icon fw fw-reply",
    title: "Reply",
    nodeFactoryMethod: BallerinaASTFactory.createReplyStatement
};

var createReturnStatementTool = {
    id: "Return",
    name: "Return",
    iconSrc: require("./../../../images/tool-icons/return.svg"),
    cssClass: "icon fw fw-return",
    title: "Return",
    nodeFactoryMethod: BallerinaASTFactory.createReturnStatement
};

var createWorkerInvocationStatementTool = {
    id: "WorkerInvocation",
    name: "Send",
    iconSrc: require("./../../../images/tool-icons/worker-invoke.svg"),
    cssClass: "icon fw fw-worker-invoke",
    title: "Worker Invocation",
    nodeFactoryMethod: BallerinaASTFactory.createWorkerInvocationStatement
};

var createWorkerReplyStatementTool = {
    id: "WorkerReply",
    name: "Receive",
    iconSrc: require("./../../../images/tool-icons/worker-reply.svg"),
    cssClass: "icon fw fw-worker-reply",
    title: "Worker Receive",
    nodeFactoryMethod: BallerinaASTFactory.createWorkerReplyStatement
};

var createThrowStatementTool = {
    id: "Throw",
    name: "Throw",
    iconSrc: require("./../../../images/tool-icons/throw.svg"),
    cssClass: "icon fw fw-throw",
    title: "Throw",
    nodeFactoryMethod: DefaultBallerinaASTFactory.createThrowStatement
};

var createAbortStatementTool = {
    id: "Abort",
    name: "Abort",
    iconSrc: require("./../../../images/tool-icons/throw.svg"),
    cssClass: "icon fw fw-throw",
    title: "Abort",
    nodeFactoryMethod: DefaultBallerinaASTFactory.createAbortStatement
};

var createTransactionAbortedStatementTool = {
    id: "Transaction",
    name: "Transaction",
    iconSrc: require("./../../../images/tool-icons/try-catch.svg"),
    cssClass: "icon fw fw-try-catch",
    title: "Transaction",
    nodeFactoryMethod: DefaultBallerinaASTFactory.createTransactionAbortedStatement
};

var statementToolDefArray = [createIfStatementTool, createAssignmentExpressionTool,
    createVariableDefinitionStatementTool, createFunctionInvocationTool, createReturnStatementTool,
    createReplyStatementTool, createWhileStatementTool, createBreakStatementTool, createTryCatchStatementTool,
    createThrowStatementTool, createWorkerInvocationStatementTool, createWorkerReplyStatementTool,
    createTransformStatementTool, createAbortStatementTool, createTransactionAbortedStatementTool];

// Create statements tool group
var statements = new ToolGroup({
    toolGroupName: "Statements",
    toolGroupID: "statements-tool-group",
    toolOrder: "horizontal",
    toolDefinitions: statementToolDefArray
});

var seperator = {
    id: "constructs_seperator",
    seperator: true
};

//creating a one gourp for constructs
var constructsToolDefArray = _.union(mainToolDefArray, [seperator], statementToolDefArray);

constructsToolDefArray.forEach(tool => {
    var icon = document.createElement('img');
    icon.setAttribute('src', tool.iconSrc);
    tool.icon = icon;
});

var constructs = new ToolGroup({
    toolGroupName: "Constructs",
    toolGroupID: "constructs-tool-group",
    toolOrder: "horizontal",
    toolDefinitions: constructsToolDefArray,
    gridConfig: true
});

ToolPalette.push(constructs);
export default ToolPalette;
