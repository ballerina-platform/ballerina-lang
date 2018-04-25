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
    CREATE_NEW_FILE: 'create-new-file',
    OPEN_FILE: 'open-file',
    OPEN_FOLDER: 'open-folder',
    REMOVE_FOLDER: 'remove-folder',
    SAVE_FILE: 'save-file',
    SAVE_FILE_AS: 'save-as-file',
    SHOW_FILE_OPEN_WIZARD: 'show-file-open-wizard',
    SHOW_FOLDER_OPEN_WIZARD: 'show-folder-open-wizard',
    REFRESH_EXPLORER: 'refresh-explorer',
    GO_TO_FILE_IN_EXPLORER: 'go-to-file-in-explorer',
    REFRESH_PATH_IN_EXPLORER: 'refresh-path-in-explorer',
};

export const EVENTS = {
    FILE_OPENED: 'file-opened',
    FILE_UPDATED: 'file-updated',
    FILE_SAVED: 'file-saved',
    FILE_CLOSED: 'file-closed',
    DIRTY_STATE_CHANGE: 'dirty-state-change',
    CONTENT_MODIFIED: 'content-modified',
};

export const VIEWS = {
    EXPLORER: 'composer.view.workspace.explorer',
    EXPLORER_OPEN_ACTIVTY: 'composer.view.workspace.explorer.open.activity',
};

export const MENUS = {
    FILE_MENU: 'composer.menu.file',
    NEW_FILE: 'composer.menu.workspace.new-file',
    SHOW_FILE_OPEN_WIZARD: 'composer.menu.workspace.show-file-open-wizard',
    SHOW_FOLDER_OPEN_WIZARD: 'composer.menu.workspace.show-folder-open-wizard',
    SAVE_FILE: 'composer.menu.workspace.save-file',
    SAVE_FILE_AS: 'composer.menu.workspace.save-file-as',
};

export const TOOLS = {
    GROUP: 'composer.tool.group.file',
    NEW_FILE: 'composer.tool.workspace.new-file',
    SAVE_FILE: 'composer.tool.workspace.save-file',
    OPEN_FILE: 'composer.tool.workspace.open-file',
};

export const LABELS = {
    NEW_FILE: 'New File',
    SAVE: 'Save',
    SAVE_AS: 'Save As',
    FILE: 'File',
    SHOW_FILE_OPEN_WIZARD: 'Open File',
    SHOW_FOLDER_OPEN_WIZARD: 'Open Project Directory',
};

export const DIALOGS = {
    SAVE_FILE: 'composer.dialog.save-file',
    OPEN_FILE: 'composer.dialog.open-file',
    OPEN_FOLDER: 'composer.dialog.open-folder',
    REPLACE_FILE_CONFIRM: 'composer.dialog.replace-file-confirm',
    DELETE_FILE_CONFIRM: 'composer.dialog.delete-file-confirm',
};

export const HISTORY = {
    OPENED_FILES: 'composer.workspace.opened-files',
    OPENED_FOLDERS: 'composer.workspace.opened-workspace-folders',
};

export const PLUGIN_ID = 'composer.plugin.workspace.manager';
