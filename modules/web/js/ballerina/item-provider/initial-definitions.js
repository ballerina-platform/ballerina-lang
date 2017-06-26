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
import _ from 'lodash';
import BallerinaASTFactory from './../ast/ballerina-ast-factory';
import ToolGroup from './../tool-palette/tool-group';
import DefaultBallerinaASTFactory from '../ast/default-ballerina-ast-factory';

const ToolPalette = [];

const createResourceDefTool = {
    id: 'resource',
    name: 'Resource',
    cssClass: 'icon fw fw-resource',
    title: 'Resource',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createResourceDefinition,
};

const createServiceDefTool = {
    id: 'service',
    name: 'Service',
    cssClass: 'icon fw fw-service',
    title: 'Service',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createServiceDefinition,
};

const createFunctionDefTool = {
    id: 'function',
    name: 'Function',
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
    cssClass: 'icon fw fw-main-function',
    title: 'Main Function',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createMainFunctionDefinition,
};

const createConnectorDefTool = {
    id: 'connectorDefinition',
    name: 'Connector',
    cssClass: 'icon fw fw-connector',
    title: 'Connector Definition',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createConnectorDefinition,
};

const createConnectorActionTool = {
    id: 'connectorAction',
    name: 'Action',
    cssClass: 'icon fw fw-dgm-action',
    title: 'Connector Action',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createConnectorAction,
};

const createStructsDefTool = {
    id: 'struct',
    name: 'Struct',
    cssClass: 'icon fw fw-struct',
    title: 'Struct',
    nodeFactoryMethod: BallerinaASTFactory.createStructDefinition,
};

const createWorkerDecTool = {
    id: 'worker',
    name: 'Worker',
    cssClass: 'icon fw fw-worker',
    title: 'Worker',
    nodeFactoryMethod: BallerinaASTFactory.createWorkerDeclaration,
};

const createAnnotationDefTool = {
    id: 'annotation',
    name: 'Annotation',
    cssClass: 'icon fw fw-annotation',
    title: 'Annotation Definition',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createAnnotationDefinition,
};

const mainToolDefArray = [createServiceDefTool, createResourceDefTool, createFunctionDefTool,
    createMainFunctionDefTool, createConnectorDefTool, createConnectorActionTool, createStructsDefTool,
    createWorkerDecTool, createAnnotationDefTool];

const createIfStatementTool = {
    id: 'if',
    name: 'If',
    cssClass: 'icon fw fw-dgm-if-else',
    title: 'If',
    nodeFactoryMethod: BallerinaASTFactory.createIfElseStatement,
};

const createWhileStatementTool = {
    id: 'while',
    name: 'While',
    cssClass: 'icon fw fw-dgm-while',
    title: 'While',
    nodeFactoryMethod: BallerinaASTFactory.createWhileStatement,
};

const createBreakStatementTool = {
    id: 'break',
    name: 'Break',
    cssClass: 'icon fw fw-break',
    title: 'Break',
    nodeFactoryMethod: BallerinaASTFactory.createBreakStatement,
};

const createTryCatchStatementTool = {
    id: 'try-catch',
    name: 'Try-Catch',
    cssClass: 'icon fw fw-try-catch',
    title: 'Try-Catch',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createTryCatchStatement,
};

const createAssignmentExpressionTool = {
    id: 'Assignment',
    name: 'Assignment',
    cssClass: 'icon fw fw-assign',
    title: 'Assignment',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createAggregatedAssignmentStatement,
};

const createTransformStatementTool = {
    id: 'Transform',
    name: 'Transform',
    cssClass: 'icon fw fw-type-converter',
    title: 'Transform',
    nodeFactoryMethod: BallerinaASTFactory.createTransformStatement,
};

const createJoinStatementTool = {
    id: 'Fork',
    name: 'Fork',
    cssClass: 'icon fw fw-fork-join',
    title: 'Fork',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createForkJoinStatement,
};

// TODO: change the icon with the new one
const createVariableDefinitionStatementTool = {
    id: 'VariableDefinition',
    name: 'Variable',
    cssClass: 'icon fw fw-variable',
    title: 'Variable Definition',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createVariableDefinitionStatement,
};

const createFunctionInvocationTool = {
    id: 'FunctionInvocation',
    name: 'Function Invoke',
    cssClass: 'icon fw fw-function-invoke',
    title: 'Function Invoke',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createAggregatedFunctionInvocationStatement,
};

const createReplyStatementTool = {
    id: 'Reply',
    name: 'Reply',
    cssClass: 'icon fw fw-reply',
    title: 'Reply',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createReplyStatement,
};

const createReturnStatementTool = {
    id: 'Return',
    name: 'Return',
    cssClass: 'icon fw fw-return',
    title: 'Return',
    nodeFactoryMethod: BallerinaASTFactory.createReturnStatement,
};

const createWorkerInvocationStatementTool = {
    id: 'WorkerInvocation',
    name: 'Send',
    cssClass: 'icon fw fw-worker-invoke',
    title: 'Worker Invocation',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createWorkerInvocationStatement,
};

const createWorkerReplyStatementTool = {
    id: 'WorkerReply',
    name: 'Receive',
    cssClass: 'icon fw fw-worker-reply',
    title: 'Worker Receive',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createWorkerReplyStatement,
};

const createThrowStatementTool = {
    id: 'Throw',
    name: 'Throw',
    cssClass: 'icon fw fw-throw',
    title: 'Throw',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createThrowStatement,
};

const createAbortStatementTool = {
    id: 'Abort',
    name: 'Abort',
    cssClass: 'icon fw fw-abort',
    title: 'Abort',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createAbortStatement,
};

const createTransactionAbortedStatementTool = {
    id: 'Transaction',
    name: 'Transaction',
    cssClass: 'icon fw fw-transaction',
    title: 'Transaction',
    nodeFactoryMethod: DefaultBallerinaASTFactory.createTransactionAbortedStatement,
};

const statementToolDefArray = [createIfStatementTool, createAssignmentExpressionTool,
    createVariableDefinitionStatementTool, createFunctionInvocationTool, createReturnStatementTool,
    createReplyStatementTool, createWhileStatementTool, createBreakStatementTool, createTryCatchStatementTool,
    createThrowStatementTool, createWorkerInvocationStatementTool, createWorkerReplyStatementTool,
    createTransformStatementTool, createJoinStatementTool, createAbortStatementTool,
    createTransactionAbortedStatementTool];

const seperator = {
    id: 'constructs_seperator',
    name: '',
    seperator: true,
};

// creating a one gourp for constructs
const constructsToolDefArray = _.union(mainToolDefArray, [seperator], statementToolDefArray);

const constructs = new ToolGroup({
    toolGroupName: 'Constructs',
    toolGroupID: 'constructs-tool-group',
    toolOrder: 'horizontal',
    toolDefinitions: constructsToolDefArray,
    gridConfig: true,
});

ToolPalette.push(constructs);
export default ToolPalette;
