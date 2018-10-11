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
        id: 'if',
        name: 'If',
        icon: 'if-else',
        title: 'If',
        nodeFactoryMethod: DefaultNodeFactory.createIf,
        description: 'Provide a way to perform conditional execution',
    },
    {
        id: 'while',
        name: 'While',
        icon: 'while',
        title: 'While',
        nodeFactoryMethod: DefaultNodeFactory.createWhile,
        description: 'Provide a way to execute a series of statements as long as a boolean expression is met',
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
        id: 'foreach',
        name: 'Foreach',
        icon: 'foreach',
        title: 'Foreach',
        nodeFactoryMethod: DefaultNodeFactory.createForeach,
        description: 'Provide a way to iterate',
    },
    /*
    {
        id: 'Fork',
        name: 'Fork',
        icon: 'fork-join',
        title: 'Fork',
        nodeFactoryMethod: DefaultNodeFactory.createForkJoin,
        description: 'Provide a way to replicate a message to any number of parallel workers'
        + ' and have them independently operate on the copies of the message',
    }*/
];

export default tools;
