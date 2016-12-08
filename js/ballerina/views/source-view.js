/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['log', 'lodash', 'jquery', 'event_channel', 'ace/ace'], function(log, _, $, EventChannel, ace) {

    /**
     * @class SourceView
     * @augments EventChannel
     * @constructor
     * @class SourceView  Wraps the Ace editor for source view
     * @param {Object} args - Rendering args for the view
     * @param {String} args.container - selector for div element to render ace editor
     * @param {String} [args.content] - initial content for the editor
     */
    var SourceView = function (args) {
        this._options = args;
        if(!_.has(args, 'container')){
            log.error('container is now specified for rendering source view.')
        }
        this._container = _.get(args, 'container');
        this._content = _.get(args, 'content');
    };

    SourceView.prototype = Object.create(EventChannel.prototype);
    SourceView.prototype.constructor = SourceView;

    SourceView.prototype.render = function () {
        this._editor = ace.edit(this._container);
        //Avoiding ace warning
        this._editor.$blockScrolling = Infinity;
        this._editor.setTheme("ace/theme/twilight");
        // this._editor.session.setMode("ace/mode/ballerina"); FIXME: enable ballerina mod for ace
        // var langTools = ace.require("ace/ext/language_tools");
        this._editor.setOptions({
            enableBasicAutoCompletion:true
        });
        this._editor.setBehavioursEnabled(true);
        var self = this;
        //bind auto complete to key press
        this._editor.commands.on("afterExec", function(e){
            if (e.command.name == "insertstring"&&/^[\w.]$/.test(e.args)) {
                self._editor.execCommand("startAutocomplete");
            }
        });
        this._editor.session.setValue(this._content);
    };

    /**
     * Set the content of text editor.
     * @param {String} content - content for the editor.
     *
     */
    SourceView.prototype.setContent = function(content){
        this._editor.session.setValue(content);
    };

    SourceView.prototype.show = function(){
        $(this._container).show();
    };

    SourceView.prototype.hide = function(){
        $(this._container).hide();
    };

    SourceView.prototype.isVisible = function(){
       return  $(this._container).is(':visible')
    };

    return SourceView;
});

