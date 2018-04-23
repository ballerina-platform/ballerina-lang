/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import { PLUGIN_ID as LAYOUT_MANAGER_PLUGIN_ID } from './core/layout/constants';
import { PLUGIN_ID as ALERT_PLUGIN_ID } from './core/alert/constants';
import BallerinaPlugin from './plugins/ballerina/plugin';
import DebuggerPlugin from './plugins/debugger/plugin';
import HelpPlugin from './plugins/help/plugin';
import TryItPlugin from './plugins/try-it/plugin';
import WelcomeTabPlugin from './plugins/welcome-tab/plugin';
import ImportSwaggerPlugin from './plugins/import-swagger/plugin';
import ExportDiagramPlugin from './plugins/export-diagram/plugin';
import CodeExplorerPlugin from './plugins/code-explorer/plugin';
import { PLUGIN_ID as HELP_PLUGIN_ID } from './plugins/help/constants';
import { WELCOME_TAB_PLUGIN_ID } from './plugins/welcome-tab/constants';
import ImportStructPlugin from './plugins/import-struct/plugin';
import ToolsPlugin from './plugins/tools/plugin';
import TextEditorPlugin from './plugins/text-file-editor/plugin';

export default {
    app: {
        plugins: [
            BallerinaPlugin,
            CodeExplorerPlugin,
            DebuggerPlugin,
            HelpPlugin,
            TryItPlugin,
            ToolsPlugin,
            WelcomeTabPlugin,
            ImportSwaggerPlugin,
            ImportStructPlugin,
            ExportDiagramPlugin,
            TextEditorPlugin,
        ],
    },
    // provide plugin specific configs - if any.
    // plugin-id will be the key
    pluginConfigs: {
        [ALERT_PLUGIN_ID]: {
            container: 'alert-container',
        },
        [LAYOUT_MANAGER_PLUGIN_ID]: {
            container: 'app-container',
            dialogContainer: 'dialog-container',
        },
        [HELP_PLUGIN_ID]: {
            issue_tracker_url: 'https://github.com/ballerina-lang/ballerina/issues?q=is%3Aopen+is%3Aissue+label%3Acomponent%2FComposer',
            example_url: 'https://ballerinalang.org/docs/by-example/',
            api_reference_url: 'https://ballerinalang.org/docs/api/0.970.0/',
        },
        [WELCOME_TAB_PLUGIN_ID]: {
            userGuide: 'https://ballerina.io/learn/by-example/',
            samples: [
                {
                    title: 'Integration Introduction Examples',
                    column: 0,
                    samples: [
                        { name: 'Hello World Service', url: 'hello-world-service' },
                        { name: 'JSON Transformation', url: 'transform-json' },
                        { name: 'JSON to XML Conversion', url: 'json-to-xml-conversion' },
                        { name: 'Circuit Breaker', url: 'http-circuit-breaker' },
                        { name: 'Load Balancing', url: 'http-load-balancer' },
                        { name: 'Failover', url: 'http-failover' },
                        { name: 'Tables and SQL', url: 'sql-queries-on-tables' }],
                },
                {
                    title: 'Integration Reference',
                    column: 0,
                    samples: [
                        { name: 'Content-based Routing', url: 'content-based-routing' },
                        { name: 'Websockets', url: 'websocket-basic-sample' },
                        { name: 'Distributed Transactions', url: 'distributed-transactions' }],
                },
                {
                    title: 'HTTP/HTTPS',
                    column: 0,
                    samples: [
                        { name: 'Sessions', url: 'http-sessions' },
                        { name: 'Client Connector', url: 'http-client-connector' },
                        { name: 'Redirects', url: 'http-redirects' },
                        { name: 'Base and Path', url: 'base-path-and-path' },
                        { name: 'Query Path Matrix Param', url: 'query-path-matrix-param' },
                        { name: 'Produces/Consumes', url: 'produces-consumes' },
                        { name: 'Header Based Routing', url: 'header-based-routing' },
                        { name: 'Server-Client', url: 'https-server-client-connectors' },
                        { name: 'Passthrough', url: 'passthrough' },
                        { name: 'Mutual SSL', url: 'mutual-ssl' },
                        { name: 'Filter Connector', url: 'filter-connector' },
                        { name: 'HTTPS Server Connector', url: 'https-server-connector' },
                        { name: 'HTTP Disable Chunking', url: 'http-disable-chunking' },
                        { name: 'HTTP Trace Logs', url: 'http-trace-logs' },
                        { name: 'HTTP to WebSocket Upgrade', url: 'http-to-websocket-upgrade' }],
                },
                {
                    title: 'Language Reference',
                    column: 1,
                    samples: [
                        { name: 'Hello World - Main', url: 'hello-world' },
                        { name: 'Hello World Parallel', url: 'hello-world-parallel' },
                        { name: 'Functions', url: 'functions' },
                        { name: 'Async', url: 'async' },
                        { name: 'If Else', url: 'if-else' },
                        { name: 'While', url: 'while' },
                        { name: 'Transactions', url: 'transactions' },
                        { name: 'Errors', url: 'errors' },
                        { name: 'Throw', url: 'throw' },
                        { name: 'Try/Catch/Finally', url: 'try-catch-finally' },
                        { name: 'Fork/Join', url: 'fork-join' },
                        { name: 'Worker', url: 'worker' },
                        { name: 'Worker Interaction', url: 'worker-interaction' },
                        { name: 'Function Pointers', url: 'function-pointers' },
                        { name: 'Lambda', url: 'lambda' },
                        { name: 'Transformer', url: 'transformers' },
                        { name: 'For Each', url: 'foreach' },
                        { name: 'Fork Join Condition Some', url: 'fork-join-condition-some' },
                        { name: 'Fork Join Variable Access', url: 'fork-join-variable-access' }],
                },
                {
                    title: 'Type Reference',
                    column: 1,
                    samples: [
                        { name: 'Value Types', url: 'value-types' },
                        { name: 'Strings', url: 'strings' },
                        { name: 'String Template', url: 'string-template' },
                        { name: 'Blob Type', url: 'blob-type' },
                        { name: 'Maps', url: 'maps' },
                        { name: 'XML', url: 'xml' },
                        { name: 'XML Namespaces', url: 'xml-namespaces' },
                        { name: 'JSON', url: 'json' },
                        { name: 'JSON Literals', url: 'json-literals' },
                        { name: 'JSON Struct/Map Conversion', url: 'json-struct-map-conversion' },
                        { name: 'Constrained JSON', url: 'constrained-json' },
                        { name: 'XML to JSON Conversion', url: 'xml-to-json-conversion' },
                        { name: 'Any Type', url: 'any-type' },
                        { name: 'Var', url: 'var' },
                        { name: 'Identifier Literals', url: 'identifier-literals' },
                        { name: 'Constants', url: 'constants' },
                        { name: 'Global Variables', url: 'global-variables' },
                        { name: 'Arrays', url: 'arrays' },
                        { name: 'Array of Arrays', url: 'array-of-arrays' },
                        { name: 'Structs', url: 'structs' },
                        { name: 'Lengthof', url: 'lengthof' },
                        { name: 'Type Casting', url: 'type-casting' },
                        { name: 'Type Conversion', url: 'type-conversion' },
                        { name: 'Ternary Operators', url: 'ternary' }],
                },
                {
                    title: 'Deployment Basics',
                    column: 2,
                    samples: [
                        { name: 'Tracing', url: 'observe-tracing' }],
                },
                {
                    title: 'Unit Testing',
                    column: 2,
                    samples: [
                        { name: 'Assertions', url: 'testerina-assertions' },
                        { name: 'Before After Suite', url: 'testerina-before-after-suite' },
                        { name: 'Before After', url: 'testerina-before-after' },
                        { name: 'Before Each', url: 'testerina-before-each' },
                        { name: 'Data Provider', url: 'testerina-data-provider' },
                        { name: 'Depends On', url: 'testerina-depends-on' },
                        { name: 'Function Mocks', url: 'testerina-function-mocks' },
                        { name: 'Groups', url: 'testerina-groups' }],
                },
                {
                    title: 'Message Broker',
                    column: 2,
                    samples: [
                        { name: 'MB Simple Queue Message Producer', url: 'mb-simple-queue-message-producer' },
                        { name: 'MB Simple Queue Message Receiver', url: 'mb-simple-queue-message-receiver' },
                        { name: 'MB Simple Topic Message Publisher', url: 'mb-simple-topic-message-publisher' },
                        { name: 'MB Simple Topic Message Subscriber', url: 'mb-simple-topic-message-subscriber' }],
                },
                {
                    title: 'WebSockets',
                    column: 2,
                    samples: [
                        { name: 'WebSocket Chat Application', url: 'websocket-chat-application' },
                        { name: 'WebSocket Proxy Server', url: 'websocket-proxy-server' }],
                },
                {
                    title: 'JMS',
                    column: 2,
                    samples: [
                        { name: 'Durable Topic Message Subscriber', url: 'jms-durable-topic-message-subscriber' },
                        { name: 'Queue Message Producer Transactional', url: 'jms-queue-message-producer-transactional' },
                        { name: 'Queue Message Producer', url: 'jms-queue-message-producer' },
                        { name: 'Queue Message Receiver Sync Client Ack', url: 'jms-queue-message-receiver-sync-client-ack' },
                        { name: 'Queue Message Receiver Sync', url: 'jms-queue-message-receiver-sync' },
                        { name: 'Queue Message Receiver with Client Acknowledgment', url: 'jms-queue-message-receiver-with-client-acknowledgment' },
                        { name: 'Queue Message Receiver', url: 'jms-queue-message-receiver' },
                        { name: 'Simple Durable Topic Message Subscriber', url: 'jms-simple-durable-topic-message-subscriber' },
                        { name: 'Simple Queue Message Producer', url: 'jms-simple-queue-message-producer' },
                        { name: 'Simple Queue Message Receiver', url: 'jms-simple-queue-message-receiver' },
                        { name: 'Simple Topic Message Producer', url: 'jms-simple-topic-message-producer' },
                        { name: 'Simple Topic Message Subscriber', url: 'jms-simple-topic-message-subscriber' },
                        { name: 'Topic Message Producer', url: 'jms-topic-message-producer' },
                        { name: 'Topic Message Subscriber', url: 'jms-topic-message-subscriber' }],
                },
                {
                    title: 'Standard Library',
                    column: 3,
                    samples: [
                        { name: 'Date Time', url: 'date-time' },
                        { name: 'Caching', url: 'caching' },
                        { name: 'Config API', url: 'config-api' },
                        { name: 'File API', url: 'file-api' },
                        { name: 'Byte I/O', url: 'byte-i-o' },
                        { name: 'Character I/O', url: 'character-i-o' },
                        { name: 'Record I/O', url: 'record-i-o' },
                        { name: 'Log API', url: 'log-api' },
                        { name: 'Math', url: 'math-functions' },
                        { name: 'SQL', url: 'sql-queries-on-tables' },
                        { name: 'Task Timer', url: 'task-timer' },
                        { name: 'Task Appointment', url: 'task-appointment' }],
                },
                {
                    title: 'Security',
                    column: 3,
                    samples: [
                        { name: 'Taint Checking', url: 'taint-checking' }],
                },
                {
                    title: 'Data Management',
                    column: 3,
                    samples: [
                        { name: 'SQL Client Connector', url: 'sql-connector' }],
                },
                {
                    title: 'Streams',
                    column: 3,
                    samples: [
                        { name: 'Streams', url: 'streams' },
                        { name: 'Streams within Services', url: 'streams-within-services' },
                        { name: 'Streams Sequences', url: 'streams-sequences' },
                        { name: 'Streams Patterns', url: 'streams-patterns' },
                        { name: 'Streams Join', url: 'streams-join' }],
                },
                {
                    title: 'gRPC',
                    column: 3,
                    samples: [
                        { name: 'Bidirectional Streaming', url: 'grpc-bidirectional-streaming' },
                        { name: 'Client Streaming', url: 'grpc-client-streaming' },
                        { name: 'Secured Unary', url: 'grpc-secured-unary' },
                        { name: 'Server Streaming', url: 'grpc-server-streaming' },
                        { name: 'Unary Blocking', url: 'grpc-unary-blocking' },
                        { name: 'Unary Non Blocking', url: 'grpc-unary-non-blocking' }],
                },
            ]
            ,
        },
    },
};
