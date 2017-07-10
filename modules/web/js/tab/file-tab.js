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
import $ from 'jquery';
import _ from 'lodash';
import React from 'react';
import ReactDOM from 'react-dom';
import EventChannel from 'event_channel';
// FIXME: importing ast visitor here as it magically resolve subsequent module loading issue
// should be due to an unresolved cyclic dependency
import ASTVisitor from 'ballerina/visitors/ast-visitor';
import BallerinaFileEditor from 'ballerina/views/ballerina-file-editor.jsx';
import BallerinaASTFactory from 'ballerina/ast/ballerina-ast-factory';
import DiagramRenderContext from 'ballerina/diagram-render/diagram-render-context';
import BallerinaASTDeserializer from 'ballerina/ast/ballerina-ast-deserializer';
import Backend from 'ballerina/views/backend';
import Tab from './tab';
import File from '../workspace/file';
import DebugManager from '../debugger/debug-manager';
import BallerinaEnvFactory from '../ballerina/env/ballerina-env-factory';
import UndoManager from '../ballerina/undo-manager/undo-manager';
import SourceModifyOperation from '../ballerina/undo-manager/source-modify-operation';


/**
 * Represents a file tab used for editing ballerina.
 *
 * @class FileTab
 * @extends {Tab}
 */
class FileTab extends Tab {
    /**
     * Creates an instance of FileTab.
     * @param {Object} options The object as args for creating the file tab.
     * @param {File} options.file The file object.
     * @param {BallerinaASTRoot} options.astRoot The ballerina ast root.
     * @param {Object} options.parseResponse The JSON object which is received by the ballerina file service.
     * @memberof FileTab
     */
    constructor(options) {
        super(options);
        if (!_.has(options, 'file')) {
            this.file = new File({
                isTemp: true, isDirty: false,
            }, {
                storage: this.getParent().getBrowserStorage(),
            });
        } else {
            this.file = _.get(options, 'file');
        }
        this.app = options.application;
        this._undoManager = new UndoManager();
    }

    /**
     * Gets the title of the file tab. By default it is 'untitled'.
     *
     * @returns {string} The name of the file.
     * @memberof FileTab
     */
    getTitle() {
        return _.isNil(this.file) ? 'untitled' : this.file.getName();
    }

    /**
     * Gets the file object of this file tab.
     *
     * @returns {File} The file object.
     * @memberof FileTab
     */
    getFile() {
        return this.file;
    }

    /**
     * Renders the view of the file tab.
     *
     * @returns {void}
     * @memberof FileTab
     */
    render() {
        Tab.prototype.render.call(this);
        const fileEditorEventChannel = new EventChannel();
        const editorProps = {
            file: this.file,
            tabHeader: this.getHeader(),
            commandManager: this.app.commandManager,
        };

        // create Rect component for diagram
        const editorReactRoot = React.createElement(BallerinaFileEditor, editorProps, null);
        ReactDOM.render(editorReactRoot, this.getContentContainer());
    }
   
