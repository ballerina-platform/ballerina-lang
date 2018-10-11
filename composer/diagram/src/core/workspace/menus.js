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
import _ from 'lodash';
import { MENUS, COMMANDS, LABELS } from './constants';
import { MENU_DEF_TYPES } from './../menu/constants';

/**
 * Provides menu definitions of workspace plugin.
 *
 * @returns {Object[]} menu definitions.
 *
 */
export function getMenuDefinitions(workspaceManager) {
    return [
        {
            id: MENUS.FILE_MENU,
            label: LABELS.FILE,
            isActive: (appContext) => {
                return true;
            },
            icon: '',
            order: 0,
            type: MENU_DEF_TYPES.ROOT,
        },
        {
            id: MENUS.NEW_FILE,
            parent: MENUS.FILE_MENU,
            label: LABELS.NEW_FILE,
            isActive: () => {
                return true;
            },
            command: COMMANDS.CREATE_NEW_FILE,
            icon: 'add',
            type: MENU_DEF_TYPES.ITEM,
            divider: {
                after: true,
            },
        },
        {
            id: MENUS.SHOW_FILE_OPEN_WIZARD,
            parent: MENUS.FILE_MENU,
            label: LABELS.SHOW_FILE_OPEN_WIZARD,
            isActive: (appContext) => {
                return true;
            },
            command: COMMANDS.SHOW_FILE_OPEN_WIZARD,
            icon: 'document',
            type: MENU_DEF_TYPES.ITEM,
        },
        {
            id: MENUS.SHOW_FOLDER_OPEN_WIZARD,
            parent: MENUS.FILE_MENU,
            label: LABELS.SHOW_FOLDER_OPEN_WIZARD,
            isActive: (appContext) => {
                return true;
            },
            command: COMMANDS.SHOW_FOLDER_OPEN_WIZARD,
            icon: 'folder-open',
            type: MENU_DEF_TYPES.ITEM,
        },
        {
            id: MENUS.SHOW_CREATE_PROJECT_WIZARD,
            parent: MENUS.FILE_MENU,
            label: LABELS.SHOW_CREATE_PROJECT_WIZARD,
            isActive: (appContext) => {
                return true;
            },
            command: COMMANDS.SHOW_CREATE_PROJECT_WIZARD,
            icon: 'folder-open',
            type: MENU_DEF_TYPES.ITEM,
            divider: {
                after: true,
            },
        },
        {
            id: MENUS.SAVE_FILE,
            parent: MENUS.FILE_MENU,
            label: LABELS.SAVE,
            isActive: () => {
                const { editor } = workspaceManager.appContext;
                const activeTab = editor.getActiveEditor();
                return !_.isNil(activeTab) && !_.isNil(activeTab.file) && activeTab.isDirty;
            },
            command: COMMANDS.SAVE_FILE,
            icon: 'save',
            type: MENU_DEF_TYPES.ITEM,
        },
        {
            id: MENUS.SAVE_FILE_AS,
            parent: MENUS.FILE_MENU,
            label: LABELS.SAVE_AS,
            isActive: () => {
                const { editor } = workspaceManager.appContext;
                const activeTab = editor.getActiveEditor();
                return !_.isNil(activeTab) && !_.isNil(activeTab.file);
            },
            command: COMMANDS.SAVE_FILE_AS,
            icon: 'folder-open',
            type: MENU_DEF_TYPES.ITEM,
            divider: {
                after: true,
            },
        },
    ];
}
