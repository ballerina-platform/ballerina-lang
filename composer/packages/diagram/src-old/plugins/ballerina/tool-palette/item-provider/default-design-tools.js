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
        id: 'endpointDeclaration',
        name: 'Endpoint',
        icon: 'endpoint',
        title: 'Endpoint Declaration',
        nodeFactoryMethod: DefaultNodeFactory.createEndpoint,
        description: 'Participant in the integration and is used to interact with an external'
        + ' system or a service defined',
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
        id: 'connectorDefinition',
        name: 'Connector',
        icon: 'connector',
        title: 'Connector Definition',
        nodeFactoryMethod: DefaultNodeFactory.createConnector,
        description: 'Participant in the integration and is used to interact with an external'
        + ' system or a service defined',
    },
    // ////////////////////////////////////////////////////////////////////////////////////////
    {
        id: 'main_tool_seperator',
        name: '',
        seperator: true,
    },
    // ////////////////////////////////////////////////////////////////////////////////////////
    {
        id: 'ActionInvocation',
        name: 'Action Invoke',
        icon: 'action',
        title: 'Action Invoke',
        nodeFactoryMethod: DefaultNodeFactory.createConnectorActionInvocationAssignmentStatement,
        description: 'Provide a way to invoke/call endpoints',
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
        id: 'while',
        name: 'While',
        icon: 'dgm-while',
        title: 'While',
        nodeFactoryMethod: DefaultNodeFactory.createWhile,
        description: 'Provide a way to execute a series of statements as long as a boolean expression is met',
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
];

export const TopLevelElements = tools;

export default {
    name: 'Constructs',
    id: 'constructs-tool-group',
    order: 'horizontal',
    tools,
    gridConfig: true,
};
