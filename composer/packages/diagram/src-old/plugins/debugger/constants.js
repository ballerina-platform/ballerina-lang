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

export const COMMANDS = {
    RUN: 'run',
    INPUT: 'input',
    RUN_WITH_DEBUG: 'debug',
    TOGGLE_DEBUGGER: 'toggle-debugger',
    TOGGLE_LAUNCHER: 'toggle-launcher',
    STEP_OVER: 'step-over',
    RESUME: 'resume',
    STEP_IN: 'step-in',
    STEP_OUT: 'step-out',
    STOP: 'stop',
    SHOW_LAUNCHER_CONFIG_DIALOG: 'show-launcher-config-dialog',
    SHOW_REMOTE_DEBUG_DIALOG: 'show-remote-debug-dialog',
};

export const VIEWS = {
    DEBUGGER_PANEL: 'composer.view.debugger.left-panel',
    DEBUGGER_CONSOLE: 'composer.view.debugger.console',
};

export const MENUS = {
    DEBUG_MENU: 'composer.menu.debugger.debug',
    RUN_START_MENU: 'composer.menu.debugger.run.start',
    DEBUG_START_MENU: 'composer.menu.debugger.debug.start',
    STOP_MENU: 'composer.menu.debugger.stop',
};

export const LABELS = {
    DEBUG: 'Run',
    DEBUG_START: 'Start Debugging',
    DEBUG_STOP: 'Stop Debugging',
};

export const DIALOG_IDS = {
    LAUNCHER_CONFIG: 'composer.dialog.laucher-config',
    REMOTE_DEBUG: 'composer.dialog.remote-debug',
};

export const TOOLS = {
    GROUP: 'composer.tool.group.debugger',
    RUN: 'composer.tool.debugger.run',
    STOP: 'composer.tool.debugger.stop-launcher',
    DEBUG: 'composer.tool.debugger.debug',
    DEBUG_STOP: 'composer.tool.debugger.stop-debugger',
    STEP_OVER: 'composer.tool.debugger.step-over',
    RESUME: 'composer.tool.debugger.resume',
    STEP_IN: 'composer.tool.debugger.step-in',
    STEP_OUT: 'composer.tool.debugger.step-out',
    SHOW_LAUNCHER_CONFIG_DIALOG: 'show-launcher-config-dialog',
};

export const PLUGIN_ID = 'composer.plugin.debugger';
