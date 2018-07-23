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
import { COMMANDS as WORKSPACE_COMMANDS } from 'core/workspace/constants';
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
            cmdID: COMMANDS.OPEN_EXAMPLE,
            handler: () => {
                plugin.appContext.command.dispatch(WORKSPACE_COMMANDS.SHOW_EXTERNAL_LINK,
                    { url: plugin.config.example_url });
            },
        },
        {
            cmdID: COMMANDS.OPEN_API_REFERENCE,
            handler: () => {
                plugin.appContext.command.dispatch(WORKSPACE_COMMANDS.SHOW_EXTERNAL_LINK,
                    { url: plugin.config.api_reference_url });
            },
        },
        {
            cmdID: COMMANDS.REPORT_ISSUE,
            handler: () => {
                plugin.appContext.command.dispatch(WORKSPACE_COMMANDS.SHOW_EXTERNAL_LINK,
                    { url: plugin.config.issue_tracker_url });
            },
        },
        {
            cmdID: COMMANDS.SHOW_ABOUT,
            handler: () => {
                const id = DIALOG.ABOUT;
                plugin.appContext.command.dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, { id });
            },
        },
    ];
}
