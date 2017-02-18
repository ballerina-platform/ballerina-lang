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
define(['jquery', 'backbone', 'lodash', 'log', 'event_channel', './debug-manager', './variable-tree'], 
    function ($, Backbone, _, log, EventChannel, DebugManager, VariableTree ) {

    var instance;

    var Frames = function (){

        var template = 
        '<div class="debug-panel-header">'+
        '   <a class="tool-group-header-title">Frames</a></span>'+
        '</div>'+
        '<div class="panel-group" id="frameAccordion">'+
        '<% _.forEach(frames, function(frame, index) { %>'+       
        '    <div class="panel panel-default">'+
        '      <div class="panel-heading">'+
        '        <h4 class="panel-title">'+
        '          <a data-toggle="collapse" data-parent="#frameAccordion" href="#<%- frame.frameName %>"><%- frame.frameName %>'+
        '           <span class="debug-frame-pkg-name">'+
        '           <i class="fw fw-package"></i> <%- frame.packageName %>'+
        '           </span>'+
        '          </a>'+
        '        </h4>'+
        '      </div>'+
        '      <div id="<%- frame.frameName %>" class="panel-collapse collapse <% if(index == 0){%>in<% } %>">'+
        '        <div class="panel-body">'+
        '        <div class="debug-v-tree">'+
        '          <ul>'+
        '          <% _.forEach(frame.variables, function(v) { %>'+
        '          <li>'+
        '          <strong><%- v.name %></strong> = <%- v.value %> (<%- v.type %>)'+
        '          <ul>'+
        '            <li>type : <%- v.type %></li>'+
        '            <li>scope : <%- v.scope %></li>'+
        '          </ul>'+
        '          </li>'+
        '          <% }); %>'+
        '          </ul>'+
        '        </div>'+        
        '        </div>'+
        '      </div>'+
        '    </div>'+
        '<% }); %>'+
        '</div>';
  

        this.compiled = _.template(template);

        this.js_tree_options = {
            "core": {
                "themes":{
                    "icons":false
                }
            }
        };

        DebugManager.on('debug-hit', _.bindKey(this,'render'));
        DebugManager.on('resume-execution', _.bindKey(this,'clear'));
    };

    Frames.prototype = Object.create(EventChannel.prototype);
    Frames.prototype.constructor = Frames;

    Frames.prototype.setContainer = function(container){
        this.container = container;
    };

    Frames.prototype.clear =function(message){
        this.container.empty();
    };

    Frames.prototype.render = function (message) {
        //clear duplicate main
        message.frames = _.uniqWith(message.frames, function(obj, other){
            if (_.isEqual(obj.frameName,other.frameName) && _.isEqual(obj.packageName,other.packageName))
                return true;
        });
        //reverse order
        message.frames = _.reverse(message.frames);

        var html = this.compiled(message);
        this.container.html(html);

        //render variables tree
        $(".debug-v-tree").jstree(this.js_tree_options);
    };


    return (instance = (instance || new Frames() ));
});