    initHandlers() {

        const fileEditorEventChannel = new EventChannel();

        // change tab header class to match look and feel of source view
        fileEditorEventChannel.on('source-view-activated swagger-view-activated', function () {
            this.getHeader().addClass('inverse');
            this.app.workspaceManager.updateMenuItems();
        }, this);
        fileEditorEventChannel.on('design-view-activated', function () {
            this.getHeader().removeClass('inverse');
            this.app.workspaceManager.updateMenuItems();
        }, this);

        fileEditorEventChannel.on('design-view-activated', () => {
            const breakpoints = fileEditor.getSourceView().getBreakpoints() || [];
            fileEditor.showDesignViewBreakpoints(breakpoints);
        }, this);

        fileEditorEventChannel.on('source-view-activated', () => {
            fileEditor.showSourceViewBreakPoints();
        });

        fileEditorEventChannel.on('add-breakpoint', function (row) {
            DebugManager.addBreakPoint(row, this._file.getName());
        }, this);

        fileEditorEventChannel.on('remove-breakpoint', function (row) {
            DebugManager.removeBreakPoint(row, this._file.getName());
        }, this);

        this.on('tab-removed', function () {
            const docUri = this._file.isPersisted() ? this._file.getPath() : (`/temp/${this._file.id}`);
            // Send document closed notification to the language server
            const documentOptions = {
                textDocument: {
                    documentUri: docUri,
                    documentId: this._file.id,
                },
            };
            //this.app.langseverClientController.documentDidCloseNotification(documentOptions);
            this.removeAllBreakpoints();
        });

        DebugManager.on('debug-hit', (message) => {
            const position = message.location;
            // Normalize file separator to /, ballerina debugger core can use file seperator \ or / depending on the OS
            // TODO: refactor this after API changes of BreakpointDTO  in core
            const fileName = position.fileName.replace(/\\/g, '/');
            if (fileName === fileEditor.getFileNameWithPackage()) {
                fileEditor.debugHit(DebugManager.createDebugPoint(position.lineNumber, position.fileName));
            }
        }, this);

        fileEditorEventChannel.on('content-modified', function (event) {
            const updatedContent = fileEditor.getContent();
            // if the modification happened from design view
            // updadte source view content
            if (!fileEditor.isInSourceView()) {
                fileEditor.getSourceView().replaceContent(updatedContent, true);
            }
            this._handleUndoRedoStackOnUpdate(event);
            this._file.setContent(updatedContent);
            this._file.setDirty(true);
            this._file.save();
            this.app.workspaceManager.updateMenuItems();
            this.trigger('tab-content-modified');
        }, this);

        this._file.on('dirty-state-change', function () {
            this.app.workspaceManager.updateSaveMenuItem();
            this.updateHeader();
        }, this);
    }

    /**
     * Updates the header/title of the file-tab
     *
     * @memberof FileTab
     */
    updateHeader() {
        if (this.file.isDirty()) {
            this.getHeader().setText(`* ${this.getTitle()}`);
        } else {
            this.getHeader().setText(this.getTitle());
        }
    }

    /**
     * Re-render current ballerina file.
     */
    reRender() {
        if (this._fileEditor) {
            this._fileEditor.reRender();
        }
    }

    /**
     * Gets the ballerina file editor
     *
     * @returns {BallerinaFileEditor} The file editor.
     * @memberof FileTab
     */
    getBallerinaFileEditor() {
        return this._fileEditor;
    }

    /**
     * Removes all breakpoints using {@link DebugManager}.
     *
     * @memberof FileTab
     */
    removeAllBreakpoints() {
        DebugManager.removeAllBreakpoints(this.file.getName());
    }

    /**
     * Gets all debug points using {@link DebugManager}.
     *
     * @returns {DebugPoint[]} Debug points.
     * @memberof FileTab
     */
    getBreakPoints() {
        return DebugManager.getDebugPoints(this.file.getName());
    }

    /**
     * Gets the undo manager.
     *
     * @returns {UndoManager} The undo manager.
     * @memberof FileTab
     */
    getUndoManager() {
        return this._undoManager;
    }

    /**
     * Handles the undoredo stack on update.
     *
     * @param {Object} changeEvent Event.
     * @param {string} changeEvent.title The event title.
     * @memberof FileTab
     */
    _handleUndoRedoStackOnUpdate(changeEvent) {
        let undoableOperationType = DiagramManipulationOperation;
        // user did the change while in source view
        if (this._fileEditor.isInSourceView()) {
            undoableOperationType = SourceModifyOperation;
        }

        // eslint-disable-next-line new-cap
        const undoableOp = new undoableOperationType({
            title: changeEvent.title,
            editor: this._fileEditor,
        });
        this._undoManager.push(undoableOp);
    }
}

export default FileTab;
