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

import { getCommandDefinitions } from './commands';
import { getHandlerDefinitions } from './handlers';
import { getMenuDefinitions } from './menus';
import { PLUGIN_ID, VIEWS as VIEW_IDS } from './constants';

import DebuggerPanel from './views/DebuggerPanel';
import DebuggerConsole from './views/DebugConsole';

import LaunchManager from './LaunchManager';
import DebugManager from './DebugManager';
/**
 * Debugger is responsible for debugging files.
 *
 * @class DebuggerPlugin
 */
class DebuggerPlugin extends Plugin {

    /**
     * @inheritdoc
     */
    getID() {
        return PLUGIN_ID;
    }

    /**
     * @inheritdoc
     */
    getContributions() {
        const { COMMANDS, HANDLERS, MENUS, VIEWS } = CONTRIBUTIONS;
        return {
            [COMMANDS]: getCommandDefinitions(this),
            [HANDLERS]: getHandlerDefinitions(this),
            [MENUS]: getMenuDefinitions(this),
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
                        activityBarIcon: 'bug',
                        panelTitle: 'Debug',
                        panelActions: [
                            {
                                icon: '',
                                state: () => {

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
        };
    }


}

export default DebuggerPlugin;
