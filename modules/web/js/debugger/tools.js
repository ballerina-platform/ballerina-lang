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
import Backbone from 'backbone';
import _ from 'lodash';
import log from 'log';
import EventChannel from 'event_channel';
import DebugManager from './debug-manager';
import alerts from 'alerts';
import Mousetrap from 'mousetrap';

var instance;

class Tools extends EventChannel {
    constructor() {
        this.compiled = _.template('<% if (!active) { %>'
            + '<div class="debug-panel-header">'
            + '     <span class="tool-group-header-title">Debug</span>'
            + '</div>'
            + '<div class="btn-group col-xs-12">'
            + '     <div type="button" class="btn btn-default text-left btn-debug-activate col-xs-12" id="debug_application" title="Start Debugging Application"><span class="launch-label">Application</span><button type="button" class="btn btn-default pull-right btn-config" title="Config"><i class="fw fw-configarations"></i></button></div>'
            + '     <button type="button" class="btn btn-default text-left btn-debug-activate col-xs-12" id="debug_service" title="Start Debugging Service">Service</button>'
            + '     <button type="button" class="btn btn-default text-left btn-debug-activate col-xs-12" id="remote_debug" title="Start Debugging Remotely">Debug Remotely</button>'
            + '</div>'
            + '<% } %>'
            + '<% if (active) { %>'
            + '<div class="debug-panel-header">'
            + '     <span class="tool-group-header-title">Debugger Active</span></span>'
            + '</div>'
            + '<div class="">'
            + '<button type="button" class="btn btn-default btn-debug-action" data-action="Stop"  title="Stop Debug ( Alt + P )"><i class="fw fw-stop" /></button>'
            + '<button type="button" class="btn btn-default btn-debug-action <% if (!navigation) { %>disabled<%}%>" data-action="Resume"  title="Resume ( Alt + R )"><i class="fw fw-start " /></button>'
            + '<button type="button" class="btn btn-default btn-debug-action <% if (!navigation) { %>disabled<%}%>" data-action="StepOver"  title="Step Over ( Alt + O )"><i class="fw fw-stepover " /></button>'
            + '<button type="button" class="btn btn-default btn-debug-action <% if (!navigation) { %>disabled<%}%>" data-action="StepIn"  title="Step In ( Alt + I )"><i class="fw fw-stepin " /></button>'
            + '<button type="button" class="btn btn-default btn-debug-action <% if (!navigation) { %>disabled<%}%>" data-action="StepOut"  title="Step Out ( Alt + U )"><i class="fw fw-stepout " /></button>'
            + '</div><% } %>');

        this.connectionDialog = $("#modalDebugConnection");
        this.appArgsDialog = $("#modalRunApplicationWithArgs");
        this.navigation = false;

        $('.debug-connect-button').on("click", _.bindKey(this, 'connect'));
        DebugManager.on("session-terminated", _.bindKey(this, 'connectionError'));
        DebugManager.on("session-started", _.bindKey(this, 'connectionStarted'));
        DebugManager.on("session-ended",_.bindKey(this, 'render'));
        DebugManager.on("debug-hit",_.bindKey(this, 'enableNavigation'));
        DebugManager.on("resume-execution",_.bindKey(this, 'disableNavigation'));
        DebugManager.on("session-completed",_.bindKey(this, 'disableNavigation'));
    }

