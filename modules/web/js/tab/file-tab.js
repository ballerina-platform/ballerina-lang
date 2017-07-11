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
import Tab from './tab';
import File from '../workspace/file';
import BallerinaFileEditor from '../ballerina/views/ballerina-file-editor.jsx';
import UndoManager from '../ballerina/undo-manager/undo-manager';
import UndoableBalEditorOperation from '../ballerina/undo-manager/undoable-bal-editor-operation';

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

        this.file.on('dirty-state-change', () => {
            this.app.workspaceManager.updateSaveMenuItem();
            this.updateHeader();
        });

        this.file.on('content-modified', (evt) => {
            this._handleUndoRedoStackOnUpdate(evt);
            this.app.workspaceManager.updateMenuItems();
        });
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
        // eslint-disable-next-line new-cap
        const undoableOp = new UndoableBalEditorOperation({
            file: this.file,
            changeEvent,
        });
        this._undoManager.push(undoableOp);
    }
}

export default FileTab;
