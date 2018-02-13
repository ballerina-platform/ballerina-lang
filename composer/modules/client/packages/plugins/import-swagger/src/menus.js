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
import { WorkspaceConstants, MenuConstants } from '@ballerina-lang/composer-core';
import { MENUS, COMMANDS, LABELS } from './constants';

/**
 * Provides menu definitions of help plugin.
 *
 * @returns {Object[]} menu definitions.
 *
 */
export function getMenuDefinitions(plugin) {
    return [
        {
            id: MENUS.IMPORT_SWAGGER_MENU,
            parent: WorkspaceConstants.MENUS.FILE_MENU,
            label: LABELS.IMPORT_SWAGGER_MENU_TITLE,
            isActive: (appContext) => {
                return true;
            },
            icon: 'swagger',
            command: COMMANDS.SHOW_IMPORT_SWAGGER_DIALOG,
            type: MenuConstants.MENU_DEF_TYPES.ITEM,
        },
    ];
}