    setArgs(args) {
        var self = this;
        this.container = args.container;
        this.launchManager = args.launchManager;
        this.application = args.application;
        this.toolbarShortcuts = args.toolbarShortcuts;

        this.container.on('click', '.btn-debug-action', _.bindKey(this, 'handleMouseAction'));

        this.container.on('click', '#debug_application', _.bindKey(this, 'debugApplication'));
        this.container.on('click', '#debug_service', _.bindKey(this, 'debugService'));

        this.container.on('click', '.btn-config', function(e) {
            e.preventDefault();
            e.stopPropagation();
            self.appArgsDialog.modal('show');
        });

        $('#form-run-application-with-args').submit(function(e) {
            e.preventDefault();
            var args = _.map($(this).serializeArray(), function(input) {
                return input.value;
            }).join(' ').trim();
            var activeTab = self.application.tabController.getActiveTab();
            if(activeTab && activeTab.getFile()) {
                var id = activeTab.getFile().id;
                self.application.browserStorage.put('launcher-app-configs-' + id, args);
            }
            self.appArgsDialog.modal('hide');
        });

        var wrapper = $("#form-run-application-with-args .input_fields_wrap");
        $('#form-run-application-with-args .add_field_button').on('click', function() {
            $(wrapper).append('<div class="removable"><input type="text" name="applicationArgs[]" class="form-control"/><button class="remove_field btn-file-dialog">Remove</button></div>');
        });

        $(wrapper).on("click",".remove_field", function(e){
            e.preventDefault();
            $(this).parent('div').remove();
        });

        this.appArgsDialog.on('shown.bs.modal', function() {
            $('#form-run-application-with-args .removable').remove();
            var activeTab = self.application.tabController.getActiveTab();
            if(activeTab && activeTab.getFile()) {
                var id = activeTab.getFile().id;
                var args = (self.application.browserStorage.get('launcher-app-configs-' + id) || "").split(" ");
                _.each(args, function(arg, i) {
                    if(i === 0) {
                        $("#form-run-application-with-args input[type='text']").get(0).value = arg;
                    } else {
                        $(wrapper).append('<div class="removable"><input type="text" name="applicationArgs[]" class="form-control" value="' + arg
                        + '"/><button class="remove_field btn-file-dialog">Remove</button></div>');
                    }
                });
            }
        });

        this.container.on('click', '#remote_debug', function () {
            $('.debug-connection-group').removeClass("has-error");
            $('.debug-connection-error').addClass("hide");
            self.connectionDialog.modal('show');
        });
    }

    render() {
        var context = {};
        context.active = DebugManager.active;
        context.navigation = this.navigation;
        this.container.html(this.compiled(context));
        $('.btn-debug-activate').tooltip();
    }

    handleMouseAction(event) {
        var actionName = $(event.currentTarget).data('action');
        this.application.commandManager.dispatch(actionName);
    }

    handleAction(actionName) {
        switch(actionName){
            case 'Resume':
                var action = DebugManager.resume.bind(DebugManager);
                break;
            case 'StepOver':
                var action = DebugManager.stepOver.bind(DebugManager);
                break;
            case 'StepIn':
                var action = DebugManager.stepIn.bind(DebugManager);
                break;
            case 'StepOut':
                var action = DebugManager.stepOut.bind(DebugManager);
                break;
            case 'Stop':
                var action = DebugManager.stop.bind(DebugManager);
                break;
        }

        return function() {
            if(this.navigation) {
                action();
            }
            if(!this.navigation && actionName == "Stop") {
                action();
            }
        }

    }

    connect() {
        $('.debug-connection-group').removeClass("has-error");
        $('.debug-connection-error').addClass("hide");
        DebugManager.connect($("#debugUrl").val());
    }

    connectionError() {
        $('.debug-connection-group').addClass("has-error");
        $('.debug-connection-error').removeClass("hide");
        this.render();
    }

    connectionStarted() {
        var self = this;
        this.render();

        _.each(this.toolbarShortcuts, function(commandInfo) {
            self.application.commandManager.registerCommand(commandInfo.id, {shortcuts: commandInfo.shortcuts});
            self.application.commandManager.registerHandler(commandInfo.id, self.handleAction(commandInfo.id), self);
        });

        this.connectionDialog.modal('hide');
        DebugManager.publishBreakPoints();
        DebugManager.startDebug();
    }

    debugApplication() {
        var activeTab = this.application.tabController.getActiveTab();
        if(this.isReadyToRun(activeTab)) {
            var file = activeTab.getFile();
            this.launchManager.debugApplication(file);
        } else {
            alerts.error("Save file before start debugging application");
        }
    }

    debugService() {
        var activeTab = this.application.tabController.getActiveTab();
        if(this.isReadyToRun(activeTab)) {
            var file = activeTab.getFile();
            this.launchManager.debugService(file);
        } else {
            alerts.error("Save file before start debugging service");
        }
    }

    isReadyToRun(tab) {
       if (!typeof tab.getFile === "function") {
           return false;
       }

       var file = tab.getFile();
       // file is not saved give an error and avoid running
       if(file.isDirty()) {
           return false;
       }

       return true;

    }

    enableNavigation(message) {
        this.navigation = true;
        this.render();
    }

    disableNavigation() {
        this.navigation = false;
        this.render();
    }
}

export default new Tools();
