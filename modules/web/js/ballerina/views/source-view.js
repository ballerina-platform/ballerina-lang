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
import 'brace';
import 'brace/ext/language_tools';
import 'brace/ext/searchbox';
import SourceGenVisitor from './../visitors/source-gen/ballerina-ast-root-visitor';
import EnableDefaultWSVisitor from './../visitors/source-gen/enable-default-ws-visitor';
import SourceViewCompleterFactory from './../../ballerina/utils/source-view-completer-factory';

let ace = global.ace;
let Range = ace.acequire('ace/range').Range;
let AceUndoManager = ace.acequire('ace/undomanager').UndoManager;

// override default undo manager of ace editor
class NotifyingUndoManager extends AceUndoManager {
    constructor(sourceView) {
        super();
        this.sourceView = sourceView;
    }
    execute(args) {
        super.execute(args);
        if (!this.sourceView.inSilentMode) {
            let changeEvent = {
                type: 'source-modified',
                title: 'Modify source'
            };
            this.sourceView.trigger('modified', changeEvent);
        }
        this.sourceView.inSilentMode = false;
    }
}

 // require ballerina mode
 const langTools = ace.acequire('ace/ext/language_tools');

// require possible themes
function requireAll(requireContext) {
    return requireContext.keys().map(requireContext);
}
requireAll(require.context('ace', false, /theme-/));

