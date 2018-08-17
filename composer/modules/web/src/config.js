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
import ImportRecordPlugin from './plugins/import-record/plugin';
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
            ImportRecordPlugin,
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
            issue_tracker_url: 'https://github.com/ballerina-platform/ballerina-lang/issues?q=is%3Aopen+is%3Aissue+label%3Acomponent%2FComposer',
            example_url: 'https://ballerina.io/learn/by-example/',
            api_reference_url: 'https://ballerina.io/learn/api-docs/ballerina/',
        },
        [WELCOME_TAB_PLUGIN_ID]: {
            userGuide: 'https://ballerina.io/learn/by-example/',
        },
    },
};
