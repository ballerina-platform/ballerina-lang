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
import NodeFactory from './../../model/node-factory';
import FragmentUtils from './../../utils/fragment-utils';
import DefaultNodeFactory from './../../model/default-node-factory';

const ToolPalette = [];

const createhttpServiceDefTool = {
    id: 'service',
    name: 'HTTP',
    factoryArgs: {
        protocolPkgName: 'http',
        protocolPkgPath: 'ballerina.net.http',
        resources: [
            {
                parameters: [
                    {
                        type: 'http:Request',
                        value: 'req',
                    },
                    {
                        type: 'http:Response',
                        value: 'resp',
                    },
                ],
            },
        ],
    },
    icon: 'http',
    title: 'HTTP Service',
    nodeFactoryMethod: NodeFactory.createService,
    description: 'Http container of resources, each of which defines the logic for handling one type of request',
};

const createwsServiceDefTool = {
    id: 'service',
    name: 'WebSocket',
    factoryArgs: {
        protocolPkgName: 'ws',
        protocolPkgPath: 'ballerina.net.ws',
        resources: [
            {
                resourceName: 'onOpen',
                parameters: [
                    {
                        type: 'ws:Connection',
                        value: 'conn',
                    },
                ],
            },
            {
                resourceName: 'onTextMessage',
                parameters: [
                    {
                        type: 'ws:Connection',
                        value: 'conn',
                    },
                    {
                        type: 'ws:TextFrame',
                        value: 'frame',
                    },
                ],
            },
            {
                resourceName: 'onClose',
                parameters: [
                    {
                        type: 'ws:Connection',
                        value: 'conn',
                    },
                    {
                        type: 'ws:CloseFrame',
                        value: 'frame',
                    },
                ],
            },
        ],
    },
    icon: 'web-service',
    title: 'WS Service',
    nodeFactoryMethod: NodeFactory.createService,
    description: 'Web Socket container of resources, each of which defines the logic for handling one type of request',
};

const createjmsServiceDefTool = {
    id: 'service',
    name: 'jms',
    factoryArgs: {
        protocolPkgName: 'jms',
        protocolPkgPath: 'ballerina.net.jms',
        resources: [
            {
                parameters: [
                    {
                        type: 'jms:request',
                        value: 'req',
                    },
                    {
                        type: 'jms:response',
                        value: 'resp',
                    },
                ],
            },
        ],
    },
    icon: 'jaxws',
    title: 'JMS Service',
    nodeFactoryMethod: NodeFactory.createService,
    description: 'JMS container of resources, each of which defines the logic for handling one type of request',
};

const createMainFunctionDefTool = {
    id: 'function',
    name: 'Main Function',
    factoryArgs: {
        functionName: 'main',
        functionArgs: [
            {
                type: 'message',
                identifier: 'm',
            },
        ],
    },
    icon: 'main-function',
    title: 'Main Function',
    nodeFactoryMethod: DefaultNodeFactory.createMainFunction,
    definition: 'Potential entry point for command line execution',
};

const serviceToolDefArray = [createhttpServiceDefTool, createwsServiceDefTool, createMainFunctionDefTool];

const createFunctionDefTool = {
    id: 'function',
    name: 'Function',
    icon: 'function',
    title: 'Function',
    nodeFactoryMethod: NodeFactory.createFunction,
    description: 'Single operation that is intended to be a unit of reusable functionality',
};

const createConnectorDefTool = {
    id: 'connectorDefinition',
    name: 'Connector',
    icon: 'connector',
    title: 'Connector Definition',
    nodeFactoryMethod: NodeFactory.createConnector,
    description: 'Participant in the integration and is used to interact with an external system or a service defined',
};

const createConnectorActionTool = {
    id: 'connectorAction',
    name: 'Action',
    icon: 'dgm-action',
    title: 'Connector Action',
    nodeFactoryMethod: NodeFactory.createAction,
    description: 'Operation that can be executed against a connector',
};

const createStructsDefTool = {
    id: 'struct',
    name: 'Struct',
    icon: 'struct',
    title: 'Struct',
    nodeFactoryMethod: NodeFactory.createStruct,
    description: 'User-defined record type',
};

const createWorkerDecTool = {
    id: 'worker',
    name: 'Worker',
    icon: 'worker',
    title: 'Worker',
    nodeFactoryMethod: NodeFactory.createWorker,
    description: 'Programmable actor which is represented on a sequence diagram as a vertical lifeline of logic to be executed.',
};

const createAnnotationDefTool = {
    id: 'annotation',
    name: 'Annotation',
    icon: 'annotation',
    title: 'Annotation Definition',
    nodeFactoryMethod: NodeFactory.createAnnotation,
    description: 'Hold factoryArgs data related to the attached code',
};

const mainToolDefArray = [/* createServiceDefTool, createResourceDefTool, */
    createFunctionDefTool, createConnectorDefTool, createConnectorActionTool, createStructsDefTool,
    createWorkerDecTool, createAnnotationDefTool];

const createIfStatementTool = {
    id: 'if',
    name: 'If',
    icon: 'dgm-if-else',
    title: 'If',
    nodeFactoryMethod: NodeFactory.createIf,
    description: 'Provide a way to perform conditional execution',
};

const createWhileStatementTool = {
    id: 'while',
    name: 'While',
    icon: 'dgm-while',
    title: 'While',
    nodeFactoryMethod: NodeFactory.createWhile,
    description: 'Provide a way to execute a series of statements as long as a Boolean expression is met',
};

const createBreakStatementTool = {
    id: 'break',
    name: 'Break',
    icon: 'break',
    title: 'Break',
    nodeFactoryMethod: NodeFactory.createBreak,
    description: 'Provide a way to terminate the immediately enclosing loop',
};

