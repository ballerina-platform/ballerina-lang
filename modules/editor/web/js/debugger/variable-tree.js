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

    var VariableTree = function (){

        this.compiled = _.template(
                '<div class="debug-panel-header">'
              + '   <a class="tool-group-header-title">Variables</a> (<%- frameName%>)<span class="collapse-icon fw fw-up"></span>'
              + '</div>'
              + '<div id="debug-v-tree">'
              + '<ul>'
              + '<% _.forEach(variables, function(v) { %>'
              + '<li>'
              + '    <strong><%- v.name %></strong> = <%- v.value %> (<%- v.type %>)'
              + '    <ul>'
              + '        <li>name : <%- v.name %></li>'
              + '        <li>type : <%- v.type %></li>'
              + '        <li>value : <%- v.value %></li>'
              + '        <li>scope : <%- v.scope %></li>'
              + '    </ul>'
              + '</li>'
              + '<% }); %>'
              + '</ul>'
              + '</div>');

        this.js_tree_options = {
            "core": {
                "themes":{
                    "icons":false
                }
            }
        };
    };

    VariableTree.prototype = Object.create(EventChannel.prototype);
    VariableTree.prototype.constructor = VariableTree;

    VariableTree.prototype.setContainer = function(container){
        this.container = container;
    };

    VariableTree.prototype.clear =function(message){
        this.container.empty();
    };

    VariableTree.prototype.render = function (frame) {
        frame.variables = (_.isNil(frame.variables))? []: frame.variables;
        var html = this.compiled(frame)
        this.container.html(html);
        $("#debug-v-tree").jstree(this.js_tree_options);
    };


    return (instance = (instance || new VariableTree() ));
});