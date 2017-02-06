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

        this.compiled = _.template(
                '<div class="debug-panel-header">'
              + '   <a class="tool-group-header-title">Frames</a><span class="collapse-icon fw fw-up"></span>'
              + '</div>'
              + '<table class="table table-condensed table-hover debug-frames">'
              + '<% _.forEach(frames, function(frame) { %>'
              + '<tr>'
              + '   <td>'
              + '       <a><%- frame.frameName %>'
              + '           <span class="debug-frame-pkg-name">'
              + '           <i class="fw fw-package"></i> <%- frame.packageName %>'
              + '           </span>'
              + '       <a>'
              + '   </td>'
              + '</tr>'
              + '<% }); %>'
              + '</table>');

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
        VariableTree.clear();
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
        VariableTree.render(message.frames[0]);
    };


    return (instance = (instance || new Frames() ));
});