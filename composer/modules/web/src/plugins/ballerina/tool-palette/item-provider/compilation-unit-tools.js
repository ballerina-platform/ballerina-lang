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
        id: 'object',
        name: 'Object',
        icon: 'struct',
        title: 'Object',
        nodeFactoryMethod: DefaultNodeFactory.createStruct,
        description: 'User-defined record type',
    },
    /* Disable transformer for now
    {
        id: 'transformer',
        name: 'Transformer',
        icon: 'transformer',
        title: 'Transformer',
        nodeFactoryMethod: DefaultNodeFactory.createTransformer,
        description: 'Custom type conversion for transforming data',
    },
    */
];

export default tools;
