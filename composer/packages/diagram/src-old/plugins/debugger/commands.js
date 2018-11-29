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

import { COMMANDS } from './constants';

/**
 * Provides command definitions of debugger plugin.
 *
 * @returns {Object[]} command definitions.
 *
 */
export function getCommandDefinitions(debuggerInstance) {
    return [
        {
            id: COMMANDS.RUN,
            shortcut: {
                default: 'ctrl+shift+r',
            },
        },
        {
            id: COMMANDS.RUN_WITH_DEBUG,
            shortcut: {
                default: 'ctrl+shift+d',
            },
        },
        {
            id: COMMANDS.TOGGLE_DEBUGGER,
        },
        {
            id: COMMANDS.TOGGLE_LAUNCHER,
        },
        {
            id: COMMANDS.STEP_OVER,
            shortcut: {
                default: 'alt+o',
            },
        },
        {
            id: COMMANDS.RESUME,
            shortcut: {
                default: 'alt+r',
            },
        },
        {
            id: COMMANDS.STEP_IN,
            shortcut: {
                default: 'alt+i',
            },
        },
        {
            id: COMMANDS.STEP_OUT,
            shortcut: {
                default: 'alt+u',
            },
        },
        {
            id: COMMANDS.STOP,
            shortcut: {
                default: 'alt+p',
            },
        },
    ];
}
