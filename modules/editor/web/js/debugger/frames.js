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
define(['jquery', 'backbone', 'lodash', 'log', 'event_channel', /** void module - jquery plugin **/ 'js_tree'], function ($, Backbone, _, log, EventChannel) {

    var instance;

    var Frames = function (){

    };

    Frames.prototype = Object.create(EventChannel.prototype);
    Frames.prototype.constructor = Frames;

    Frames.prototype.render = function (container) {
        this.renderHeader(container);
        this.renderContentDiv(container);
        return this;
    };

    Frames.prototype.renderHeader = function (container) {
        var headerContainer =
            $('<div class="panel-heading"><a class="collapsed" data-toggle="collapse" href="#debugger-variable-tree">Frames</a></div>');
        container.append(headerContainer);
    };

    Frames.prototype.renderContentDiv = function (container) {
        var contentContainer = $('<div id="debugger-variable-tree" class="panel-collapse collapse in" role="tabpanel">' +
            '</div>');
        container.append(contentContainer);
        this.updateFrames();
    };

    Frames.prototype.updateFrames = function () {
        // <table class="table table-condensed"></table>
    };

    return (instance = (instance || new Frames() ));
});