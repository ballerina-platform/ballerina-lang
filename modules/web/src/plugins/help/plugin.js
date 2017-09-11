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
import Plugin from './../../core/plugin/plugin';
import { CONTRIBUTIONS } from './../../core/plugin/constants';

import { getCommandDefinitions } from './commands';
import { getHandlerDefinitions } from './handlers';
import { getMenuDefinitions } from './menus';
import { PLUGIN_ID, DIALOG, VIEWS as HELP_VIEWS, LABELS} from './constants';
import { REGIONS, COMMANDS as LAYOUT_COMMANDS } from './../../core/layout/constants';
import { COMMANDS as WORKSPACE_COMMANDS } from './../../core/workspace/constants';
import AboutDialog from './dialogs/AboutDialog';
import WelcomeView from '../../../js/welcome-page/welcome';
/**
 * Help plugin.
 *
 * @class HelpPlugin
 */
class HelpPlugin extends Plugin {

    /**
     * @inheritdoc
     */
    getID() {
        return PLUGIN_ID;
    }

    /**
     * @inheritdoc
     */
    onAfterInitialRender() {
        const { command } = this.appContext;
        command.dispatch(LAYOUT_COMMANDS.SHOW_VIEW, HELP_VIEWS.WELCOME);
    }

    createNewHandler() {
        console.log(this);
    }

    openFileHandler() {
        const { command } = this.appContext;
        command.dispatch(WORKSPACE_COMMANDS.SHOW_FILE_OPEN_WIZARD, '');
    }

    openDirectoryHandler() {
        const { command } = this.appContext;
        command.dispatch(WORKSPACE_COMMANDS.SHOW_FOLDER_OPEN_WIZARD, '');
    }

    /**
     * @inheritdoc
     */
    getContributions() {
        const { COMMANDS, HANDLERS, MENUS, DIALOGS, VIEWS } = CONTRIBUTIONS;
        return {
            [COMMANDS]: getCommandDefinitions(this),
            [HANDLERS]: getHandlerDefinitions(this),
            [MENUS]: getMenuDefinitions(this),
            [DIALOGS]: [
                {
                    id: DIALOG.ABOUT,
                    component: AboutDialog,
                    propsProvider: () => {
                        return {
                        };
                    },
                },
            ],
            [VIEWS]: [
                {
                    id: HELP_VIEWS.WELCOME,
                    component: WelcomeView,
                    propsProvider: () => {
                        return {
                            createNew: this.createNewHandler.bind(this),
                            openFile: this.openFileHandler.bind(this),
                            openDirectory: this.openDirectoryHandler.bind(this),
                            referenceUrl: this.config.reference_url,
                        };
                    },
                    region: REGIONS.EDITOR_TABS,
                    // region specific options for editor-tabs views
                    regionOptions: {
                        tabTitle: LABELS.WELCOME,
                    },
                },
            ],
        };
    }
}

export default HelpPlugin;
