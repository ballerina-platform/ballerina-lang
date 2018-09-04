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

import { MENUS, COMMANDS, LABELS } from './constants';
import { MENU_DEF_TYPES } from './../../core/menu/constants';

/**
 * Provides menu definitions of help plugin.
 *
 * @returns {Object[]} menu definitions.
 *
 */
export function getMenuDefinitions(plugin) {
    return [{
            id: MENUS.HELP_MENU,
            label: LABELS.HELP,
            isActive: (appContext) => {
                return true;
            },
            icon: '',
            type: MENU_DEF_TYPES.ROOT,
        },
        {
            id: MENUS.EXAMPLE_MENU,
            parent: MENUS.HELP_MENU,
            label: LABELS.EXAMPLE,
            isActive: (appContext) => {
                return true;
            },
            order: 1,
            command: COMMANDS.OPEN_EXAMPLE,
            type: MENU_DEF_TYPES.ITEM,
        },
        {
            id: MENUS.API_REFERENCE_MENU,
            parent: MENUS.HELP_MENU,
            label: LABELS.API_REFERENCE,
            isActive: (appContext) => {
                return true;
            },
            order: 2,
            command: COMMANDS.OPEN_API_REFERENCE,
            type: MENU_DEF_TYPES.ITEM,
        },
        {
            id: MENUS.REPORT_MENU,
            parent: MENUS.HELP_MENU,
            label: LABELS.REPORT,
            isActive: (appContext) => {
                return true;
            },
            icon: 'warning',
            order: 10,
            command: COMMANDS.REPORT_ISSUE,
            type: MENU_DEF_TYPES.ITEM,
        },
        {
            id: MENUS.ABOUT_MENU,
            parent: MENUS.HELP_MENU,
            label: LABELS.ABOUT,
            isActive: (appContext) => {
                return true;
            },
            order: 20,
            command: COMMANDS.SHOW_ABOUT,
            type: MENU_DEF_TYPES.ITEM,
        },
    ];
}