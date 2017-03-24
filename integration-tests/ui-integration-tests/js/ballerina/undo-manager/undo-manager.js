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
define(['log', 'lodash', 'event_channel', './undoable-operation-factory'],
    function (log, _, EventChannel, UndoableOperationFactory) {

        /**
         * Class to represent undo/redo manager
         * @class UndoManager
         * @augments EventChannel
         * @param args
         * @constructor
         */
        var UndoManager = function(args){
            this._limit = _.get(args, 'limit', 20);
            this._undoStack = [];
            this._redoStack = [];
        };

        UndoManager.prototype = Object.create(EventChannel.prototype);
        UndoManager.prototype.constructor = UndoManager;

        UndoManager.prototype.reset = function(){
            this._undoStack = [];
            this._redoStack = [];
            this.trigger('reset');
        };

        UndoManager.prototype._push = function(undoableOperation){
            if(this._undoStack.length === this._limit){
                // remove oldest undoable operation
                this._undoStack.splice(0, 1);
            }
            this._undoStack.push(undoableOperation);
            this.trigger('undoable-operation-added', undoableOperation);
        };

        UndoManager.prototype.hasUndo = function(){
            return !_.isEmpty(this._undoStack);
        };

        UndoManager.prototype.undoStackTop = function(){
            return _.last(this._undoStack);
        };

        UndoManager.prototype.redoStackTop = function(){
            return _.last(this._redoStack);
        };

        UndoManager.prototype.undo = function(){
            var taskToUndo = this._undoStack.pop();
            taskToUndo.undo();
            this._redoStack.push(taskToUndo);
        };

        UndoManager.prototype.hasRedo = function(){
            return !_.isEmpty(this._redoStack);
        };

        UndoManager.prototype.redo = function(){
            var taskToRedo = this._redoStack.pop();
            taskToRedo.redo();
            this._undoStack.push(taskToRedo);
        };

        UndoManager.prototype.getOperationFactory = function(){
            return UndoableOperationFactory;
        };

        UndoManager.prototype.onUndoableOperation = function(event){
            var undoableOperation = UndoableOperationFactory.getOperation(event);
            this._push(undoableOperation);
        };

        return UndoManager;
    });

