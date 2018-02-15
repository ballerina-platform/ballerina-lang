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

import { LayoutConstants, AlertConstants } from '@ballerina-lang/composer-core';
import { BallerinaPlugin } from '@ballerina-lang/composer-ballerina-plugin';
import { DebuggerPlugin } from '@ballerina-lang/composer-debugger-plugin';
import { HelpPlugin, HelpPluginConstants } from '@ballerina-lang/composer-help-plugin';
import { TryItPlugin } from '@ballerina-lang/composer-try-it-plugin';
import { WelcomeTabPlugin, WelcomeTabConstants } from '@ballerina-lang/composer-welcome-tab-plugin';
import { ImportSwaggerPlugin } from '@ballerina-lang/composer-import-swagger-plugin';
import { DiagramExportPlugin } from '@ballerina-lang/composer-export-diagram-plugin';
import { CodeExplorerPlugin } from '@ballerina-lang/composer-code-explorer-plugin';
import { ImportStructPlugin } from '@ballerina-lang/composer-import-struct-plugin';

export default {
    app: {
        plugins: [
            BallerinaPlugin,
            CodeExplorerPlugin,
            DebuggerPlugin,
            HelpPlugin,
            TryItPlugin,
            WelcomeTabPlugin,
            ImportSwaggerPlugin,
            ImportStructPlugin,
            DiagramExportPlugin,
        ],
    },
    // provide plugin specific configs - if any.
    // plugin-id will be the key
    pluginConfigs: {
        [AlertConstants.PLUGIN_ID]: {
            container: 'alert-container',
        },
        [LayoutConstants.PLUGIN_ID]: {
            container: 'app-container',
            dialogContainer: 'dialog-container',
        },
        [HelpPluginConstants.PLUGIN_ID]: {
            issue_tracker_url: 'https://github.com/ballerinalang/composer/issues/',
            example_url: 'https://ballerinalang.org/docs/by-example/',
            api_reference_url: 'https://ballerinalang.org/docs/api/0.96.1/',
        },
        [WelcomeTabConstants.PLUGIN_ID]: {
            userGuide: 'http://ballerinalang.org/docs/user-guide/',
            samples: [
                {
                    name: 'Echo Service',
                    isFile: true,
                    path: '/samples/echoService/echoService.bal',
                    image: 'preview_echoService',
                },
                {
                    name: 'Hello World',
                    isFile: true,
                    path: '/samples/helloWorld/helloWorld.bal',
                    image: 'preview_helloWorld',
                },
                {
                    name: 'Passthrough Service',
                    isFile: false,
                    openFolder: false,
                    folder: '/samples/passthroughService/passthroughservice',
                    path: '/samples/passthroughService/passthroughservice/samples/nyseStockQuoteService.bal',
                    image: 'preview_passthroughService',
                },
                {
                    name: 'Transformer',
                    isFile: false,
                    openFolder: true,
                    folder: '/samples/transformer',
                    path: '/samples/transformer/StructTransformer.bal',
                    image: 'preview_transformStmt',
                },
                {
                    name: 'Service Chaining',
                    isFile: false,
                    openFolder: false,
                    folder: '/samples/serviceChaining/',
                    path: '/samples/serviceChaining/servicechaining/samples/ATMLocatorService.bal',
                    image: 'preview_servicechaining',
                },
                {
                    name: 'RESTful Service',
                    isFile: false,
                    openFolder: false,
                    folder: '/samples/restfulService/restfulservice',
                    path: '/samples/restfulService/restfulservice/samples/productsService.bal',
                    image: 'preview_restfulService',
                },
                {
                    name: 'Routing Services',
                    isFile: false,
                    openFolder: false,
                    folder: '/samples/routingServices',
                    path: '/samples/routingServices/routingServices/samples/contentBasedRoutingService.bal',
                    image: 'preview_routingServices',
                },
                {
                    name: 'WebSocket',
                    isFile: false,
                    openFolder: true,
                    folder: '/samples/websocket',
                    path: '/samples/websocket/echoserver/server/EchoServer.bal',
                    image: 'preview_websocket',
                }],
        },
    },
};
