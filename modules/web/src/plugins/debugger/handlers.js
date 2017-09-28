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

import { COMMANDS as LAYOUT_COMMANDS } from 'core/layout/constants';
import { COMMANDS, DIALOG_IDS } from './constants';
import LaunchManager from './LaunchManager';
import DebugManager from './DebugManager';

/**
 * Provides command handler definitions of debugger plugin.
 * @param {debugger} debugger plugin instance
 * @returns {Object[]} command handler definitions.
 *
 */
export function getHandlerDefinitions(debuggerPlugin) {
    return [
        {
            cmdID: COMMANDS.RUN_WITH_DEBUG,
            handler: () => {
                const activeEditor = debuggerPlugin.appContext.editor.getActiveEditor();
                if (activeEditor && activeEditor.file) {
                    LaunchManager.run(activeEditor.file, true, debuggerPlugin.getArgumentConfigs(activeEditor.file));
                }
            },
        },
        {
            cmdID: COMMANDS.STOP,
            handler: () => {
                LaunchManager.stop();
            },
        },
        {
            cmdID: COMMANDS.RUN,
            handler: () => {
                const activeEditor = debuggerPlugin.appContext.editor.getActiveEditor();
                if (activeEditor && activeEditor.file) {
                    LaunchManager.run(activeEditor.file, false, debuggerPlugin.getArgumentConfigs(activeEditor.file));
                }
            },
        },
        {
            cmdID: COMMANDS.SHOW_LAUNCHER_CONFIG_DIALOG,
            handler: () => {
                const id = DIALOG_IDS.LAUNCHER_CONFIG;
                const { command: { dispatch } } = debuggerPlugin.appContext;
                dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, { id });
            },
        },
        {
            cmdID: COMMANDS.RESUME,
            handler: () => {
                DebugManager.resume();
            },
        },
        {
            cmdID: COMMANDS.STEP_OVER,
            handler: () => {
                DebugManager.stepOver();
            },
        },
        {
            cmdID: COMMANDS.STEP_IN,
            handler: () => {
                DebugManager.stepIn();
            },
        },
        {
            cmdID: COMMANDS.STEP_OUT,
            handler: () => {
                DebugManager.stepOut();
            },
        },
    ];
}
