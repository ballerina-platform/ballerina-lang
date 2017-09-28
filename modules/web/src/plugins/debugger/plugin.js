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
import Plugin from 'core/plugin/plugin';
import { CONTRIBUTIONS } from 'core/plugin/constants';

import { REGIONS } from 'core/layout/constants';

import { COMMANDS as TOOL_BAR_COMMANDS } from 'core/toolbar/constants';

import { getCommandDefinitions } from './commands';
import { getHandlerDefinitions } from './handlers';
import { getMenuDefinitions } from './menus';
import { PLUGIN_ID, VIEWS as VIEW_IDS, DIALOG_IDS, COMMANDS as COMMAND_IDS, TOOLS as TOOL_IDS } from './constants';

import DebuggerPanel from './views/DebuggerPanel';
import DebuggerConsole from './views/DebugConsole';
import LauncherConfigDialog from './views/LauncherConfigDialog';

import LaunchManager from './LaunchManager';
import DebugManager from './DebugManager';
/**
 * Debugger is responsible for debugging files.
 *
 * @class DebuggerPlugin
 */
class DebuggerPlugin extends Plugin {

    constructor() {
        super();
        this.debuggerActive = false;
        this.launcherActive = false;

        LaunchManager.on('execution-started execution-ended', () => {
            const { command: { dispatch } } = this.appContext;
            dispatch(TOOL_BAR_COMMANDS.UPDATE_TOOL_BAR, {});
        });

        DebugManager.on('debugging-started execution-ended', () => {
            const { command: { dispatch } } = this.appContext;
            dispatch(TOOL_BAR_COMMANDS.UPDATE_TOOL_BAR, {});
        });
    }

    /**
     * @inheritdoc
     */
    getID() {
        return PLUGIN_ID;
    }

    /**
     * create unique id for file
     * @param {File} file
     * @returns string - unique id
     * @memberof DebuggerPlugin
     */
    getFileUniqueID(file = {}) {
        const name = file.name;
        const path = file.path;
        return encodeURI(`${path}/${name}`);
    }

    getArgumentConfigs() {
        const activeEditor = this.appContext.editor.getActiveEditor();

        let configArgumentsStr = '';
        const uniqueID = this.getFileUniqueID(activeEditor.file);
        if (activeEditor && activeEditor.file) {
            configArgumentsStr = this.appContext.pref.get(`launcher-app-configs-${uniqueID}`) || '';
        }
        return configArgumentsStr;
    }

