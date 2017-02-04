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
define(['require', 'log', 'lodash', 'jquery', 'event_channel', 'ace/ace', '../utils/ace-mode', 'beautify', 'ace/ext/language_tools'],
    function(require, log, _, $, EventChannel, ace, BallerinaMode, Beautify) {

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
            log.error('container is not specified for rendering source view.')
        }
        this._container = _.get(args, 'container');
        this._content = _.get(args, 'content');
    };

    SourceView.prototype = Object.create(EventChannel.prototype);
    SourceView.prototype.constructor = SourceView;

    SourceView.prototype.render = function () {
        var self = this;
        this._editor = ace.edit(this._container);
        //Avoiding ace warning
        this._editor.$blockScrolling = Infinity;
        this._editor.setTheme(_.get(this._options, 'theme'));
        this._editor.setOptions({
            enableBasicAutocompletion:true
        });
        this._editor.setBehavioursEnabled(true);
        var self = this;
        //bind auto complete to key press
        this._editor.commands.on("afterExec", function(e){
            if (e.command.name == "insertstring"&&/^[\w.]$/.test(e.args)) {
                self._editor.execCommand("startAutocomplete");
            }
        });

        this._editor.on("gutterdblclick", function(e){
            var row = e.getDocumentPosition().row;
            var breakpointsArray = e.editor.session.getBreakpoints();
            if(!(row in breakpointsArray)){
                e.editor.session.setBreakpoint(row);
                self.trigger('add-breakpoint', row, e.editor.session.getBreakpoints());
            }else{
                e.editor.session.clearBreakpoint(row);
                self.trigger('remove-breakpoint', row);
            }
            e.stop();
        });

        this._editor.getSession().setValue(this._content);
        this._editor.getSession().setMode(_.get(this._options, 'mode'));
        this._editor.renderer.setScrollMargin(_.get(this._options, 'scroll_margin'), _.get(this._options, 'scroll_margin'));
        this._editor.setOptions({
            fontSize: _.get(this._options, 'font_size')
        });
        this._editor.on("change", function() {
            if(!self._inSilentMode){
                self.trigger('modified');
            }
        });

    };

    /**
     * Set the content of text editor.
     * @param {String} content - content for the editor.
     *
     */
    SourceView.prototype.setContent = function(content){
        // avoid triggering change event on format
        this._inSilentMode = true;
        this._editor.session.setValue(content);
        var fomatter = require('ballerina').utils.AceFormatter;
        fomatter.beautify(this._editor.getSession());
        this._inSilentMode = false;
        this.markClean();
    };

    SourceView.prototype.getContent = function(){
        return this._editor.session.getValue();
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

    SourceView.prototype.isClean = function(){
       return this._editor.getSession().getUndoManager().isClean();
    };

    SourceView.prototype.markClean = function(){
       this._editor.getSession().getUndoManager().markClean();
    };

    return SourceView;
});

