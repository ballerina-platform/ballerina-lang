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

    var Variable_Tree = function(args, debuggerInstance){
        this._options = args;
        this.debugger = debuggerInstance;
    };

    Variable_Tree.prototype = Object.create(EventChannel.prototype);
    Variable_Tree.prototype.constructor = Variable_Tree;

    Variable_Tree.prototype.render = function () {
        this.renderHeader();
        this.renderContentDiv();
        return this;
    };

    Variable_Tree.prototype.renderHeader = function () {
        var container = $( '#' + _.get(this._options, 'containerId'));
        var headerContainer = $('<div class="panel-heading"><a class="collapsed" data-toggle="collapse" href="#debugger-variable-tree">Variables</a></div>');
        container.append(headerContainer);
    };

    Variable_Tree.prototype.renderContentDiv = function () {
        var container = $( '#' + _.get(this._options, 'containerId'));
        var contentContainer = $('<div id="debugger-variable-tree" class="panel-collapse collapse" role="tabpanel">' +
            '</div>');
        container.append(contentContainer);
    };

    Variable_Tree.prototype.onVariableTreeUpdate = function() {
        // TODO: update  #debugger-variable-tree on contentUpdate
    };


    return Variable_Tree;
});