// require ballerina mode
let mode = ace.acequire('ace/mode/ballerina');

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
        if (!_.has(args, 'container')) {
            log.error('container is not specified for rendering source view.');
        }
        this._container = _.get(args, 'container');
        this._content = _.get(args, 'content');
        this._fileEditor = _.get(args, 'fileEditor');
        this._debugger = _.get(args, 'debugger', undefined);
        this._markers = {};
        this._gutter = 25;
        this._storage = _.get(args, 'storage');
        this._langserverController = _.get(args, 'langserverClientController');
        this.inSilentMode = false;
    }

    render() {
        let self = this;
        this._editor = ace.edit(this._container);
        let mode = ace.acequire(_.get(this._options, 'mode')).Mode;
        this._editor.getSession().setMode(_.get(this._options, 'mode'));
        this._editor.getSession().setUndoManager(new NotifyingUndoManager(this));
        // Avoiding ace warning
        this._editor.$blockScrolling = Infinity;
        let editorThemeName = (this._storage.get('pref:sourceViewTheme') !== null) ? this._storage.get('pref:sourceViewTheme')
            : _.get(this._options, 'theme');
        let editorFontSize = (this._storage.get('pref:sourceViewFontSize') !== null) ? this._storage.get('pref:sourceViewFontSize')
            : _.get(this._options, 'font_size');

        let editorTheme = ace.acequire(editorThemeName);

        const sourceViewCompleter = SourceViewCompleterFactory.getSourceViewCompleter(this._langserverController);
        // TODO: Enable the completer after features are completed
        if (sourceViewCompleter) {
            langTools.setCompleters(sourceViewCompleter);
        }

        this._editor.setTheme(editorTheme);
        this._editor.setFontSize(editorFontSize);
        this._editor.setOptions({
            enableBasicAutocompletion: true,
        });
        this._editor.setBehavioursEnabled(true);
        // bind auto complete to key press
        this._editor.commands.on('afterExec', (e) => {
            if (e.command.name === 'insertstring' && /^[\w.]$/.test(e.args)) {
                self._editor.execCommand('startAutocomplete');
            }
        });

        this._editor.getSession().setValue(this._content);
        this._editor.renderer.setScrollMargin(_.get(this._options, 'scroll_margin'), _.get(this._options, 'scroll_margin'));
        this._editor.on('change', (event) => {
            // IMPORTANT: now listening to changes via overridden undo manager
        });

        // register actions
        if (this._debugger !== undefined && this._debugger.isEnabled()) {
            this._editor.on('guttermousedown', _.bind(this.toggleDebugPoints, this));
        }
    }

    /**
     * Set the content of text editor.
     * IMPORTANT: Updating editor content using this api
     * won't enable history in undo-manager of the editor.
     * Use @link(SourceView#replaceContent) if you wish to enable history.
     * 
     * @deprecated
     * @param {String} content - content for the editor.
    */
    setContent(content) {
        this._editor.session.setValue(content);
        this.markClean();
    }

    /**
     * Replace content of the editor while maintaining history
     * 
     * @param {*} newContent content to insert
     */
    replaceContent (newContent, doSilently) {
        if (doSilently) {  
            this.inSilentMode = true;
        }
        const session = this._editor.getSession();
        const contentRange = new Range(0, 0, session.getLength(), 
                        session.getRowLength(session.getLength()));
        session.replace(contentRange, newContent);
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
        let id = command.id;
        let hasShortcut = _.has(command, 'shortcuts');
        let self = this;
        if (hasShortcut) {
            let macShortcut = _.replace(command.shortcuts.mac.key, '+', '-');
            let winShortcut = _.replace(command.shortcuts.other.key, '+', '-');
            this.getEditor().commands.addCommand({
                name: id,
                bindKey: { win: winShortcut, mac: macShortcut },
                exec() {
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
        return $(this._container).is(':visible');
    }

    format() {
        const  validateRes = this._fileEditor.validatorBackend.parse({ 
            content: this._editor.getSession().getValue()
        });
        if ((validateRes.errors && !_.isEmpty(validateRes.errors))
            || (validateRes.error && !_.isEmpty(validateRes.message))) {
            // if there are syntax errors or issues with validate service
            // prevent formatting as AST building is not possible
            alerts.error(`Cannot format due to syntax errors`);
            return;
        }
        const  parserRes = this._fileEditor.parserBackend.parse(
            {
                name: this._fileEditor.getFile().getName(),
                path: this._fileEditor.getFile().getPath(),
                content: this._editor.getSession().getValue(),
                package: 'Current Package',
            },
        );
        if (parserRes.error && !_.isEmpty(parserRes.message)) {
            alerts.error(`Cannot format due to syntax errors : ${  parserRes.message}`);
            return;
        }
        const ast = this._fileEditor.deserializer.getASTModel(parserRes);
        const enableDefaultWSVisitor = new EnableDefaultWSVisitor();
        ast.accept(enableDefaultWSVisitor);
        const sourceGenVisitor = new SourceGenVisitor();
        ast.accept(sourceGenVisitor);
        const formattedContent = sourceGenVisitor.getGeneratedSource();
        this.replaceContent(formattedContent, false);
    }

    // dbeugger related functions.

    toggleDebugPoints(e) {
        let target = e.domEvent.target;
        if (target.className.indexOf('ace_gutter-cell') === -1)
            {return;}
        if (!this._editor.isFocused())
            {return;}
        if (e.clientX > this._gutter + target.getBoundingClientRect().left)
            {return;}


        let breakpoints = e.editor.session.getBreakpoints(row, 0);
        var row = e.getDocumentPosition().row;
        if (_.isUndefined(breakpoints[row])) {
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
        if (this.debugPointMarker !== undefined) {
            this._editor.getSession().removeMarker(this.debugPointMarker);
        }
    }

    isClean() {
        return this._editor.getSession().getUndoManager().isClean();
    }

    undo() {
        this._editor.getSession().getUndoManager().undo();
    }

    redo() {
        this._editor.getSession().getUndoManager().redo();
    }

    markClean() {
        this._editor.getSession().getUndoManager().markClean();
    }

    jumpToLine({expression=''}) {
        const range = this._editor.find(expression.trim() , {
            regExp:false,
        });
        if(!range) {
            
        }
    }

    setBreakpoints(breakpoints = []) {
        // ace editor breakpoints counts from 0;
        let sourceViewBreakPoints = breakpoints.map(breakpoint => breakpoint - 1);
        this._editor.getSession().setBreakpoints(sourceViewBreakPoints);
    }

    getBreakpoints() {
        const sourceViewBreakPointRows = this._editor.getSession().getBreakpoints() || [];
        const sourceViewBreakPoints = sourceViewBreakPointRows.map((value, row) => row + 1);
        return sourceViewBreakPoints;
    }

    resize() {
        this._editor.resize();
    }
}

export default SourceView;
