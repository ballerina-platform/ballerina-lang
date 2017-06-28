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
import _ from 'lodash';
import EventChannel from 'event_channel';
import alerts from 'alerts';
import DebugManager from './debug-manager';

/**
 * @description Debugger toolbar
 * @class Tools
 * @extends {EventChannel}
 */
class Tools extends EventChannel {
    /**
     * Creates an instance of Tools.
     *
     * @memberof Tools
     */
    constructor() {
        super();
        this.compiled = _.template(
            `<% if (!active) { %>
                <div class="debug-panel-header">
                    <span class="tool-group-header-title">Debug</span>
                </div>
                <div class="btn-group col-xs-12">
                    <div type="button" 
                        class="btn btn-default text-left btn-debug-activate col-xs-12"
                        id="debug_application"
                        title="Start Debugging Application">
                        <span class="launch-label">Application</span>
                            <button type="button" class="btn btn-default pull-right btn-config" title="Config">
                                <i class="fw fw-configarations"></i>
                            </button>
                    </div>
                    <button type="button"
                        class="btn btn-default text-left btn-debug-activate col-xs-12"
                        id="debug_service" 
                        title="Start Debugging Service">
                        Service
                    </button>
                    <button type="button"
                        class="btn btn-default text-left btn-debug-activate col-xs-12"
                        id="remote_debug" title="Start Debugging Remotely">
                        Debug Remotely
                    </button>
                </div>
            <% } %>
            <% if (active) { %>
                <div class="debug-panel-header">
                    <span class="tool-group-header-title">Debugger Active</span></span>
                </div>
                <div>
                    <button type="button"
                        class="btn btn-default btn-debug-action pull-left"
                        data-action="Stop"  
                        title="Stop Debug ( Alt + P )"
                    >
                        <i class="fw fw-stop" />
                    </button>
                    <button type="button"
                        class="btn btn-default btn-debug-action pull-left <% if (!navigation) { %>disabled<%}%>"
                        data-action="Resume"  title="Resume ( Alt + R )">
                        <i class="fw fw-start " />
                    </button>
                    <button type="button"
                        class="btn btn-default btn-debug-action pull-left <% if (!navigation) { %>disabled<%}%>"
                        data-action="StepOver"  title="Step Over ( Alt + O )">
                        <i class="fw fw-stepover " />
                    </button>
                    <button 
                        type="button" 
                        class="btn btn-default btn-debug-action pull-left <% if (!navigation) { %>disabled<%}%>"
                        data-action="StepIn"  
                        title="Step In ( Alt + I )">
                        <i class="fw fw-stepin " />
                    </button>
                    <button type="button" 
                        class="btn btn-default btn-debug-action pull-left <% if (!navigation) { %>disabled<%}%>"
                        data-action="StepOut"  title="Step Out ( Alt + U )">
                        <i class="fw fw-stepout " />
                    </button>
                    <div class="clearfix">
                </div>
            <% } %>
              `);

        this.connectionDialog = $('#modalDebugConnection');
        this.appArgsDialog = $('#modalRunApplicationWithArgs');
        this.navigation = false;

        $('.debug-connect-button').on('click', () => { this.connect(); });
        DebugManager.on('session-terminated', () => { this.connectionError(); });
        DebugManager.on('session-started', () => { this.connectionStarted(); });
        DebugManager.on('session-ended', () => { this.render(); });
        DebugManager.on('debug-hit', () => { this.enableNavigation(); });
        DebugManager.on('resume-execution', () => { this.disableNavigation(); });
    }
    /**
     *
     * @description show set debugger arguments dialog
     * @param {Object} ui configs
     *
     * @memberof Tools
     */
    setArgs(args) {
        this.container = args.container;
        this.launchManager = args.launchManager;
        this.application = args.application;
        this.toolbarShortcuts = args.toolbarShortcuts;

        this.container.on('click', '.btn-debug-action', (event) => { this.handleMouseAction(event); });

        this.container.on('click', '#debug_application', (event) => { this.debugApplication(event); });
        this.container.on('click', '#debug_service', (event) => { this.debugService(event); });

        this.container.on('click', '.btn-config', (e) => {
            e.preventDefault();
            e.stopPropagation();
            this.appArgsDialog.modal('show');
        });

        $('#form-run-application-with-args').submit((e) => {
            e.preventDefault();
            const newArgs = $(this).serializeArray().map(input => input.value)
                                .join(' ')
                                .trim();
            const activeTab = this.application.tabController.getActiveTab();
            if (activeTab && activeTab.getFile()) {
                const id = activeTab.getFile().id;
                this.application.browserStorage.put(`launcher-app-configs-${id}`, newArgs);
            }
            this.appArgsDialog.modal('hide');
        });

        const wrapper = $('#form-run-application-with-args .input_fields_wrap');
        $('#form-run-application-with-args .add_field_button').on('click', () => {
            $(wrapper).append(`<div class="removable">
                <input type="text" name="applicationArgs[]" class="form-control"/>
                <button class="remove_field btn-file-dialog">Remove</button>
            </div>`);
        });

        $(wrapper).on('click', '.remove_field', function (e) {
            e.preventDefault();
            $(this).parent('div').remove();
        });

        this.appArgsDialog.on('shown.bs.modal', () => {
            $('#form-run-application-with-args .removable').remove();
            const activeTab = this.application.tabController.getActiveTab();
            if (activeTab && activeTab.getFile()) {
                const { id } = activeTab.getFile();
                const argsString = this.application.browserStorage.get(`launcher-app-configs-${id}`) || '';
                const currentArgs = argsString.split(' ');
                _.each(currentArgs, (arg, i) => {
                    if (i === 0) {
                        $('#form-run-application-with-args input[type=\'text\']').get(0).value = arg;
                    } else {
                        $(wrapper).append(`<div class="removable">
                            <input type="text" name="applicationArgs[]" class="form-control" value="${arg}"/>
                            <button class="remove_field btn-file-dialog">Remove</button>
                        </div>`);
                    }
                });
            }
        });

        this.container.on('click', '#remote_debug', () => {
            $('.debug-connection-group').removeClass('has-error');
            $('.debug-connection-error').addClass('hide');
            this.connectionDialog.modal('show');
        });
    }
    /**
     * @description renders debugger toolbar
     * @memberof Tools
     */
    render() {
        const context = {};
        context.active = DebugManager.active;
        context.navigation = this.navigation;
        this.container.html(this.compiled(context));
        $('.btn-debug-activate').tooltip();
    }
    /**
     *
     * handles all mouse click/ keyboard shortcut events on debugger toolbar and delegates to handleAction
     * @param {Object} event - dom event
     *
     * @memberof Tools
     */
    handleMouseAction(event) {
        const actionName = $(event.currentTarget).data('action');
        this.application.commandManager.dispatch(actionName);
    }

