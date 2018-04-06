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
import { getServiceEndpoint } from 'api-client/api-client';
import Plugin from 'core/plugin/plugin';
import { CONTRIBUTIONS } from 'core/plugin/constants';

import { REGIONS, COMMANDS as LAYOUT_COMMANDS } from 'core/layout/constants';

import { getCommandDefinitions } from './commands';
import { getHandlerDefinitions } from './handlers';
import { getMenuDefinitions } from './menus';
import { PLUGIN_ID, VIEWS as VIEW_IDS, DIALOG_IDS, COMMANDS as COMMAND_IDS, TOOLS as TOOL_IDS } from './constants';

import DebuggerPanel from './views/DebuggerPanel';
import DebuggerConsole from './views/DebugConsole';
import LauncherConfigDialog from './views/LauncherConfigDialog';
import RemoteDebugDialog from './views/RemoteDebugDialog';
import TraceLog from './views/TraceLog/index';

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
            dispatch(LAYOUT_COMMANDS.UPDATE_ALL_ACTION_TRIGGERS, {});
        });

        DebugManager.on('debugging-started execution-ended', () => {
            const { command: { dispatch } } = this.appContext;
            dispatch(LAYOUT_COMMANDS.UPDATE_ALL_ACTION_TRIGGERS, {});
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

        let configArguments;
        const uniqueID = this.getFileUniqueID(activeEditor.file);
        if (activeEditor && activeEditor.file) {
            configArguments = this.appContext.pref.get(`launcher-app-configs-${uniqueID}`) || [];
        }
        return configArguments;
    }

    /**
     * @inheritdoc
     */
    getContributions() {
        const { COMMANDS, HANDLERS, VIEWS, DIALOGS, TOOLS, MENUS } = CONTRIBUTIONS;
        return {
            [MENUS]: getMenuDefinitions(this),
            [COMMANDS]: getCommandDefinitions(this),
            [HANDLERS]: getHandlerDefinitions(this),
            [VIEWS]: [
                {
                    id: VIEW_IDS.DEBUGGER_PANEL,
                    component: DebuggerPanel,
                    propsProvider: () => {
                        LaunchManager.init(getServiceEndpoint('ballerina-launcher'));
                        // TODO Fix this and get from config service/or another solution
                        DebugManager.init(this.appContext.debuggerPath);
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
                        activityBarIcon: 'bug',
                        panelTitle: 'Debugger',
                        panelActions: [
                            {
                                icon: 'configurations',
                                handleAction: () => {
                                    const { command: { dispatch } } = this.appContext;
                                    dispatch(COMMAND_IDS.SHOW_LAUNCHER_CONFIG_DIALOG, {});
                                },
                                description: 'Configure Application Arguments',
                            },
                            {
                                icon: 'console',
                                handleAction: () => {
                                    const { command: { dispatch } } = this.appContext;
                                    dispatch(LAYOUT_COMMANDS.TOGGLE_BOTTOM_PANEL);
                                },
                                description: 'Toggle Console',
                            },
                        ],
                    },
                    displayOnLoad: true,
                },
                {
                    id: VIEW_IDS.DEBUGGER_CONSOLE,
                    component: DebuggerConsole,
                    propsProvider: () => {
                        LaunchManager.init(getServiceEndpoint('ballerina-launcher'));
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
                {
                    id: VIEW_IDS.LOGS_CONSOLE,
                    component: TraceLog,
                    propsProvider: () => {
                        LaunchManager.init(getServiceEndpoint('ballerina-launcher'));
                        return {
                            debuggerPlugin: this,
                            LaunchManager,
                        };
                    },
                    region: REGIONS.BOTTOM_PANEL,
                    // region specific options for bottom views
                    regionOptions: {
                        panelTitle: 'Trace Logs',
                        panelActions: [
                            {
                                icon: 'start',
                                handleAction: () => {

                                },
                            },
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
                        const configArguments = this.getArgumentConfigs() || [];
                        return {
                            debuggerPlugin: this,
                            configArguments,
                            onSaveConfigs: (newConfigArguments = []) => {
                                this.appContext.pref.put(`launcher-app-configs-${uniqueID}`, newConfigArguments);
                            },
                        };
                    },
                },
                {
                    id: DIALOG_IDS.REMOTE_DEBUG,
                    component: RemoteDebugDialog,
                    propsProvider: () => {
                        return {
                            debuggerPlugin: this,
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
                        const activeEditor = this.appContext.editor.getActiveEditor();
                        return activeEditor && activeEditor.definition &&
                            activeEditor.definition.id === 'composer.editor.ballerina' && !LaunchManager.active;
                    },
                    isVisible: () => {
                        return !LaunchManager.active;
                    },
                    description: 'Run',
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
                    description: 'Stop',
                },
                {
                    id: TOOL_IDS.DEBUG,
                    group: TOOL_IDS.GROUP,
                    icon: 'bug',
                    commandID: COMMAND_IDS.RUN_WITH_DEBUG,
                    isVisible: () => {
                        if (LaunchManager.active || DebugManager.active) {
                            return false;
                        }
                        return true;
                    },
                    isActive: () => {
                        const activeEditor = this.appContext.editor.getActiveEditor();
                        return activeEditor && activeEditor.definition &&
                            activeEditor.definition.id === 'composer.editor.ballerina' && !DebugManager.active;
                    },
                    description: 'Run With Debug',
                },
                {
                    id: TOOL_IDS.DEBUG_STOP,
                    group: TOOL_IDS.GROUP,
                    icon: 'stop',
                    commandID: COMMAND_IDS.STOP,
                    isVisible: () => {
                        return DebugManager.active;
                    },
                    description: 'Stop Debug',
                },
                {
                    id: TOOL_IDS.RESUME,
                    group: TOOL_IDS.GROUP,
                    icon: 'start',
                    commandID: COMMAND_IDS.RESUME,
                    isVisible: () => {
                        return DebugManager.active;
                    },
                    description: 'Resume',
                },
                {
                    id: TOOL_IDS.STEP_OVER,
                    group: TOOL_IDS.GROUP,
                    icon: 'stepover',
                    commandID: COMMAND_IDS.STEP_OVER,
                    isVisible: () => {
                        return DebugManager.active;
                    },
                    description: 'Step Over',
                },
                {
                    id: TOOL_IDS.STEP_IN,
                    group: TOOL_IDS.GROUP,
                    icon: 'stepin',
                    commandID: COMMAND_IDS.STEP_IN,
                    isVisible: () => {
                        return DebugManager.active;
                    },
                    description: 'Step In',
                },
                {
                    id: TOOL_IDS.STEP_OUT,
                    group: TOOL_IDS.GROUP,
                    icon: 'stepout',
                    commandID: COMMAND_IDS.STEP_OUT,
                    isVisible: () => {
                        return DebugManager.active;
                    },
                    description: 'Step Out',
                },
            ],
        };
    }


}

export default DebuggerPlugin;
