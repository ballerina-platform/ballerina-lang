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

export const PLUGIN_ID = 'composer.plugin.lang.ballerina';

export const EDITOR_ID = 'composer.editor.ballerina';

export const DOC_VIEW_ID = 'composer.view.ballerina.docs';

export const COMMANDS = {
    DIAGRAM_MODE_CHANGE: 'diagram-mode-change',
};

export const TOOLS = {
    GROUP: 'composer.tool.group.ballerina',
    DEFAULT_VIEWS: 'composer.tool.ballerina.default-view',
    ACTION_VIEW: 'composer.tool.ballerina.action-view',
    COMPACT_VIEW: 'composer.tool.ballerina.compact-view',
};

export const DIALOGS = {
    OPEN_PROGRAM_DIR_CONFIRM: 'composer.dialog.ballerina.open-program-dir-confirm',
    FIX_PACKAGE_NAME_OR_PATH_CONFIRM: 'composer.dialog.ballerina.fix-package-name-or-path-confirm',
    INVALID_SWAGGER_DIALOG: 'composer.dialog.ballerina.invalid-swagger-dialog',
};

export const EVENTS = {
    ACTIVE_BAL_AST_CHANGED: 'active-bal-ast-changed',
    UPDATE_PACKAGE_DECLARATION: 'update-bal-file-package-declaration',
    SCROLL_DESIGN_VIEW: 'scroll-design-view',
};

export const RESPOSIVE_MENU_TRIGGER = {
    ICON_MODE: 650,
    HIDDEN_MODE: 460,
};

export const ACTION_BOX_POSITION = {
    SINGLE_ACTION_OFFSET: 12,
};

export const VIEWS = {
    TOOL_PALLETE: 'composer.views.ballerina.tool-pallette',
};