    /**
     * Handle trigger action from CommandManager
     * @param {any} actionName - Name of the action triggered
     * @memberof Tools
     */
    handleAction(actionName) {
        let action = () => {};
        switch (actionName) {
        case 'Resume':
            action = DebugManager.resume.bind(DebugManager);
            break;
        case 'StepOver':
            action = DebugManager.stepOver.bind(DebugManager);
            break;
        case 'StepIn':
            action = DebugManager.stepIn.bind(DebugManager);
            break;
        case 'StepOut':
            action = DebugManager.stepOut.bind(DebugManager);
            break;
        case 'Stop':
            action = DebugManager.stop.bind(DebugManager);
            break;
        default:
            throw Error('Unknown action');
        }

        return () => {
            if (this.navigation) {
                action();
            }
            if (!this.navigation && actionName === 'Stop') {
                action();
            }
        };
    }
    /**
     *
     * Connect to a remote Debugging Url submited by user
     *
     * @memberof Tools
     */
    connect() {
        $('.debug-connection-group').removeClass('has-error');
        $('.debug-connection-error').addClass('hide');
        const debugUrl = `ws://${$('#debugUrl').val()}/debug`;
        try {
            DebugManager.connect(debugUrl);
        } catch (error) {
            this.connectionError();
        }
    }

    /**
     * Display an error if could not connect to remote debugging Url
     *
     *
     * @memberof Tools
     */
    connectionError() {
        $('.debug-connection-group').addClass('has-error');
        $('.debug-connection-error').removeClass('hide');
        this.render();
    }
    /**
     * Handle Debugging connection start
     *
     * @memberof Tools
     */
    connectionStarted() {
        this.render();

        _.each(this.toolbarShortcuts, (commandInfo) => {
            this.application.commandManager.registerCommand(commandInfo.id, { shortcuts: commandInfo.shortcuts });
            this.application.commandManager.registerHandler(commandInfo.id, this.handleAction(commandInfo.id), this);
        });

        this.connectionDialog.modal('hide');
        DebugManager.publishBreakpoints();
        DebugManager.startDebug();
    }
    /**
     * Handle start debugging application
     *
     * @memberof Tools
     */
    debugApplication() {
        const activeTab = this.application.tabController.getActiveTab();
        if (this.isReadyToRun(activeTab)) {
            const file = activeTab.getFile();
            activeTab._fileEditor.publishBreakpoints();
            this.launchManager.debugApplication(file);
        } else {
            alerts.error('Save file before start debugging application');
        }
    }
    /**
     * Handle start debugging service
     *
     * @memberof Tools
     */
    debugService() {
        const activeTab = this.application.tabController.getActiveTab();
        if (this.isReadyToRun(activeTab)) {
            const file = activeTab.getFile();
            activeTab._fileEditor.publishBreakpoints();
            this.launchManager.debugService(file);
        } else {
            alerts.error('Save file before start debugging service');
        }
    }
    /**
     *
     * Returns true if file is saved and read to run
     * @param {FileTab} tab
     * @returns Boolean
     *
     * @memberof Tools
     */
    isReadyToRun(tab) {
        if (typeof tab.getFile !== 'function') {
            return false;
        }

        const file = tab.getFile();
        // file is not saved give an error and avoid running
        if (file.isDirty()) {
            return false;
        }

        return true;
    }
    /**
     * Enables Debugging Navigation
     *
     * @memberof Tools
     */
    enableNavigation() {
        this.navigation = true;
        this.render();
    }
    /**
     * Disables Debugging Navigation
     *
     * @memberof Tools
     */
    disableNavigation() {
        this.navigation = false;
        this.render();
    }
}

export default new Tools();
