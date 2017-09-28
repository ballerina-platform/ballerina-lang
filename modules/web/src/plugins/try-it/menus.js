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

import { MENU_DEF_TYPES } from 'core/menu/constants';
import { MENUS as VIEW_MENU} from 'core/layout/constants';
import { MENUS, COMMANDS, LABELS } from './constants';

/**
 * Provides menu definitions of try-it plugin.
 * @returns {Object[]} menu definitions.
 */
export function getMenuDefinitions(plugin) {
    return [
        {
            id: MENUS.TRY_IT_MENU,
            parent: VIEW_MENU.VIEW_MENU,
            label: LABELS.TRY_IT_HEADING,
            isActive: (appContext) => {
                return true;
            },
            command: COMMANDS.SHOW_TRY_IT,
            icon: 'dgm-try-catch',
            type: MENU_DEF_TYPES.ITEM,
        },
    ];
}
