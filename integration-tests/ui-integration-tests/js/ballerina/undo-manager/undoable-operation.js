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
define(['lodash', 'event_channel'],
    function (_, EventChannel) {

        /**
         * Class to represent undoable operation
         * @class UndoableOperation
         * @augments EventChannel
         * @param args
         * @constructor
         */
        var UndoableOperation = function(args){
            this.setTitle(_.get(args, 'title', undefined));
            this.setEditor(_.get(args, 'editor', undefined));
            this.setSkipInSourceView(_.get(args, 'skipInSourceView', false));
        };

        UndoableOperation.prototype = Object.create(EventChannel.prototype);
        UndoableOperation.prototype.constructor = UndoableOperation;

        UndoableOperation.prototype.canUndo = function(){
            return !(this.getEditor().isInSourceView() && this.skipInSourceView());
        };

        UndoableOperation.prototype.canRedo = function(){
            return !(this.getEditor().isInSourceView() && this.skipInSourceView());
        };

        UndoableOperation.prototype.getTitle = function(){
            return this._title;
        };

        UndoableOperation.prototype.setTitle = function(title){
            this._title = title;
        };

        UndoableOperation.prototype.getEditor = function(){
            return this._editor;
        };

        UndoableOperation.prototype.setEditor = function(editor){
            this._editor = editor;
        };

        UndoableOperation.prototype.skipInSourceView = function(){
            return this._skipInSourceView;
        };

        UndoableOperation.prototype.setSkipInSourceView = function(skipInSourceView){
            this._skipInSourceView = skipInSourceView;
        };

        UndoableOperation.prototype.undo = function(){};
        UndoableOperation.prototype.redo = function(){};

        return UndoableOperation;
    });

