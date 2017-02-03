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
define(['require', 'log', 'lodash', 'jquery', 'event_channel', 'ace/ace', '../utils/ace-mode', 'beautify', 'ace/ext/language_tools','ace/range',
    ],
    function(require , log, _, $, EventChannel, ace, BallerinaMode, Beautify, language_tools, Range) {

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
        this._debugger = _.get(args, 'debugger', undefined);
        this._markers = {};
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

        this._editor.getSession().setValue(this._content);
        this._editor.getSession().setMode(_.get(this._options, 'mode'));
        this._editor.renderer.setScrollMargin(_.get(this._options, 'scroll_margin'), _.get(this._options, 'scroll_margin'));
        this._editor.setOptions({
            fontSize: _.get(this._options, 'font_size')
        });

        //register actions
        if(this._debugger != undefined && this._debugger.isEnabled()){
            this._editor.on("guttermousedown", _.bind(this.toggleDebugPoints, this));
            this._debugger.on("resume-execution", _.bind(this.clearExistingDebugHit, this));
        }
    };


    /**
     * Set the content of text editor.
     * @param {String} content - content for the editor.
     *
     */
    SourceView.prototype.setContent = function(content){
        this._editor.session.setValue(content);
        var fomatter = require('ballerina').utils.AceFormatter;
        fomatter.beautify(this._editor.getSession());
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
    
    //dbeugger related functions. 

    SourceView.prototype.toggleDebugPoints = function(e){
        var target = e.domEvent.target; 
        if (target.className.indexOf("ace_gutter-cell") == -1)
            return; 
        if (!this._editor.isFocused()) 
            return; 
        if (e.clientX > 25 + target.getBoundingClientRect().left) 
            return; 


        var breakpoints = e.editor.session.getBreakpoints(row, 0);
        var row = e.getDocumentPosition().row;        
        if(_.isUndefined(breakpoints[row])){
            this._markers[row] = this._editor.getSession().addMarker(new Range.Range(row, 0, row, 2000), "debug-point", "line", true);
            e.editor.session.setBreakpoint(row);
            this.trigger('add-breakpoint', row + 1);
        }
        else{
            this._editor.getSession().removeMarker(this._markers[row]);
            delete this._markers[row];
            this.trigger('remove-breakpoint', row + 1);
            e.editor.session.clearBreakpoint(row);
        }
        e.stop();
    };

    SourceView.prototype.debugHit = function(position){
        this.debugPointMarker = this._editor.getSession().addMarker(new Range.Range(position.line - 1, 0, position.line - 1, 2000), "debug-point-hit", "line", true);
    }

    SourceView.prototype.clearExistingDebugHit = function(position){
        if(this.debugPointMarker != undefined){
            this._editor.getSession().removeMarker(this.debugPointMarker);
        }
    }    

    SourceView.prototype.isClean = function(){
       return this._editor.getSession().getUndoManager().isClean();
    };

    SourceView.prototype.markClean = function(){
       this._editor.getSession().getUndoManager().markClean();
    };

    return SourceView;
});