    /**
     * @inheritdoc
     */
    getContributions() {
        const { COMMANDS, HANDLERS, VIEWS, DIALOGS, TOOLS } = CONTRIBUTIONS;
        return {
            [COMMANDS]: getCommandDefinitions(this),
            [HANDLERS]: getHandlerDefinitions(this),
            [VIEWS]: [
                {
                    id: VIEW_IDS.DEBUGGER_PANEL,
                    component: DebuggerPanel,
                    propsProvider: () => {
                        LaunchManager.init(this.appContext.services.launcher.endpoint);
                        DebugManager.init(this.appContext.services.debugger.endpoint);
                        return {
                            debuggerPlugin: this,
                            commandProxy: this.appContext.command,
                            LaunchManager,
                            DebugManager,
                        };
                    },
                    region: REGIONS.LEFT_PANEL,
                    // region specific options for left-panel views
                    regionOptions: {
                        activityBarIcon: 'start',
                        panelTitle: 'Run',
                        panelActions: [
                            {
                                icon: 'configarations',
                                handleAction: () => {
                                    const { command: { dispatch } } = this.appContext;
                                    dispatch(COMMAND_IDS.SHOW_LAUNCHER_CONFIG_DIALOG, {});
                                },
                            },
                        ],
                    },
                    displayOnLoad: true,
                },
                {
                    id: VIEW_IDS.DEBUGGER_CONSOLE,
                    component: DebuggerConsole,
                    propsProvider: () => {
                        LaunchManager.init(this.appContext.services.launcher.endpoint);
                        return {
                            debuggerPlugin: this,
                            LaunchManager,
                        };
                    },
                    region: REGIONS.BOTTOM_PANEL,
                    // region specific options for bottom views
                    regionOptions: {
                        panelTitle: 'Console',
                        panelActions: [
                        ],
                    },
                    displayOnLoad: true,
                },
            ],
            [DIALOGS]: [
                {
                    id: DIALOG_IDS.LAUNCHER_CONFIG,
                    component: LauncherConfigDialog,
                    propsProvider: () => {
                        const activeEditor = this.appContext.editor.getActiveEditor();
                        const uniqueID = this.getFileUniqueID(activeEditor.file);
                        const configArguments = this.getArgumentConfigs().split(' ') || [];
                        return {
                            debuggerPlugin: this,
                            configArguments,
                            onSaveConfigs: (newConfigArguments = []) => {
                                this.appContext.pref.put(`launcher-app-configs-${uniqueID}`, newConfigArguments.join(' '));
                            },
                        };
                    },
                },
            ],
            [TOOLS]: [
                {
                    id: TOOL_IDS.RUN,
                    group: TOOL_IDS.GROUP,
                    icon: 'start',
                    commandID: COMMAND_IDS.RUN,
                    isActive: () => {
                        return !LaunchManager.active;
                    },
                    isVisible: () => {
                        return !LaunchManager.active;
                    },
                },
                {
                    id: TOOL_IDS.STOP,
                    group: TOOL_IDS.GROUP,
                    icon: 'stop',
                    commandID: COMMAND_IDS.STOP,
                    isVisible: () => {
                        if (DebugManager.active) {
                            return false;
                        }
                        return LaunchManager.active;
                    },
                },
                {
                    id: TOOL_IDS.DEBUG,
                    group: TOOL_IDS.GROUP,
                    icon: 'bug',
                    commandID: COMMAND_IDS.RUN_WITH_DEBUG,
                    isVisible: () => {
                        return !DebugManager.active;
                    },
                    isActive: () => {
                        return !LaunchManager.active;
                    },
                },
                {
                    id: TOOL_IDS.DEBUG_STOP,
                    group: TOOL_IDS.GROUP,
                    icon: 'stop',
                    commandID: COMMAND_IDS.STOP,
                    isVisible: () => {
                        return DebugManager.active;
                    },
                },
                {
                    id: TOOL_IDS.RESUME,
                    group: TOOL_IDS.GROUP,
                    icon: 'start',
                    commandID: COMMAND_IDS.RESUME,
                    isVisible: () => {
                        return DebugManager.active;
                    },
                },
                {
                    id: TOOL_IDS.STEP_OVER,
                    group: TOOL_IDS.GROUP,
                    icon: 'stepover',
                    commandID: COMMAND_IDS.STEP_OVER,
                    isVisible: () => {
                        return DebugManager.active;
                    },
                },
                {
                    id: TOOL_IDS.STEP_IN,
                    group: TOOL_IDS.GROUP,
                    icon: 'stepin',
                    commandID: COMMAND_IDS.STEP_IN,
                    isVisible: () => {
                        return DebugManager.active;
                    },
                },
                {
                    id: TOOL_IDS.STEP_OUT,
                    group: TOOL_IDS.GROUP,
                    icon: 'stepout',
                    commandID: COMMAND_IDS.STEP_OUT,
                    isVisible: () => {
                        return DebugManager.active;
                    },
                },
            ],
        };
    }


}


    // STEP_OVER: 'step-over',
    // RESUME: 'resume',
    // STEP_IN: 'step-in',
    // STEP_OUT: 'step-out',
    // STOP: 'stop',
    // SHOW_LAUNCHER_CONFIG_DIALOG: 'show-launcher-config-dialog',

export default DebuggerPlugin;
