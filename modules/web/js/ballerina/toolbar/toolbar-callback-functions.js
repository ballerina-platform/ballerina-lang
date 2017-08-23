// noinspection JSAnnotator
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
import $ from 'jquery';
import alerts from 'alerts';
import LaunchManager from './../../launcher/launch-manager';
import DebugManager from './../../debugger/debug-manager';
import Tool from './../../debugger/tools';
import './toolbar.css';

export function isReadyToRun(tab) {
    if (typeof tab.getFile !== 'function') {
        return false;
    }
    const file = tab.getFile();
    if (file.isDirty()) {
        return false;
    }

    return true;
}

/* Start functions */
export function startFunction(app, tool, state) {
    const className = $('#startIcon').attr('class');
    if (className.search('stop') > 0) {
        LaunchManager.stopProgram();
    } else if (state === 'none') {
        state = 'block';
    } else {
        state = 'none';
    }

    const obj = {
        menuName: 'startMenu',
        state,
    };

    return obj;
}

export function startApplicationFunction(app) {
    const activeTab = app.tabController.getActiveTab();
    if (isReadyToRun(activeTab)) {
        const file = activeTab.getFile();
        LaunchManager.runApplication(file);
    } else {
        alerts.error('Save file before running application');
    }
}

export function startServiceFunction(app) {
    const activeTab = app.tabController.getActiveTab();
    if (isReadyToRun(activeTab)) {
        const file = activeTab.getFile();
        LaunchManager.runService(file);
    } else {
        alerts.error('Save file before running service');
    }
}

export function startConfigurationFunction() {
    this.appArgsDialog = $('#modalRunApplicationWithArgs');
    event.preventDefault();
    event.stopPropagation();
    this.appArgsDialog.modal('show');
}

/* Debug functions */

export function debugFunction(app, tool, state) {
    if (state === 'none') {
        state = 'block';
    } else {
        state = 'none';
    }
    const obj = {
        menuName: 'debugMenu',
        state,
    };
    return obj;
}

/* Show debugger toolbar*/
export function showDebuggerToolbar() {
    const debuggerTools = '<div id="debugger-tools"> ' +
        '<button id="debugStop" class="btn btn-default btn-debug-action-toolbar pull-left" data-action="Stop" ' +
        'title="Stop Debug ( Alt + P )"> <i class="fw fw-stop stop" /> </button> ' +
        '<button id="debugResume" type="button" class="btn btn-default btn-debug-action-toolbar pull-left" ' +
        'data-action="Resume" title="Resume ( Alt + R )"> <i class="fw fw-start resume" /> </button> ' +
        '<button id="debugStepOver" type="button" class="btn btn-default btn-debug-action-toolbar pull-left" ' +
        'data-action="StepOver" title="Step Over ( Alt + O )"> <i class="fw fw-stepover step" /> </button> ' +
        '<button id="debugStepIn" type="button" class="btn btn-default btn-debug-action-toolbar pull-left" ' +
        'data-action="StepIn" title="Step In ( Alt + I )"> <i class="fw fw-stepin step" /> </button> ' +
        '<button id="debugStepOut" type="button" class="btn btn-default btn-debug-action-toolbar pull-left" ' +
        'data-action="StepOut" title="Step Out ( Alt + U )"> <i class="fw fw-stepout step" /> </button> ' +
        '<div class="clearfix"> </div>';
    if ($('#debugger-tools').length === 0) {
        $('#debug-tool').after(debuggerTools);
    }
    const debuggerInstance = DebugManager.application.debugger;
    debuggerInstance._activateBtn.tab('show');
    const width = debuggerInstance.lastWidth || debuggerInstance._options.defaultWidth;
    debuggerInstance._$parent_el.parent().width(width);
    debuggerInstance._containerToAdjust.css('padding-left', width);
    debuggerInstance._verticalSeparator.css('left', width - debuggerInstance._options.separatorOffset);
}

export function removeDebuggingToolbar() {
    $('#debugger-tools').remove();
    const debuggerInstance = DebugManager.application.debugger;
    debuggerInstance._$parent_el.parent().width('0px');
    debuggerInstance._containerToAdjust.css('padding-left', debuggerInstance._options.leftOffset);
    debuggerInstance._verticalSeparator.css('left',
        debuggerInstance._options.leftOffset - debuggerInstance._options.separatorOffset);
    debuggerInstance._activateBtn.parent('li').removeClass('active');
}

export function addStopApplication() {
    $('#startIcon').removeClass('fw-start');
    $('#startIcon').addClass('fw-stop stop');
}

export function removeStopApplication() {
    $('#startIcon').removeClass('fw-stop stop');
    $('#startIcon').addClass('fw-start');
}

$('body').on('click', '#debugStop', () => {
    DebugManager.stop();
    removeDebuggingToolbar();
});

$('body').on('click', '#debugResume', () => {
    DebugManager.resume();
    if (DebugManager.active === false) {
        removeDebuggingToolbar();
    }
});

$('body').on('click', '#debugStepOver', () => {
    DebugManager.stepOver();
    if (DebugManager.active === false) {
        removeDebuggingToolbar();
    }
});

$('body').on('click', '#debugStepIn', () => {
    DebugManager.stepIn();
    if (DebugManager.active === false) {
        removeDebuggingToolbar();
    }
});

$('body').on('click', '#debugStepOut', () => {
    DebugManager.stepOut();
    if (DebugManager.active === false) {
        removeDebuggingToolbar();
    }
});

export function debugApplicationFunction(app) {
    const activeTab = app.tabController.getActiveTab();
    if (isReadyToRun(activeTab)) {
        showDebuggerToolbar();
        Tool.showWaiting();
        const file = activeTab.getFile();
        LaunchManager.debugApplication(file);
    } else {
        alerts.error('Save file before start debugging application');
    }
}

export function debugServiceFunction(app) {
    const activeTab = app.tabController.getActiveTab();
    if (isReadyToRun(activeTab)) {
        showDebuggerToolbar();
        Tool.showWaiting();
        const file = activeTab.getFile();
        LaunchManager.debugService(file);
    } else {
        alerts.error('Save file before start debugging service');
    }
}

export function remoteDebugFunction() {
    this.connectionDialog = $('#modalDebugConnection');
    this.connectionDialog.modal('show');
}

export function debugConfigurationFunction() {
    this.appArgsDialog = $('#modalRunApplicationWithArgs');
    this.appArgsDialog.modal('show');
}

/* New file */
export function newFileFunction(app, tool, state) {
    app.commandManager.dispatch(tool.command.id);
}

/* Open file */
export function openFileFunction(app, tool, state) {
    app.commandManager.dispatch(tool.command.id);
}

/* Save file */
export function saveFileFunction(app, tool, state) {
    app.commandManager.dispatch(tool.command.id);
}

/* Swagger Editor */
export function swaggerEditorFunction(app, tool, state) {
    app.commandManager.dispatch(tool.command.id);
}

/* Undo */
export function undoFunction(app, tool, state) {
    app.commandManager.dispatch(tool.command.id);
}

/* Redo */
export function redoFunction(app, tool, state) {
    app.commandManager.dispatch(tool.command.id);
}

/* Reset Menu */
export function resetMenu() {
    const dropdowns = document.getElementsByClassName('dropdown-content');
    let i;
    for (i = 0; i < dropdowns.length; i++) {
        dropdowns[i].style.display = 'none';
    }
}

