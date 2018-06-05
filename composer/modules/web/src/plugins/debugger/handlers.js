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
import { COMMANDS as WORKSPACE_COMMANDS } from 'core/workspace/constants';
import { COMMANDS, DIALOG_IDS, VIEWS as VIEW_IDS } from './constants';
import LaunchManager from './LaunchManager';
import DebugManager from './DebugManager';

function saveFile(dispatch, activeEditor, onSaveSuccess) {
    if (!activeEditor.file.isDirty) {
        onSaveSuccess();
    } else {
        dispatch(WORKSPACE_COMMANDS.SAVE_FILE, {
            file: activeEditor.file,
            onSaveSuccess,
        });
    }

}

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
                const { command: { dispatch } } = debuggerPlugin.appContext;
                if (activeEditor && activeEditor.file) {
                    saveFile(dispatch, activeEditor, () => {
                        dispatch(LAYOUT_COMMANDS.SHOW_BOTTOM_PANEL);
                        dispatch(LAYOUT_COMMANDS.SHOW_VIEW, { id: VIEW_IDS.DEBUGGER_PANEL });
                        LaunchManager.run(activeEditor.file, true,
                            debuggerPlugin.getArgumentConfigs(activeEditor.file));
                    });
                    dispatch('debugger-run-with-debug-executed', activeEditor.file);
                }
            },
        },
        {
            cmdID: COMMANDS.STOP,
            handler: () => {
                if (DebugManager.active) {
                    DebugManager.stop();
                }
                if (LaunchManager.active) {
                    LaunchManager.stop();
                }
                const { command } = debuggerPlugin.appContext;
                command.dispatch('debugger-stop-executed');
            },
        },
        {
            cmdID: COMMANDS.RUN,
            handler: () => {
                const activeEditor = debuggerPlugin.appContext.editor.getActiveEditor();
                const { command: { dispatch } } = debuggerPlugin.appContext;
                if (activeEditor && activeEditor.file) {
                    saveFile(dispatch, activeEditor, () => {
                        dispatch(LAYOUT_COMMANDS.SHOW_BOTTOM_PANEL);
                        dispatch(LAYOUT_COMMANDS.SHOW_VIEW, { id: VIEW_IDS.DEBUGGER_PANEL });
                        LaunchManager.run(activeEditor.file, false,
                            debuggerPlugin.getArgumentConfigs(activeEditor.file));
                    });
                    dispatch('debugger-run-executed', activeEditor.file);
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
            cmdID: COMMANDS.SHOW_REMOTE_DEBUG_DIALOG,
            handler: () => {
                const id = DIALOG_IDS.REMOTE_DEBUG;
                const { command: { dispatch } } = debuggerPlugin.appContext;
                dispatch(LAYOUT_COMMANDS.POPUP_DIALOG, { id });
            },
        },
        {
            cmdID: COMMANDS.RESUME,
            handler: (threadId) => {
                DebugManager.resume(threadId);
            },
        },
        {
            cmdID: COMMANDS.STEP_OVER,
            handler: (threadId) => {
                DebugManager.stepOver(threadId);
            },
        },
        {
            cmdID: COMMANDS.STEP_IN,
            handler: (threadId) => {
                DebugManager.stepIn(threadId);
            },
        },
        {
            cmdID: COMMANDS.STEP_OUT,
            handler: (threadId) => {
                DebugManager.stepOut(threadId);
            },
        },
    ];
}
