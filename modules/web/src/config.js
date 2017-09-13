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
import BallerinaPlugin from './plugins/ballerina/plugin';
import DebuggerPlugin from './plugins/debugger/plugin';
import HelpPlugin from './plugins/help/plugin';
import TryItPlugin from './plugins/try-it/plugin';
import WelcomeTabPlugin from './plugins/welcome-tab/plugin';
import { PLUGIN_ID as HELP_PLUGIN_ID } from './plugins/help/constants';
import { WELCOME_TAB_PLUGIN_ID } from './plugins/welcome-tab/constants';

export default {
    app: {
        plugins: [
            BallerinaPlugin,
            DebuggerPlugin,
            HelpPlugin,
            TryItPlugin,
            WelcomeTabPlugin,
        ],
    },
    // provide plugin specific configs - if any.
    // plugin-id will be the key
    pluginConfigs: {
        [LAYOUT_MANAGER_PLUGIN_ID]: {
            container: 'app-container',
            dialogContainer: 'dialog-container',
        },
        [HELP_PLUGIN_ID]: {
            issue_tracker_url: 'https://github.com/ballerinalang/composer/issues/',
            reference_url: 'http://ballerinalang.org/docs/user-guide/',
        },
        [WELCOME_TAB_PLUGIN_ID]: {
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
                    folder: '/samples/passthroughService/passthroughservice',
                    path: '/samples/passthroughService/passthroughservice/samples/nyseStockQuoteService.bal',
                    image: 'preview_passthroughService',
                },
                {
                    name: 'Transform Statement',
                    isFile: false,
                    folder: '/samples/transformStmt',
                    path: '/samples/transformStmt/StructTransform.bal',
                    image: 'preview_transformStmt',
                },
                {
                    name: 'JMS',
                    isFile: false,
                    folder: '/samples/jms/',
                    path: '/samples/jms/jmsReceiver.bal',
                    image: 'preview_jms',
                },
                {
                    name: 'Restful Service',
                    isFile: false,
                    folder: '/samples/restfulService/restfulservice',
                    path: '/samples/restfulService/restfulservice/samples/productsService.bal',
                    image: 'preview_restfulService',
                },
                {
                    name: 'Routing Services',
                    isFile: false,
                    folder: '/samples/routingServices',
                    path: '/samples/routingServices/samples/contentBasedRoutingService.bal',
                    image: 'preview_routingServices',
                },
                {
                    name: 'Websocket',
                    isFile: false,
                    folder: '/samples/websocket',
                    path: '/samples/websocket/echoServer/server/websocketEchoServer.bal',
                    image: 'preview_websocket',
                }],
        }
    },
};
