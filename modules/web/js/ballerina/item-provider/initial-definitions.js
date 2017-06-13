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

const ToolPalette = [];

const createResourceDefTool = {
    id: 'resource',
    name: 'Resource',
    iconSrc: require('./../../../images/tool-icons/resource.svg'),
    cssClass: 'icon fw fw-resource',
    title: 'Resource',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createResourceDefinition,
};

const createServiceDefTool = {
    id: 'service',
    name: 'Service',
    iconSrc: require('./../../../images/tool-icons/service.svg'),
    cssClass: 'icon fw fw-service',
    title: 'Service',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createServiceDefinition,
};

const createFunctionDefTool = {
    id: 'function',
    name: 'Function',
    iconSrc: require('./../../../images/tool-icons/function.svg'),
    cssClass: 'icon fw fw-function',
    title: 'Function',
    nodeFactoryMethod: BallerinaASTFactory.createFunctionDefinition,
};

const createMainFunctionDefTool = {
    id: 'function',
    name: 'Main Function',
    meta: {
        functionName: 'main',
        functionArgs: [
            {
                type: 'message',
                identifier: 'm',
            },
        ],
    },
    iconSrc: require('./../../../images/tool-icons/main-function.svg'),
    cssClass: 'icon fw fw-main-function',
    title: 'Main Function',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createMainFunctionDefinition,
};

const createConnectorDefTool = {
    id: 'connectorDefinition',
    name: 'Connector',
    iconSrc: require('./../../../images/tool-icons/connector.svg'),
    cssClass: 'icon fw fw-connector',
    title: 'Connector Definition',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createConnectorDefinition,
};

const createConnectorActionTool = {
    id: 'connectorAction',
    name: 'Action',
    iconSrc: require('./../../../images/tool-icons/action.svg'),
    cssClass: 'icon fw fw-dgm-action',
    title: 'Connector Action',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createConnectorAction,
};

const createStructsDefTool = {
    id: 'struct',
    name: 'Struct',
    iconSrc: require('./../../../images/tool-icons/struct.svg'),
    cssClass: 'icon fw fw-struct',
    title: 'Struct',
    nodeFactoryMethod: BallerinaASTFactory.createStructDefinition,
};

const createWorkerDecTool = {
    id: 'worker',
    name: 'Worker',
    iconSrc: require('./../../../images/tool-icons/worker.svg'),
    cssClass: 'icon fw fw-worker',
    title: 'Worker',
    nodeFactoryMethod: BallerinaASTFactory.createWorkerDeclaration,
};

const createAnnotationDefTool = {
    id: 'annotation',
    name: 'Annotation',
    iconSrc: require('./../../../images/tool-icons/annotation-black.svg'),
    cssClass: 'icon fw fw-annotation',
    title: 'Annotation Definition',
    nodeFactoryMethod: BallerinaASTFactory.createAnnotationDefinition,
};

const mainToolDefArray = [createServiceDefTool, createResourceDefTool, createFunctionDefTool,
    createMainFunctionDefTool, createConnectorDefTool, createConnectorActionTool, createStructsDefTool,
    createWorkerDecTool, createAnnotationDefTool];

const elements = new ToolGroup({
    toolGroupName: 'Elements',
    toolOrder: 'horizontal',
    toolGroupID: 'main-tool-group',
    toolDefinitions: mainToolDefArray,
});

const createIfStatementTool = {
    id: 'if',
    name: 'If',
    iconSrc: require('./../../../images/tool-icons/dgm-if-else.svg'),
    cssClass: 'icon fw fw-dgm-if-else',
    title: 'If',
    nodeFactoryMethod: BallerinaASTFactory.createIfElseStatement,
};

const createWhileStatementTool = {
    id: 'while',
    name: 'While',
    iconSrc: require('./../../../images/tool-icons/dgm-while.svg'),
    cssClass: 'icon fw fw-dgm-while',
    title: 'While',
    nodeFactoryMethod: BallerinaASTFactory.createWhileStatement,
};

const createBreakStatementTool = {
    id: 'break',
    name: 'Break',
    iconSrc: require('./../../../images/tool-icons/break.svg'),
    cssClass: 'icon fw fw-break',
    title: 'Break',
    nodeFactoryMethod: BallerinaASTFactory.createBreakStatement,
};

const createTryCatchStatementTool = {
    id: 'try-catch',
    name: 'Try-Catch',
    iconSrc: require('./../../../images/tool-icons/try-catch.svg'),
    cssClass: 'icon fw fw-try-catch',
    title: 'Try-Catch',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createTryCatchStatement,
};

const createAssignmentExpressionTool = {
    id: 'Assignment',
    name: 'Assignment',
    iconSrc: require('./../../../images/tool-icons/assign.svg'),
    cssClass: 'icon fw fw-assign',
    title: 'Assignment',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createAggregatedAssignmentStatement,
};

