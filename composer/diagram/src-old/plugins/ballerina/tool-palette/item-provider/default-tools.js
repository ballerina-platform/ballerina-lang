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

import DefaultNodeFactory from './../../model/default-node-factory';

const tools = [
    {
        id: 'function',
        name: 'Main Function',
        icon: 'main-function',
        title: 'Main Function',
        nodeFactoryMethod: DefaultNodeFactory.createMainFunction,
        description: 'Potential entry point for command line execution',
    },
    {
        id: 'http-service',
        name: 'HTTP Service',
        icon: 'http',
        title: 'HTTP Service',
        nodeFactoryMethod: DefaultNodeFactory.createHTTPServiceDef,
        description: 'HTTP server connector allows Ballerina programmers to expose their APIs to HTTP clients',
    },
    {
        id: 'ws-service',
        name: 'WebSocket',
        icon: 'web-service',
        title: 'WebSocket Service',
        nodeFactoryMethod: DefaultNodeFactory.createWSServiceDef,
        description: 'WebSocket server connector is a protocol that provides full-duplex, persistent ' +
        'communication channels over a single TCP connection',
    },
    // ////////////////////////////////////////////////////////////////////////////////////////
    {
        id: 'constructs_seperator',
        name: '',
        seperator: true,
    },
    // ////////////////////////////////////////////////////////////////////////////////////////
    {
        id: 'function',
        name: 'Function',
        icon: 'function',
        title: 'Function',
        nodeFactoryMethod: DefaultNodeFactory.createFunction,
        description: 'Single operation that is intended to be a unit of reusable functionality',
    },
    {
        id: 'connectorDefinition',
        name: 'Connector',
        icon: 'connector',
        title: 'Connector Definition',
        nodeFactoryMethod: DefaultNodeFactory.createConnector,
        description: 'Participant in the integration and is used to interact with an external'
        + ' system or a service defined',
    },
    {
        id: 'struct',
        name: 'Struct',
        icon: 'struct',
        title: 'Struct',
        nodeFactoryMethod: DefaultNodeFactory.createRecord,
        description: 'User-defined record type',
    },
    {
        id: 'worker',
        name: 'Worker',
        icon: 'worker',
        title: 'Worker',
        nodeFactoryMethod: DefaultNodeFactory.createWorker,
        description: 'Programmable actor which is represented on a sequence diagram'
        + ' as a vertical lifeline of logic to be executed.',
    },
    {
        id: 'transformer',
        name: 'Transformer',
        icon: 'transformer',
        title: 'Transformer',
        nodeFactoryMethod: DefaultNodeFactory.createTransformer,
        description: 'Custom type conversion for transforming data',
    },
    {
        id: 'enum',
        name: 'Enum',
        icon: 'enum',
        title: 'Enum',
        nodeFactoryMethod: DefaultNodeFactory.createEnum,
        description: 'User defined enums',
    },
    // ////////////////////////////////////////////////////////////////////////////////////////
    {
        id: 'main_tool_seperator',
        name: '',
        seperator: true,
    },
    // ////////////////////////////////////////////////////////////////////////////////////////
    {
        id: 'VariableDefinition',
        name: 'Variable',
        icon: 'variable',
        title: 'Variable Definition',
        nodeFactoryMethod: DefaultNodeFactory.createVarDefStmt,
        description: 'Statements that can be added and positioned in any order',
    },
    {
        id: 'Assignment',
        name: 'Assignment',
        icon: 'assign',
        title: 'Assignment',
        nodeFactoryMethod: DefaultNodeFactory.createAssignmentStmt,
        description: 'Provide a way to assign a value to a variable accessor',
    },
    // Need a separate icon for the bind
    {
        id: 'Bind',
        name: 'Bind',
        icon: 'uri',
        title: 'Bind',
        nodeFactoryMethod: DefaultNodeFactory.createBindStmt,
        description: 'Provide a way to bind a connector to an endpoint',
    },
    {
        id: 'if',
        name: 'If',
        icon: 'dgm-if-else',
        title: 'If',
        nodeFactoryMethod: DefaultNodeFactory.createIf,
        description: 'Provide a way to perform conditional execution',
    },
    {
        id: 'FunctionInvocation',
        name: 'Function Invoke',
        icon: 'function-invoke',
        title: 'Function Invoke',
        nodeFactoryMethod: DefaultNodeFactory.createInvocation,
        description: 'Provide a way to invoke/call functions',
    },
    {
        id: 'while',
        name: 'While',
        icon: 'dgm-while',
        title: 'While',
        nodeFactoryMethod: DefaultNodeFactory.createWhile,
        description: 'Provide a way to execute a series of statements as long as a boolean expression is met',
    },
    {
        id: 'break',
        name: 'Break',
        icon: 'break',
        title: 'Break',
        nodeFactoryMethod: DefaultNodeFactory.createBreak,
        description: 'Provide a way to terminate the immediately enclosing loop',
    },
    {
        id: 'next',
        name: 'Next',
        icon: 'next',
        title: 'Next',
        nodeFactoryMethod: DefaultNodeFactory.createNext,
        description: 'Provide a way to continue with the immediately enclosing loop',
    },
    {
        id: 'try-catch',
        name: 'Try-Catch',
        icon: 'try-catch',
        title: 'Try-Catch',
        nodeFactoryMethod: DefaultNodeFactory.createTry,
        description: 'Handle the exception by the block after the catch,'
        + ' if any exception occurs while executing the first block of statements',
    },
    {
        id: 'Throw',
        name: 'Throw',
        icon: 'throw',
        title: 'Throw',
        nodeFactoryMethod: DefaultNodeFactory.createThrow,
        description: 'Provide a way to throw errors',
    },
    {
        id: 'Return',
        name: 'Return',
        icon: 'return',
        title: 'Return',
        nodeFactoryMethod: DefaultNodeFactory.createReturn,
        description: 'Evaluate the expression, stops the current function'
        + ', and returns the result of the expression to the caller',
    },
    {
        id: 'WorkerInvocation',
        name: 'Send',
        icon: 'worker-invoke',
        title: 'Worker Invocation',
        nodeFactoryMethod: DefaultNodeFactory.createWorkerInvocation,
        description: 'Provide a way to send a message to a worker',
    },
    {
        id: 'WorkerReply',
        name: 'Receive',
        icon: 'worker-reply',
        title: 'Worker Receive',
        nodeFactoryMethod: DefaultNodeFactory.createWorkerReceive,
        description: 'Provide a way to receive the reply from a worker',
    },
    {
        id: 'Transaction',
        name: 'Transaction',
        icon: 'transaction',
        title: 'Transaction',
        nodeFactoryMethod: DefaultNodeFactory.createTransaction,
        description: 'Series of data manipulation statements that must either'
        + ' fully complete or fully fail, leaving the system in a consistent state',
    },
    {
        id: 'Abort',
        name: 'Abort',
        icon: 'abort',
        title: 'Abort',
        nodeFactoryMethod: DefaultNodeFactory.createAbort,
        description: 'Can be executed after the transaction is rolled back due to any conditions',
    },
    {
        id: 'Fork',
        name: 'Fork',
        icon: 'fork-join',
        title: 'Fork',
        nodeFactoryMethod: DefaultNodeFactory.createForkJoin,
        description: 'Provide a way to replicate a message to any number of parallel workers'
        + ' and have them independently operate on the copies of the message',
    },
    {
        id: 'Namespace',
        name: 'Namespace',
        icon: 'namespace',
        title: 'Namespace',
        nodeFactoryMethod: DefaultNodeFactory.createXmlns,
        description: 'Can be used for xml qualified names',
    },
];

export default {
    name: 'Constructs',
    id: 'constructs-tool-group',
    order: 'horizontal',
    tools,
    gridConfig: true,
};
