/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import _ from 'lodash';
import $ from 'jquery';
import 'brace';
import 'brace/ext/language_tools';
import 'brace/ext/searchbox';
import '../ballerina/utils/ace-mode';
import { completerFactory } from './completer-factory.js';
const ace = global.ace;
const Range = ace.acequire('ace/range');


// require possible themes
function requireAll(requireContext) {
    return requireContext.keys().map(requireContext);
}
requireAll(require.context('ace', false, /theme-/));

// require ballerina mode
const mode = ace.acequire('ace/mode/ballerina');
const langTools = ace.acequire('ace/ext/language_tools');

class ExpressionEditor {

    constructor(bBox, callback, props, packageScope) {
        this.props = props;
        const expression = _.isNil(props.getterMethod.call(props.model)) ? '' : props.getterMethod.call(props.model);
        // workaround to handle http://stackoverflow.com/questions/21926083/failed-to-execute-removechild-on-node
        this.removed = false;

        this.expressionEditor = $("<div class='expression_editor'>");
        this.expressionEditor.width(bBox.w + 2);
        this.expressionEditor.height(bBox.h + 2);
        this.expressionEditor.offset({ top: bBox.y - 1, left: bBox.x - 1 });
        this.expressionEditor.css('border', '2px solid #333333');
        this.expressionEditor.css('padding-top', '6px');
        this.expressionEditor.css('background', 'white');
        this.expressionEditor.css('position', 'absolute');
        this.expressionEditor.css('min-width', bBox.w + 2);

        const editorContainer = $("<div class='expression_editor_container'>").appendTo(this.expressionEditor);
        $(editorContainer).css('height', '22px');
        $(editorContainer).text(expression);
        this._editor = ace.edit(editorContainer[0]);

        const mode = ace.acequire('ace/mode/ballerina').Mode;
        this._editor.getSession().setMode('ace/mode/ballerina');
        // Avoiding ace warning
        this._editor.$blockScrolling = Infinity;

        const editorTheme = ace.acequire('ace/theme/chrome');
        this._editor.setTheme(editorTheme);

        // set OS specific font size to prevent Mac fonts getting oversized.
        if (this.isRunningOnMacOS()) {
            this._editor.setFontSize('10pt');
        } else {
            this._editor.setFontSize('12pt');
        }

        const completers = completerFactory.getCompleters(props.key, packageScope);
        if (completers) {
            langTools.setCompleters(completers);
        }

        this._editor.setOptions({
            enableBasicAutocompletion: true,
            highlightActiveLine: false,
            showGutter: false,
        });

        this._editor.setBehavioursEnabled(true);
        this._editor.focus();

        // we need to place the cursor at the end of the text
        this._editor.gotoLine(1, expression.length);

        // resize the editor to the text width.
        this.expressionEditor.css('width', this.getNecessaryWidth(expression));
        this.expressionEditor.focus();
        this._editor.resize();

        // bind auto complete to key press
        this._editor.commands.on('afterExec', (event) => {
            if (event.command.name === 'insertstring' && /^[\w.]$/.test(event.args)) {
                this._editor.execCommand('startAutocomplete');
            }
        });

        // remove newlines in pasted text
        this._editor.on('paste', (e) => {
            e.text = e.text.replace(/[\r\n]+/g, ' ');
        });

        // when enter is pressed we will commit the change.
        this._editor.commands.bindKey('Enter|Shift-Enter', (e) => {
            const text = this._editor.getSession().getValue();
            // props.setterMethod.call(props.model, text);
            props.model.trigger('update-property-text', text, props.key);
            props.model.trigger('focus-out');
            this.distroy();
            if (_.isFunction(callback)) {
                callback(text);
            }
        });

        // When the user is typing text we will resize the editor.
        this._editor.on('change', (event) => {
            const text = this._editor.getSession().getValue();
            $(this.expressionEditor).css('width', this.getNecessaryWidth(text));
            this._editor.resize();
        });

        this._editor.on('blur', (event) => {
            const text = this._editor.getSession().getValue();
            props.setterMethod.call(props.model, text);
            props.model.trigger('update-property-text', text, props.key);
            props.model.trigger('focus-out');
            if (!this.removed) {
                this.distroy();
            }
            if (_.isFunction(callback)) {
                callback(text);
            }
        });

        // following snipet is to handle adding ";" at the end of statement.
        this.end_check = /^([^"]|"[^"]*")*?(;)/;
        this._editor.commands.addCommand({
            name: 'semicolon',
            exec: (e) => {
                // get the value and append ';' to get the end result of the key action
                const curser = this._editor.selection.getCursor().column;
                const text = this._editor.getSession().getValue();
                const textWithSemicolon = [text.slice(0, curser), ';', text.slice(curser)].join('');
                if (this.end_check.exec(textWithSemicolon)) {
                    props.model.trigger('update-property-text', text, props.key);
                    props.model.trigger('focus-out');
                    this.distroy();
                    if (_.isFunction(callback)) {
                        callback(text);
                    }
                } else {
                    this._editor.insert(';');
                }
            },
            bindKey: ';',
        });
    }

    render(container) {
        $(container).append(this.expressionEditor);
    }

    distroy() {
        if (!this.removed) {
            this.removed = true;
            // commit if there are any changes
            // let text = this._editor.getSession().getValue();
            // this.props.model.trigger('update-property-text', text , this.props.key);
            // distroy the editor
            // this._editor.distroy();
            this.expressionEditor.remove();
        }
    }

    getNecessaryWidth(text) {
        const width = text.length * 8 + 40;
        if (width < this.default_with) {
            return this.default_with;
        }
        return width;
    }


    static getOperatingSystem() {
        let operatingSystem = 'Unknown OS';
        if (navigator.appVersion.indexOf('Win') !== -1) {
            operatingSystem = 'Windows';
        } else if (navigator.appVersion.indexOf('Mac') !== -1) {
            operatingSystem = 'MacOS';
        } else if (navigator.appVersion.indexOf('X11') !== -1) {
            operatingSystem = 'UNIX';
        } else if (navigator.appVersion.indexOf('Linux') !== -1) {
            operatingSystem = 'Linux';
        }
        return operatingSystem;
    }

    isRunningOnMacOS() {
        return _.isEqual(ExpressionEditor.getOperatingSystem(), 'MacOS');
    }

}

export default ExpressionEditor;
