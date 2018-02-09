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

import { VIEWS as WELCOME_TAB_VIEWS } from 'plugins/welcome-tab/constants';
import { COMMANDS as LAYOUT_COMMANDS } from 'core/layout/constants';
import { COMMANDS, DIALOG } from './constants';

/**
 * Provides command handler definitions of debugger plugin.
 * @param {debugger} debugger plugin instance
 * @returns {Object[]} command handler definitions.
 *
 */
export function getHandlerDefinitions(plugin) {
    return [
        {
            cmdID: COMMANDS.SHOW_IMPORT_SWAGGER_DIALOG,
            handler: () => {
                const { command: { dispatch } } = plugin.appContext;
                const id = DIALOG.IMPORT_SWAGGER;
                dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, { id });
            },
        },
        {
            cmdID: COMMANDS.IMPORT_SWAGGER_DEFINITION,
            handler: () => {
                window.open(plugin.config.issue_tracker_url);
            },
        },
    ];
}
