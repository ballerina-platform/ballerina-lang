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
import log from 'log';
import _ from 'lodash';
import $ from 'jquery';
import alerts from 'alerts';
import EventChannel from 'event_channel';
import SourceGenVisitor from './../visitors/source-gen/ballerina-ast-root-visitor';
import EnableDefaultWSVisitor from './../visitors/source-gen/enable-default-ws-visitor';
import 'brace';
import 'brace/ext/language_tools';
import 'brace/ext/searchbox';
var ace = global.ace;
var Range = ace.acequire('ace/range').Range;

// require possible themes
function requireAll(requireContext) {
    return requireContext.keys().map(requireContext);
}
requireAll(require.context('ace', false, /theme-/));

// require ballerina mode
var mode = ace.acequire('ace/mode/ballerina');

/**
 * @class SourceView
 * @augments EventChannel
 * @constructor
 * @class SourceView  Wraps the Ace editor for source view
 * @param {Object} args - Rendering args for the view
 * @param {String} args.container - selector for div element to render ace editor
 * @param {String} [args.content] - initial content for the editor
 */
class SourceView extends EventChannel {
    constructor(args) {
        super();
        this._options = args;
        if(!_.has(args, 'container')){
            log.error('container is not specified for rendering source view.');
        }
        this._container = _.get(args, 'container');
        this._content = _.get(args, 'content');
        this._fileEditor = _.get(args, 'fileEditor');
        this._debugger = _.get(args, 'debugger', undefined);
        this._markers = {};
        this._gutter = 25;
        this._storage = _.get(args, 'storage');
    }

    render() {
        var self = this;
        this._editor = ace.edit(this._container);
        var mode = ace.acequire(_.get(this._options, 'mode')).Mode;
        this._editor.getSession().setMode(_.get(this._options, 'mode'));
        //Avoiding ace warning
        this._editor.$blockScrolling = Infinity;
        var editorThemeName = (this._storage.get('pref:sourceViewTheme') !== null) ? this._storage.get('pref:sourceViewTheme')
            : _.get(this._options, 'theme');
        var editorFontSize = (this._storage.get('pref:sourceViewFontSize') !== null) ? this._storage.get('pref:sourceViewFontSize')
            : _.get(this._options, 'font_size');

        var editorTheme = ace.acequire(editorThemeName);

        this._editor.setTheme(editorTheme);
        this._editor.setFontSize(editorFontSize);
        this._editor.setOptions({
            enableBasicAutocompletion:true
        });
        this._editor.setBehavioursEnabled(true);
        //bind auto complete to key press
        this._editor.commands.on('afterExec', function(e){
            if (e.command.name === 'insertstring'&&/^[\w.]$/.test(e.args)) {
                self._editor.execCommand('startAutocomplete');
            }
        });

        this._editor.getSession().setValue(this._content);
        this._editor.renderer.setScrollMargin(_.get(this._options, 'scroll_margin'), _.get(this._options, 'scroll_margin'));
        this._editor.on('change', function(event) {
            if(!self._inSilentMode){
                var changeEvent = {
                    type: 'source-modified',
                    title: 'Modify source',
                    data: {
                        type: event.action,
                        lines: event.lines
                    }
                };
                self.trigger('modified', changeEvent);
            }
        });

        //register actions
        if(this._debugger !== undefined && this._debugger.isEnabled()){
            this._editor.on('guttermousedown', _.bind(this.toggleDebugPoints, this));
        }
    }

    /**
     * Set the content of text editor.
     * @param {String} content - content for the editor.
     *
     */
    setContent(content) {
        // avoid triggering change event on format
        this._inSilentMode = true;
        this._editor.session.setValue(content);
        this._inSilentMode = false;
        this.markClean();
    }

    getContent() {
        return this._editor.session.getValue();
    }

    getEditor() {
        return this._editor;
    }

