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
import { REGIONS, COMMANDS as LAYOUT_COMMANDS } from 'core/layout/constants';
import { TOOLS as DEBUGGER_TOOLS } from 'plugins/debugger/constants';
import LaunchManager from 'plugins/debugger/LaunchManager';
/** Plugin imports */
import { getCommandDefinitions } from './commands';
import { getHandlerDefinitions } from './handlers';
import TryIt from './views/try-it-container';
import { VIEWS as TRY_IT_VIEW,
    TRY_IT_PLUGIN_ID,
    LABELS,
    TOOLS as TRY_IT_TOOLS,
    COMMANDS as TRY_IT_COMMANDS,
    PLUGIN_CONSTANTS as CONSTANTS } from './constants';

/**
 * Plugin for Try-it
 */
class TryItPlugin extends Plugin {

    constructor() {
        super();
        this.activeFile = undefined;
    }

    /**
     * @inheritdoc
     */
    getID() {
        return TRY_IT_PLUGIN_ID;
    }

    /**
     * @inheritdoc
     */
    onAfterInitialRender() {
        const { command, editor } = this.appContext;

        command.on('debugger-run-with-debug-executed', (file) => {
            if (file) {
                this.activeFile = file;
            }
        });

        command.on('debugger-run-executed', (file) => {
            if (file) {
                this.activeFile = file;
            }
        });
        command.on('debugger-stop-executed', () => {
            const tryItEditor = editor.getEditorByID(TRY_IT_VIEW.TRY_IT_VIEW_ID);
            if (tryItEditor) {
                const prevEditor = editor.getEditorByID(this.activeFile.fullPath);
                editor.closeEditor(tryItEditor, prevEditor);
            }
        });
    }

    /**
     * @inheritdoc
     */
    getContributions() {
        const { COMMANDS, HANDLERS, VIEWS, TOOLS } = CONTRIBUTIONS;
        return {
            [COMMANDS]: getCommandDefinitions(this),
            [HANDLERS]: getHandlerDefinitions(this),
            [VIEWS]: [
                {
                    id: TRY_IT_VIEW.TRY_IT_VIEW_ID,
                    component: TryIt,
                    propsProvider: () => {
                        return {
                            balFile: this.activeFile,
                        };
                    },
                    region: REGIONS.EDITOR_TABS,
                    regionOptions: {
                        tabTitle: LABELS.TRY_IT_HEADING,
                        customTitleClass: 'try-it-title',
                    },
                },
            ],
            [TOOLS]: [
                {
                    id: TRY_IT_TOOLS.SHOW_TRY_IT,
                    group: DEBUGGER_TOOLS.GROUP,
                    icon: 'dgm-try-catch',
                    commandID: TRY_IT_COMMANDS.SHOW_TRY_IT,
                    isActive: () => {
                        return true;
                    },
                    isVisible: () => {
                        const isService = LaunchManager.messages.filter((message) => {
                            return message.message && message.message.startsWith(CONSTANTS.HTTP_SERVICE_PREFIX);
                        }).length > 0;
                        return LaunchManager.active && isService;
                    },
                    description: 'Try-It',
                },
            ],
        };
    }

}

export default TryItPlugin;
