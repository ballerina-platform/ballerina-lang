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
 *
 */

import { MENUS as MENU_IDS, COMMANDS, LABELS } from './constants';
import { MENU_DEF_TYPES } from './../menu/constants';
import { MENUS as WORKSPACE_MENUS } from './../workspace/constants';


/**
 * Provides menu definitions of preferences plugin.
 *
 * @returns {Object[]} menu definitions.
 *
 */
export function getMenuDefinitions() {
    // TODO To be enabled once the Preferences dialog is fixed
    /* return [
        {
            id: MENU_IDS.PREFERENCES_MENU,
            parent: WORKSPACE_MENUS.FILE_MENU,
            label: LABELS.PREFERENCES,
            isActive: (appContext) => {
                return true;
            },
            command: COMMANDS.SHOW_SETTINGS_DIALOG,
            icon: 'settings',
            type: MENU_DEF_TYPES.ITEM,
        },
    ];*/
}