    /**
     * Binds a shortcut to ace editor so that it will trigger the command on source view upon key press.
     * All the commands registered app's command manager will be bound to source view upon render.
     *
     * @param command {Object}
     * @param command.id {String} Id of the command to dispatch
     * @param command.shortcuts {Object}
     * @param command.shortcuts.mac {Object}
     * @param command.shortcuts.mac.key {String} key combination for mac platform eg. 'Command+N'
     * @param command.shortcuts.other {Object}
     * @param command.shortcuts.other.key {String} key combination for other platforms eg. 'Ctrl+N'
     */
    bindCommand(command) {
        var id = command.id,
            hasShortcut = _.has(command, 'shortcuts'),
            self = this;
        if(hasShortcut){
            var macShortcut = _.replace(command.shortcuts.mac.key, '+', '-'),
                winShortcut = _.replace(command.shortcuts.other.key, '+', '-');
            this.getEditor().commands.addCommand({
                name: id,
                bindKey: {win: winShortcut, mac: macShortcut},
                exec: function() {
                    self.trigger('dispatch-command', id);
                }
            });
        }
    }

    show() {
        $(this._container).show();
    }

    hide() {
        $(this._container).hide();
    }

    isVisible() {
        return  $(this._container).is(':visible');
    }

    format(doSilently) {
        let  parserRes = this._fileEditor.parserBackend.parse(this._editor.getSession().getValue());
        if (parserRes.error && !_.isEmpty(parserRes.message)) {
            alerts.error('Cannot format due to syntax errors : ' + parserRes.message);
            return;
        }
        let ast = this._fileEditor.deserializer.getASTModel(parserRes);
        let enableDefaultWSVisitor = new EnableDefaultWSVisitor();
        ast.accept(enableDefaultWSVisitor);
        let sourceGenVisitor = new SourceGenVisitor();
        ast.accept(sourceGenVisitor);
        let formattedContent =  sourceGenVisitor.getGeneratedSource();
        let session = this._editor.getSession();
        let contentRange = new Range(0, 0, session.getLength(), session.getRowLength(session.getLength()));
        session.replace(contentRange, formattedContent);
    }

    //dbeugger related functions.

    toggleDebugPoints(e) {
        var target = e.domEvent.target;
        if (target.className.indexOf('ace_gutter-cell') === -1)
            return;
        if (!this._editor.isFocused())
            return;
        if (e.clientX > this._gutter + target.getBoundingClientRect().left)
            return;


        var breakpoints = e.editor.session.getBreakpoints(row, 0);
        var row = e.getDocumentPosition().row;
        if(_.isUndefined(breakpoints[row])){
            e.editor.session.setBreakpoint(row);
        } else {
            this._editor.getSession().removeMarker(this._markers[row]);
            e.editor.session.clearBreakpoint(row);
        }
        e.stop();
        this.trigger('breakpoints-updated');
    }

    debugHit(position) {
        this.debugPointMarker = this._editor.getSession().addMarker(new Range(position.lineNumber - 1, 0, position.lineNumber - 1, 2000), 'debug-point-hit', 'line', true);
    }

    clearExistingDebugHit() {
        if(this.debugPointMarker !== undefined){
            this._editor.getSession().removeMarker(this.debugPointMarker);
        }
    }

    isClean() {
        return this._editor.getSession().getUndoManager().isClean();
    }

    undo() {
        return this._editor.getSession().getUndoManager().undo();
    }

    redo() {
        return this._editor.getSession().getUndoManager().redo();
    }

    markClean() {
        this._editor.getSession().getUndoManager().markClean();
    }

    jumpToLine({expression=''}) {
        var range = this._editor.find(expression.trim() , {
            regExp:false,
        });
        if(!range) {
            return;
        }
    }

    setBreakpoints(breakpoints = []) {
        // ace editor breakpoints counts from 0;
        var sourceViewBreakPoints = breakpoints.map( breakpoint => {
            return breakpoint - 1;
        });
        this._editor.getSession().setBreakpoints(sourceViewBreakPoints);
    }

    getBreakpoints() {
        const sourceViewBreakPointRows = this._editor.getSession().getBreakpoints() || [];
        const sourceViewBreakPoints = sourceViewBreakPointRows.map( (value, row) => {
            return row + 1;
        });
        return sourceViewBreakPoints;
    }
}

export default SourceView;
