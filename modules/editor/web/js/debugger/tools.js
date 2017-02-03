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

define(['jquery', 'backbone', 'lodash', 'log', 'event_channel'], function ($, Backbone, _, log, EventChannel) {

    var instance;

    var Tools = function(){
        this._contentContainer = $('<div class="btn-group">' +
            '<button type="button" class="btn btn-default btn-activate"><i class="fw fw-activate"></i> Start Debugging</button>' +
            '<button type="button" class="btn btn-default btn-action hidden" data-action="StepOver"><i class="fw fw-redo"></i></button>' +
            '<button type="button" class="btn btn-default btn-action hidden" data-action="StepIn"><i class="fw fw-down-arrow"></i></button>' +
            '<button type="button" class="btn btn-default btn-action hidden" data-action="StepOut"><i class="fw fw-right-arrow"></i></button>' +
            '<button type="button" class="btn btn-default btn-action hidden" data-action="Resume"><i class="fw fw-refresh"></i></button>' +
            '<button type="button" class="btn btn-default btn-action hidden" data-action="Stop"><i class="fw fw-square"></i></button>' +
            '</div>');
    };

    Tools.prototype = Object.create(EventChannel.prototype);
    Tools.prototype.constructor = Tools;

    Tools.prototype.render = function (container) {
        var self = this;

        this._contentContainer.find('.btn-action').on('click', function () {
            var actionName = $(this).data('action');
            self.trigger('execute-action', actionName);
        });

        this._contentContainer.find('.btn-activate').on('click', function () {
            var actionName = $(this).data('action');
            self.trigger('show-connection-dialog');
        });

        container.append(this._contentContainer);
    };

    Tools.prototype.startDebugging = function () {
        this._contentContainer.find('.btn-action').removeClass('hidden');
        this._contentContainer.find('.btn-activate').addClass('hidden');
    };


    return (instance = (instance || new Tools() ));
});