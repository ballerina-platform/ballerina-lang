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

define(['jquery', 'backbone', 'lodash', 'log', 'event_channel', './debug-manager', 'alerts'], function ($, Backbone, _, log, EventChannel, DebugManager, alerts) {

    var instance;

    var Tools = function(){
        this.compiled = _.template('<% if (!active) { %>'
            + '<div class="debug-panel-header">'
            + '     <span class="tool-group-header-title">Debug</span></span>' 
            + '</div>' 
            + '<div class="btn-group col-xs-12">' 
            + '     <button type="button" class="btn btn-default text-left btn-debug-activate col-xs-12" id="debug_application" title="Start Debug">Application</button>' 
            + '     <button type="button" class="btn btn-default text-left btn-debug-activate col-xs-12" id="debug_service" title="Start Debug">Service</button>' 
            + '     <button type="button" class="btn btn-default text-left btn-debug-activate col-xs-12" id="remote_debug" title="Start Debug">Debug Remotely</button>' 
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
        this.navigation = false;

        $('.debug-connect-button').on("click", _.bindKey(this, 'connect'));  
        DebugManager.on("session-terminated", _.bindKey(this, 'connectionError'));
        DebugManager.on("session-started",_.bindKey(this, 'connectionStarted'));      
        DebugManager.on("session-ended",_.bindKey(this, 'render'));
        DebugManager.on("debug-hit",_.bindKey(this, 'enableNavigation'));
        DebugManager.on("resume-execution",_.bindKey(this, 'disableNavigation'));
    };

    Tools.prototype = Object.create(EventChannel.prototype);
    Tools.prototype.constructor = Tools;

    Tools.prototype.setArgs = function(args){
        var self = this;
        this.container = args.container;
        this.launchManager = args.launchManager;
        this.application = args.application;

        this.container.on('click', '.btn-debug-action', _.bindKey(this, 'handleActions'));

        this.container.on('click', '#debug_application', _.bindKey(this, 'debugApplication'));
        this.container.on('click', '#debug_service', _.bindKey(this, 'debugService'));

        this.container.on('click', '#remote_debug', function () {
            $('.debug-connection-group').removeClass("has-error");
            $('.debug-connection-error').addClass("hide");
            self.connectionDialog.modal('show');
        });        
    }

    Tools.prototype.render = function () {
        var context = {};
        context.active = DebugManager.active;
        context.navigation = this.navigation;
        this.container.html(this.compiled(context));
    };

    Tools.prototype.handleActions = function(event){
        if($(event.currentTarget).hasClass('disabled')){
            return;
        } 
        var actionName = $(event.currentTarget).data('action');
        switch(actionName){
            case 'Resume':
                DebugManager.resume();
                break;
            case 'StepOver':
                DebugManager.stepOver();
                break;
            case 'StepIn':
                DebugManager.stepIn();
                break;
            case 'StepOut':
                DebugManager.stepOut();
                break;
            case 'Stop':
                DebugManager.stop();
                break;
        }
    };

    Tools.prototype.connect = function(){
        $('.debug-connection-group').removeClass("has-error");
        $('.debug-connection-error').addClass("hide");        
        DebugManager.connect($("#debugUrl").val());
    };

    Tools.prototype.connectionError = function(){
        $('.debug-connection-group').addClass("has-error");
        $('.debug-connection-error').removeClass("hide");
        this.render();
    };

    Tools.prototype.connectionStarted = function(){
        this.render();
        this.connectionDialog.modal('hide');
        DebugManager.publishBreakPoints();
        DebugManager.startDebug();
    };

    Tools.prototype.debugApplication = function(){
        var activeTab = this.application.tabController.getActiveTab();
        if(this.isReadyToRun(activeTab)) {
            var file = activeTab.getFile();
            this.launchManager.debugApplication(file);
        } else {
            alerts.error("Save file before start debugging application");
        }
    };     

    Tools.prototype.debugService = function(){
        var activeTab = this.application.tabController.getActiveTab();
        if(this.isReadyToRun(activeTab)) {
            var file = activeTab.getFile();
            this.launchManager.debugService(file);
        } else {
            alerts.error("Save file before start debugging service");
        }
    };

    Tools.prototype.isReadyToRun = function(tab) {
       if (!typeof tab.getFile === "function") {
           return false;
       }

       var file = tab.getFile();
       // file is not saved give an error and avoid running
       if(file.isDirty()) {
           return false;
       }

       return true;

    };

    Tools.prototype.enableNavigation = function(message) {
        this.navigation = true;
        this.render();        
    };

    Tools.prototype.disableNavigation = function() {
        this.navigation = false;
        this.render();
    };    

    return (instance = (instance || new Tools() ));
});