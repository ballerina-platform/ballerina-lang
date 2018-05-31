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
 * Provides menu definitions of editor plugin.
 *
 * @returns {Object[]} menu definitions.
 *
 */
export function getMenuDefinitions(plugin) {
    return [
        {
            id: MENUS.EDIT,
            label: LABELS.EDIT,
            isActive: () => {
                return true;
            },
            icon: '',
            order: 2,
            type: MENU_DEF_TYPES.ROOT,
        },
        {
            id: MENUS.UNDO,
            parent: MENUS.EDIT,
            label: LABELS.UNDO,
            isActive: () => {
                const { editor } = plugin.appContext;
                const activeEditor = editor.getActiveEditor();
                if (activeEditor && !_.isNil(activeEditor.undoManager)) {
                    return activeEditor.undoManager.hasUndo();
                }
                return false;
            },
            command: COMMANDS.UNDO,
            icon: 'undo',
            type: MENU_DEF_TYPES.ITEM,
        },
        {
            id: MENUS.REDO,
            parent: MENUS.EDIT,
            label: LABELS.REDO,
            isActive: () => {
                const { editor } = plugin.appContext;
                const activeEditor = editor.getActiveEditor();
                if (activeEditor && !_.isNil(activeEditor.undoManager)) {
                    return activeEditor.undoManager.hasRedo();
                }
                return false;
            },
            command: COMMANDS.REDO,
            icon: 'redo',
            type: MENU_DEF_TYPES.ITEM,
        },
        {
            id: MENUS.FORMAT,
            parent: MENUS.EDIT,
            label: LABELS.FORMAT,
            isActive: () => {
                const { editor } = plugin.appContext;
                const activeEditor = editor.getActiveEditor();
                return !_.isNil(activeEditor) && !_.isNil(activeEditor.file) && !_.isEmpty(activeEditor.file.content);
            },
            command: COMMANDS.FORMAT,
            icon: 'format',
            type: MENU_DEF_TYPES.ITEM,
            divider: {
                before: true,
            },
        },
    ];
}
