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

import { getMenuDefinitions } from './menus';
import { getCommandDefinitions } from './commands';
import { getHandlerDefinitions } from './handlers';
import { PLUGIN_ID, DIALOGS as DIALOG_IDS } from './constants';

import PreferencesDialog from './views/PreferencesDialog';

const PREF_KEY_PREFIX = 'composer-preferences';
const HISTORY_KEY_PREFIX = 'composer-history';

/**
 * Plugin for handling user preferences.
 *
 * @class PreferencesPlugin
 */
class PreferencesManagerPlugin extends Plugin {

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
            put: (key, value) => {
                localStorage.setItem(`${PREF_KEY_PREFIX}-${key}`, value);
            },
            get: (key) => {
                return localStorage.getItem(`${PREF_KEY_PREFIX}-${key}`);
            },
            history: {
                put: (key, value) => {
                    localStorage.setItem(`${HISTORY_KEY_PREFIX}-${key}`, value);
                },
                get: (key) => {
                    return localStorage.getItem(`${HISTORY_KEY_PREFIX}-${key}`);
                },
            },
        };
    }

    /**
     * @inheritdoc
     */
    getContributions() {
        const { COMMANDS, HANDLERS, MENUS, DIALOGS } = CONTRIBUTIONS;
        return {
            [COMMANDS]: getCommandDefinitions(this),
            [HANDLERS]: getHandlerDefinitions(this),
            [MENUS]: getMenuDefinitions(this),
            [DIALOGS]: [
                {
                    id: DIALOG_IDS.PREFERENCES_DIALOG,
                    component: PreferencesDialog,
                    propsProvider: () => {
                        return {
                        };
                    },
                },
            ],
        };
    }
}

export default PreferencesManagerPlugin;
