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

define(['jquery', 'backbone', 'lodash', 'log', 'event_channel', './debug-manager'], function ($, Backbone, _, log, EventChannel, DebugManager) {

    var instance;

    var Tools = function(){
        this._contentContainer = $('<div class="btn-group">' +
            '<button type="button" class="btn btn-default btn-debug-activate col-xs-12" title="Start Debug ( Alt + C )">Remote Debug</button>' +
            '<button type="button" class="btn btn-default btn-debug-action disabled" data-action="Stop"  title="Stop Debug ( Alt + P )"><i class="fw fw-stop" /></button>' +
            '<button type="button" class="btn btn-default btn-debug-action disabled" data-action="Resume"  title="Resume ( Alt + R )"><i class="fw fw-start " /></button>' +
            '<button type="button" class="btn btn-default btn-debug-action disabled" data-action="StepOver"  title="Step Over ( Alt + O )"><i class="fw fw-stepover " /></button>' +
            '<button type="button" class="btn btn-default btn-debug-action disabled" data-action="StepIn"  title="Step In ( Alt + I )"><i class="fw fw-stepin " /></button>' +
            '<button type="button" class="btn btn-default btn-debug-action disabled" data-action="StepOut"  title="Step Out ( Alt + U )"><i class="fw fw-stepout " /></button>' +
            '</div>');

        this.connectionDialog = $("#modalDebugConnection");

        $('.debug-connect-button').on("click", _.bindKey(this, 'connect'));  
        DebugManager.on("session-terminated", _.bindKey(this, 'connectionError'));
        DebugManager.on("session-started",_.bindKey(this, 'connectionStarted'));      
    };

    Tools.prototype = Object.create(EventChannel.prototype);
    Tools.prototype.constructor = Tools;

    Tools.prototype.render = function (container) {
        var self = this;

        this._contentContainer.find('.btn-debug-action').on('click', _.bindKey(this, 'handleActions'));

        this._contentContainer.find('.btn-debug-activate').on('click', function () {
            $('.debug-connection-group').removeClass("has-error");
            $('.debug-connection-error').addClass("hide");
            self.connectionDialog.modal('show');
        });

        container.append(this._contentContainer);
    };

    Tools.prototype.handleActions = function(event){
        if($(event.target).hasClass("disabled"))return;
        var actionName = $(event.target).data('action');
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
        $('.btn-debug-activate').removeClass('disabled');        
        $('.debug-connection-group').addClass("has-error");
        $('.debug-connection-error').removeClass("hide");
        $('.btn-debug-action').addClass('disabled');
    };

    Tools.prototype.connectionStarted = function(){
        $('.btn-debug-activate').addClass('disabled');
        $('.btn-debug-action').removeClass('disabled');
        this.connectionDialog.modal('hide');
        DebugManager.publishBreakPoints();
        DebugManager.startDebug();
    };       

    return (instance = (instance || new Tools() ));
});