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
import Plugin from './../plugin/plugin';
import { CONTRIBUTIONS } from './../plugin/constants';

import { REGIONS } from './../layout/constants';

import { getCommandDefinitions } from './commands';
import { getHandlerDefinitions } from './handlers';
import { PLUGIN_ID, VIEW_IDS } from './constants';

import AppMenuView from './views/AppMenu';

/**
 * ApplicationMenuPlugin is responsible for rendering menu items.
 *
 * @class ApplicationMenuPlugin
 */
class ApplicationMenuPlugin extends Plugin {

    constructor() {
        super();
        this.menuItems = [];
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
    getContributions() {
        const { COMMANDS, HANDLERS, VIEWS } = CONTRIBUTIONS;
        return {
            [COMMANDS]: getCommandDefinitions(this),
            [HANDLERS]: getHandlerDefinitions(this),
            [VIEWS]: [
                {
                    id: VIEW_IDS.APP_MENU,
                    component: AppMenuView,
                    propsProvider: () => {
                        return {
                            menu: this.menuItems,
                        };
                    },
                    region: REGIONS.HEADER,
                    displayOnLoad: true,
                },
            ],
        };
    }
}

export default ApplicationMenuPlugin;