const createTransformStatementTool = {
    id: 'Transform',
    name: 'Transform',
    iconSrc: require('./../../../images/tool-icons/type-converter.svg'),
    cssClass: 'icon fw fw-type-converter',
    title: 'Transform',
    nodeFactoryMethod: BallerinaASTFactory.createTransformStatement,
};

const createJoinStatementTool = {
    id: 'Fork',
    name: 'Fork',
    iconSrc: require('./../../../images/tool-icons/fork-join.svg'),
    cssClass: 'icon fw fw-fork-join',
    title: 'Fork',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createForkJoinStatement,
};

// TODO: change the icon with the new one
const createVariableDefinitionStatementTool = {
    id: 'VariableDefinition',
    name: 'Variable',
    iconSrc: require('./../../../images/variable.svg'),
    cssClass: 'icon fw fw-variable',
    title: 'Variable Definition',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createVariableDefinitionStatement,
};

const createFunctionInvocationTool = {
    id: 'FunctionInvocation',
    name: 'Function Invoke',
    iconSrc: require('./../../../images/tool-icons/function-invoke.svg'),
    cssClass: 'icon fw fw-function-invoke',
    title: 'Function Invoke',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createAggregatedFunctionInvocationStatement,
};

const createReplyStatementTool = {
    id: 'Reply',
    name: 'Reply',
    iconSrc: require('./../../../images/tool-icons/reply.svg'),
    cssClass: 'icon fw fw-reply',
    title: 'Reply',
    nodeFactoryMethod: BallerinaASTFactory.createReplyStatement,
};

const createReturnStatementTool = {
    id: 'Return',
    name: 'Return',
    iconSrc: require('./../../../images/tool-icons/return.svg'),
    cssClass: 'icon fw fw-return',
    title: 'Return',
    nodeFactoryMethod: BallerinaASTFactory.createReturnStatement,
};

const createWorkerInvocationStatementTool = {
    id: 'WorkerInvocation',
    name: 'Send',
    iconSrc: require('./../../../images/tool-icons/worker-invoke.svg'),
    cssClass: 'icon fw fw-worker-invoke',
    title: 'Worker Invocation',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createWorkerInvocationStatement,
};

const createWorkerReplyStatementTool = {
    id: 'WorkerReply',
    name: 'Receive',
    iconSrc: require('./../../../images/tool-icons/worker-reply.svg'),
    cssClass: 'icon fw fw-worker-reply',
    title: 'Worker Receive',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createWorkerReplyStatement,
};

const createThrowStatementTool = {
    id: 'Throw',
    name: 'Throw',
    iconSrc: require('./../../../images/tool-icons/throw.svg'),
    cssClass: 'icon fw fw-throw',
    title: 'Throw',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createThrowStatement,
};

const createAbortStatementTool = {
    id: 'Abort',
    name: 'Abort',
    iconSrc: require('./../../../images/tool-icons/abort.svg'),
    cssClass: 'icon fw fw-abort',
    title: 'Abort',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createAbortStatement,
};

const createTransactionAbortedStatementTool = {
    id: 'Transaction',
    name: 'Transaction',
    iconSrc: require('./../../../images/tool-icons/transaction.svg'),
    cssClass: 'icon fw fw-transaction',
    title: 'Transaction',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createTransactionAbortedStatement,
};

const statementToolDefArray = [createIfStatementTool, createAssignmentExpressionTool,
    createVariableDefinitionStatementTool, createFunctionInvocationTool, createReturnStatementTool,
    createReplyStatementTool, createWhileStatementTool, createBreakStatementTool, createTryCatchStatementTool,
    createThrowStatementTool, createWorkerInvocationStatementTool, createWorkerReplyStatementTool,
    createTransformStatementTool, createJoinStatementTool, createAbortStatementTool, createTransactionAbortedStatementTool];

// Create statements tool group
const statements = new ToolGroup({
    toolGroupName: 'Statements',
    toolGroupID: 'statements-tool-group',
    toolOrder: 'horizontal',
    toolDefinitions: statementToolDefArray,
});

const seperator = {
    id: 'constructs_seperator',
    seperator: true,
};

// creating a one gourp for constructs
const constructsToolDefArray = _.union(mainToolDefArray, [seperator], statementToolDefArray);

constructsToolDefArray.forEach((tool) => {
    const icon = document.createElement('img');
    icon.setAttribute('src', tool.iconSrc);
    tool.icon = icon;
});

const constructs = new ToolGroup({
    toolGroupName: 'Constructs',
    toolGroupID: 'constructs-tool-group',
    toolOrder: 'horizontal',
    toolDefinitions: constructsToolDefArray,
    gridConfig: true,
});

ToolPalette.push(constructs);
export default ToolPalette;
