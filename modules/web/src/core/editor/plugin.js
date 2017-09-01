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
import _ from 'lodash';
import Plugin from './../plugin/plugin';
import { CONTRIBUTIONS } from './../plugin/constants';

import { REGIONS } from './../layout/constants';

import { getCommandDefinitions } from './commands';
import { getHandlerDefinitions } from './handlers';
import { getMenuDefinitions } from './menus';
import { PLUGIN_ID, VIEWS as VIEW_IDS, DIALOGS as DIALOG_IDS, HISTORY, EVENTS } from './constants';

import EditorTabs from './views/EditorTabs';

/**
 * Editor Plugin is responsible for providing editors to opening files.
 *
 * @class EditorPlugin
 */
class EditorPlugin extends Plugin {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
    }

    /**
     * @inheritdoc
     */
    getID() {
        return PLUGIN_ID;
    }

    /**
     * @inheritdoc
     */
    init(config) {
        super.init(config);
        return {
        };
    }

    /**
     * @inheritdoc
     */
    activate(appContext) {
        super.activate(appContext);
    }

    /**
     * @inheritdoc
     */
    getContributions() {
        const { COMMANDS, HANDLERS, MENUS, VIEWS, DIALOGS } = CONTRIBUTIONS;
        return {
            [COMMANDS]: getCommandDefinitions(this),
            [HANDLERS]: getHandlerDefinitions(this),
            [MENUS]: getMenuDefinitions(this),
            [VIEWS]: [
                {
                    id: VIEW_IDS.EDITOR_TABS,
                    component: EditorTabs,
                    propsProvider: () => {
                        return {
                            editorPlugin: this,
                        };
                    },
                    region: REGIONS.EDITOR_AREA,
                    displayOnLoad: true,
                },
            ],
            [DIALOGS]: [],
        };
    }
}

export default EditorPlugin;
