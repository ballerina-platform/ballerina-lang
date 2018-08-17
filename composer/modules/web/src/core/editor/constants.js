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

export const COMMANDS = {
    OPEN_FILE_IN_EDITOR: 'open-file-in-editor',
    OPEN_CUSTOM_EDITOR_TAB: 'open-custom-editor-tab',
    UNDO: 'undo',
    REDO: 'redo',
    ACTIVATE_EDITOR_FOR_FILE: 'activate-editor-for-file',
    FORMAT: 'source-format',
};

export const EVENTS = {
    UPDATE_TAB_TITLE: 'update-tab-title',
    UNDO_EVENT: 'undo-event',
    REDO_EVENT: 'redo-event',
    ACTIVE_TAB_CHANGE: 'active-tab-change',
};

export const VIEWS = {
    EDITOR_TABS: 'composer.view.editor.tabs',
};


export const DIALOGS = {
    DIRTY_CLOSE_CONFIRM: 'composer.dialog.editor.dirty-file-close-confirm',
    OPENED_FILE_DELETE_CONFIRM: 'composer.dialog.editor.opened-file-delete-confirm',
    NON_EXISTENT_FILE_OPEN_CONFIRM: 'composer.dialog.editor.non-existent-file-open-confirm',
};

export const MENUS = {
    EDIT: 'composer.menu.editor.edit',
    UNDO: 'composer.menu.editor.undo',
    REDO: 'composer.menu.editor.redo',
    FORMAT: 'composer.menu.editor.format',
};

export const TOOLS = {
    UNDO_REDO_GROUP: 'composer.tool.group.editor.undo-redo',
    CODE_GROUP: 'composer.tool.group.editor.code ',
    UNDO: 'composer.tool.editor.undo',
    REDO: 'composer.tool.editor.redo',
    FORMAT: 'composer.tool.editor.format',
};

export const LABELS = {
    EDIT: 'Edit',
    UNDO: 'Undo',
    REDO: 'Redo',
    FORMAT: 'Reformat Code',
};

export const HISTORY = {
    ACTIVE_EDITOR: 'composer.plugin.editor.active-editor',
    PREVIEW_VIEW_IS_ACTIVE: 'preview-view-pane-is-active',
    PREVIEW_VIEW_PANEL_SIZE: 'preview-view-pane-size',
};

export const PLUGIN_ID = 'composer.plugin.editor';