const createContinueStatementTool = {
    id: 'continue',
    name: 'Continue',
    icon: 'continue',
    title: 'Continue',
    nodeFactoryMethod: NodeFactory.createContinue,
    description: 'Provide a way to continue with the immediately enclosing loop',
};

const createTryCatchStatementTool = {
    id: 'try-catch',
    name: 'Try-Catch',
    icon: 'try-catch',
    title: 'Try-Catch',
    nodeFactoryMethod: NodeFactory.createTry,
    description: 'Handle the exception by the block after the catch, if any exception occurs while executing the first block of statements ',
};

const createAssignmentExpressionTool = {
    id: 'Assignment',
    name: 'Assignment',
    icon: 'assign',
    title: 'Assignment',
    nodeFactoryMethod: NodeFactory.createAssignment,
    description: 'Provide a way to assign a value to a variable accessor',
};

const createTransformStatementTool = {
    id: 'Transform',
    name: 'Transform',
    icon: 'type-converter',
    title: 'Transform',
    nodeFactoryMethod: NodeFactory.createTransform,
    description: 'Transform any chosen variables in the enclosing scope',
};

const createJoinStatementTool = {
    id: 'Fork',
    name: 'Fork',
    icon: 'fork-join',
    title: 'Fork',
    nodeFactoryMethod: NodeFactory.createForkJoin,
    description: 'Provide a way to replicate a message to any number of parallel workers and have them independently operate on the copies of the message',
};

// TODO: change the icon with the new one
const createVariableDefinitionStatementTool = {
    id: 'VariableDefinition',
    name: 'Variable',
    icon: 'variable',
    title: 'Variable Definition',
    nodeFactoryMethod: NodeFactory.createVariableDef,
    description: 'Statements which can be added anywhere a statement is allowed. \n They can be interspersed with other statements in any order',
};

const createFunctionInvocationTool = {
    id: 'FunctionInvocation',
    name: 'Function Invoke',
    icon: 'function-invoke',
    title: 'Function Invoke',
    nodeFactoryMethod: NodeFactory.createInvocation,
    description: 'Provide a way to invoke/call functions',
};

const createReturnStatementTool = {
    id: 'Return',
    name: 'Return',
    icon: 'return',
    title: 'Return',
    nodeFactoryMethod: NodeFactory.createReturn,
    description: 'Evaluate the expression, stops the current function, and returns the result of the expression to the caller',
};

const createWorkerInvocationStatementTool = {
    id: 'WorkerInvocation',
    name: 'Send',
    icon: 'worker-invoke',
    title: 'Worker Invocation',
    nodeFactoryMethod: NodeFactory.createInvocation,
    description: 'Provide a way to send a message to a worker',
};

const createWorkerReplyStatementTool = {
    id: 'WorkerReply',
    name: 'Receive',
    icon: 'worker-reply',
    title: 'Worker Receive',
    nodeFactoryMethod: NodeFactory.createWorkerReceive,
    description: 'Provide a way to receive the reply from a worker',
};

const createThrowStatementTool = {
    id: 'Throw',
    name: 'Throw',
    icon: 'throw',
    title: 'Throw',
    nodeFactoryMethod: NodeFactory.createThrow,
    description: 'Provide a way to throw errors',
};

const createAbortStatementTool = {
    id: 'Abort',
    name: 'Abort',
    icon: 'abort',
    title: 'Abort',
    nodeFactoryMethod: NodeFactory.createAbort,
    description: 'Can be executed after the transaction is rolled back due to any conditions',
};

const createNamespaceDeclarationStatementTool = {
    id: 'Namespace',
    name: 'Namespace',
    icon: 'namespace',
    title: 'Namespace',
    nodeFactoryMethod: NodeFactory.createXmlQname,
    description: 'Can be used for xml qualified names',
};

const createTransactionAbortedStatementTool = {
    id: 'Transaction',
    name: 'Transaction',
    icon: 'transaction',
    title: 'Transaction',
    nodeFactoryMethod: NodeFactory.createAbort,
    description: 'Series of data manipulation statements that must either fully complete or fully fail, leaving the system in a consistent state',
};

const createRetryStatementTool = {
    id: 'Retry',
    name: 'Retry',
    icon: 'refresh',
    title: 'Retry',
    nodeFactoryMethod: NodeFactory.createRetry,
    description: 'Statement which sets the retry count for the transaction when transaction fails',
};

const statementToolDefArray = [
    createVariableDefinitionStatementTool,
    createAssignmentExpressionTool,

    createIfStatementTool,
    createFunctionInvocationTool,

    createWhileStatementTool,
    createTransformStatementTool,

    createBreakStatementTool,
    createContinueStatementTool,

    createTryCatchStatementTool,
    createThrowStatementTool,

    createReturnStatementTool,
    // createReplyStatementTool,

    createWorkerInvocationStatementTool,
    createWorkerReplyStatementTool,

    createTransactionAbortedStatementTool,
    createAbortStatementTool,
    createRetryStatementTool,

    createJoinStatementTool,
    createNamespaceDeclarationStatementTool,
];

const seperator = {
    id: 'constructs_seperator',
    name: '',
    seperator: true,
};

const seperator2 = {
    id: 'main_tool_seperator',
    name: '',
    seperator: true,
};

// creating a one gourp for constructs
const constructsToolDefArray = _.union(serviceToolDefArray, [seperator], mainToolDefArray,
    [seperator2], statementToolDefArray);

const constructs = {
    name: 'Constructs',
    id: 'constructs-tool-group',
    order: 'horizontal',
    tools: constructsToolDefArray,
    gridConfig: true,
};

ToolPalette.push(constructs);
export default ToolPalette;
