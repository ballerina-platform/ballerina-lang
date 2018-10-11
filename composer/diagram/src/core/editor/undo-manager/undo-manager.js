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
import _ from 'lodash';
import EventChannel from 'event_channel';

/**
 * Class to represent undo/redo manager
 * @class UndoManager
 * @augments EventChannel
 */
class UndoManager extends EventChannel {

    /**
     * @inheritdoc
     */
    constructor(args) {
        super();
        this._limit = _.get(args, 'limit', 50);
        this._undoStack = [];
        this._redoStack = [];
    }

    /**
     * Reset undo-redo stack
     */
    reset() {
        this._undoStack = [];
        this._redoStack = [];
        this.trigger('updated');
    }

    /**
     * Push a new undoable op to stack
     * @param {UndoableOperation} undoableOperation Op Instance
     */
    push(undoableOperation) {
        if (this._undoStack.length === this._limit) {
            // remove oldest undoable operation
            this._undoStack.splice(0, 1);
        }
        this._undoStack.push(undoableOperation);
        this.trigger('updated');
    }

    /**
     * @returns {boolean} true if has undoable operations
     */
    hasUndo() {
        return !_.isEmpty(this._undoStack);
    }

    /**
     * @returns {UndoableOperation} peek of undo stack
     */
    undoStackTop() {
        return _.last(this._undoStack);
    }

    /**
     * @returns {UndoableOperation} peek of redo stack
     */
    redoStackTop() {
        return _.last(this._redoStack);
    }

    /**
     * Undo most recent operation
     */
    undo() {
        const taskToUndo = this._undoStack.pop();
        taskToUndo.prepareUndo((canContinue) => {
            if (canContinue) {
                taskToUndo.undo();
                this._redoStack.push(taskToUndo);
                this.trigger('updated');
            }
        });
    }

    /**
     * @returns {boolean} true if has redoable operations
     */
    hasRedo() {
        return !_.isEmpty(this._redoStack);
    }

    /**
     * Redo most recent undone operation
     */
    redo() {
        const taskToRedo = this._redoStack.pop();
        taskToRedo.prepareRedo((canContinue) => {
            if (canContinue) {   
                taskToRedo.redo();
                this._undoStack.push(taskToRedo);
                this.trigger('updated');
            }
        });
    }
}

export default UndoManager;
