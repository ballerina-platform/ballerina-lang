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
import { LayoutConstants } from '@ballerina-lang/composer-core';
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
                window.open(plugin.config.example_url);
            },
        },
        {
            cmdID: COMMANDS.OPEN_API_REFERENCE,
            handler: () => {
                window.open(plugin.config.api_reference_url);
            },
        },
        {
            cmdID: COMMANDS.REPORT_ISSUE,
            handler: () => {
                window.open(plugin.config.issue_tracker_url);
            },
        },
        {
            cmdID: COMMANDS.SHOW_ABOUT,
            handler: () => {
                const id = DIALOG.ABOUT;
                plugin.appContext.command.dispatch(LayoutConstants.COMMANDS.POPUP_DIALOG, { id });
            },
        },
    ];
}
