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
    POPUP_DIALOG: 'popup-dialog',
    SHOW_DIALOG: 'show-dialog',
    SHOW_VIEW: 'show-view',
    HIDE_VIEW: 'hide-view',
    TOGGLE_BOTTOM_PANEL: 'toggle-bottom-panel',
    TOGGLE_LEFT_PANEL: 'toggle-left-panel',
    SHOW_BOTTOM_PANEL: 'show-bottom-panel',
    RE_RENDER_PLUGIN: 're-render-plugin',
    UPDATE_ALL_ACTION_TRIGGERS: 'update-all-action-triggers',
};

export const EVENTS = {
    TOGGLE_LEFT_PANEL: 'toggle-left-panel',
    TOGGLE_BOTTOM_PANEL: 'toggle-bottom-panel',
    SHOW_BOTTOM_PANEL: 'show-bottom-panel',
    SHOW_LEFT_PANEL: 'show-left-panel',
};

export const LABELS = {
    VIEW: 'View',
    TOGGLE_BOTTOM_PANLEL: 'Toggle Bottom Panel',
    TOGGLE_LEFT_PANLEL: 'Toggle Left Panel',
};

export const MENUS = {
    VIEW_MENU: 'composer.menu.layout.view',
    TOGGLE_BOTTOM_PANEL: 'composer.menu.layout.toggle-bottom-panel',
    TOGGLE_LEFT_PANEL: 'composer.menu.layout.toggle-left-panel',
};

export const HISTORY = {
    LEFT_PANEL_SIZE: 'left-split-pane-size',
    BOTTOM_PANEL_SIZE: 'bottom-split-pane-size',
    ACTIVE_LEFT_PANEL_VIEW: 'active-left-panel-view',
    ACTIVE_BOTTOM_PANEL_VIEW: 'active-bottom-panel-view',
    BOTTOM_PANEL_IS_ACTIVE: 'bottom-panel-is-active',
    LEFT_PANEL_IS_ACTIVE: 'left-panel-is-active',
};

export const REGIONS = {
    HEADER: 'composer.layout.region.header',
    TOOL_AREA: 'composer.layout.region.tool-area',
    ACTIVITY_BAR: 'composer.layout.region.activity-bar',
    LEFT_PANEL: 'composer.layout.region.left-panel',
    RIGHT_PANEL: 'composer.layout.region.right-panel',
    EDITOR_AREA: 'composer.layout.region.editor-area',
    EDITOR_TABS: 'composer.layout.region.editor-tabs',
    BOTTOM_PANEL: 'composer.layout.region.bottom-panel',
};

export const PLUGIN_ID = 'composer.plugin.layout.manager';
