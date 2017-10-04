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
import NodeFactory from './../../model/node-factory';
import DefaultNodeFactory from './../../model/default-node-factory';

const tools = [
    {
        id: 'service',
        name: 'HTTP',
        icon: 'http',
        title: 'HTTP Service',
        nodeFactoryMethod: DefaultNodeFactory.createHTTPServiceDef,
        description: 'Http container of resources, each of which defines the logic for'
            + 'handling one type of request',
    },
    {
        id: 'service',
        name: 'WebSocket',
        icon: 'web-service',
        title: 'WS Service',
        nodeFactoryMethod: DefaultNodeFactory.createWSServiceDef,
        description: 'Web Socket container of resources,'
            + 'each of which defines the logic for handling one type of request',
    },
    {
        id: 'function',
        name: 'Main Function',
        icon: 'main-function',
        title: 'Main Function',
        nodeFactoryMethod: DefaultNodeFactory.createMainFunction,
        definition: 'Potential entry point for command line execution',
    },
    //////////////////////////////////////////////////////////////////////////////////////////
    {
        id: 'constructs_seperator',
        name: '',
        seperator: true,
    },
    //////////////////////////////////////////////////////////////////////////////////////////
    {
        id: 'function',
        name: 'Function',
        icon: 'function',
        title: 'Function',
        nodeFactoryMethod: NodeFactory.createFunction,
        description: 'Single operation that is intended to be a unit of reusable functionality',
    },
    {
        id: 'connectorDefinition',
        name: 'Connector',
        icon: 'connector',
        title: 'Connector Definition',
        nodeFactoryMethod: NodeFactory.createConnector,
        description: 'Participant in the integration and is used to interact with an external'
            + 'system or a service defined',
    },
    {
        id: 'connectorAction',
        name: 'Action',
        icon: 'dgm-action',
        title: 'Connector Action',
        nodeFactoryMethod: NodeFactory.createAction,
        description: 'Operation that can be executed against a connector',
    },
    {
        id: 'struct',
        name: 'Struct',
        icon: 'struct',
        title: 'Struct',
        nodeFactoryMethod: NodeFactory.createStruct,
        description: 'User-defined record type',
    },
    {
        id: 'worker',
        name: 'Worker',
        icon: 'worker',
        title: 'Worker',
        nodeFactoryMethod: NodeFactory.createWorker,
        description: 'Programmable actor which is represented on a sequence diagram'
            + 'as a vertical lifeline of logic to be executed.',
    },
    {
        id: 'annotation',
        name: 'Annotation',
        icon: 'annotation',
        title: 'Annotation Definition',
        nodeFactoryMethod: NodeFactory.createAnnotation,
        description: 'Hold factoryArgs data related to the attached code',
    },
    //////////////////////////////////////////////////////////////////////////////////////////
    {
        id: 'main_tool_seperator',
        name: '',
        seperator: true,
    },
    //////////////////////////////////////////////////////////////////////////////////////////
    {
        id: 'VariableDefinition',
        name: 'Variable',
        icon: 'variable',
        title: 'Variable Definition',
        nodeFactoryMethod: DefaultNodeFactory.createVarDefStmt,
        description: 'Statements which can be added anywhere a statement is allowed.'
            + '\n They can be interspersed with other statements in any order',
    },
    {
        id: 'Assignment',
        name: 'Assignment',
        icon: 'assign',
        title: 'Assignment',
        nodeFactoryMethod: DefaultNodeFactory.createAssignmentStmt,
        description: 'Provide a way to assign a value to a variable accessor',
    },
    {
        id: 'if',
        name: 'If',
        icon: 'dgm-if-else',
        title: 'If',
        nodeFactoryMethod: NodeFactory.createIf,
        description: 'Provide a way to perform conditional execution',
    },
    {
        id: 'FunctionInvocation',
        name: 'Function Invoke',
        icon: 'function-invoke',
        title: 'Function Invoke',
        nodeFactoryMethod: NodeFactory.createInvocation,
        description: 'Provide a way to invoke/call functions',
    },
    {
        id: 'while',
        name: 'While',
        icon: 'dgm-while',
        title: 'While',
        nodeFactoryMethod: NodeFactory.createWhile,
        description: 'Provide a way to execute a series of statements as long as a Boolean expression is met',
    },
    {
        id: 'Transform',
        name: 'Transform',
        icon: 'type-converter',
        title: 'Transform',
        nodeFactoryMethod: NodeFactory.createTransform,
        description: 'Transform any chosen variables in the enclosing scope',
    },
    {
        id: 'break',
        name: 'Break',
        icon: 'break',
        title: 'Break',
        nodeFactoryMethod: NodeFactory.createBreak,
        description: 'Provide a way to terminate the immediately enclosing loop',
    },
    {
        id: 'continue',
        name: 'Continue',
        icon: 'continue',
        title: 'Continue',
        nodeFactoryMethod: NodeFactory.createContinue,
        description: 'Provide a way to continue with the immediately enclosing loop',
    },
    {
        id: 'try-catch',
        name: 'Try-Catch',
        icon: 'try-catch',
        title: 'Try-Catch',
        nodeFactoryMethod: NodeFactory.createTry,
        description: 'Handle the exception by the block after the catch,'
            + 'if any exception occurs while executing the first block of statements ',
    },
    {
        id: 'Throw',
        name: 'Throw',
        icon: 'throw',
        title: 'Throw',
        nodeFactoryMethod: NodeFactory.createThrow,
        description: 'Provide a way to throw errors',
    },
    {
        id: 'Return',
        name: 'Return',
        icon: 'return',
        title: 'Return',
        nodeFactoryMethod: NodeFactory.createReturn,
        description: 'Evaluate the expression, stops the current function'
            + ', and returns the result of the expression to the caller',
    },
    {
        id: 'WorkerInvocation',
        name: 'Send',
        icon: 'worker-invoke',
        title: 'Worker Invocation',
        nodeFactoryMethod: NodeFactory.createInvocation,
        description: 'Provide a way to send a message to a worker',
    },
    {
        id: 'WorkerReply',
        name: 'Receive',
        icon: 'worker-reply',
        title: 'Worker Receive',
        nodeFactoryMethod: NodeFactory.createWorkerReceive,
        description: 'Provide a way to receive the reply from a worker',
    },
    {
        id: 'Transaction',
        name: 'Transaction',
        icon: 'transaction',
        title: 'Transaction',
        nodeFactoryMethod: NodeFactory.createAbort,
        description: 'Series of data manipulation statements that must either'
            + ' fully complete or fully fail, leaving the system in a consistent state',
    },
    {
        id: 'Abort',
        name: 'Abort',
        icon: 'abort',
        title: 'Abort',
        nodeFactoryMethod: NodeFactory.createAbort,
        description: 'Can be executed after the transaction is rolled back due to any conditions',
    },
    {
        id: 'Retry',
        name: 'Retry',
        icon: 'refresh',
        title: 'Retry',
        nodeFactoryMethod: NodeFactory.createRetry,
        description: 'Statement which sets the retry count for the transaction when transaction fails',
    },
    {
        id: 'Fork',
        name: 'Fork',
        icon: 'fork-join',
        title: 'Fork',
        nodeFactoryMethod: NodeFactory.createForkJoin,
        description: 'Provide a way to replicate a message to any number of parallel workers'
            + 'and have them independently operate on the copies of the message',
    },
    {
        id: 'Namespace',
        name: 'Namespace',
        icon: 'namespace',
        title: 'Namespace',
        nodeFactoryMethod: NodeFactory.createXmlQname,
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
