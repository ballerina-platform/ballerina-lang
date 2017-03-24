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
define(['lodash', './undoable-operation'],
    function (_, UndoableOperation) {

        /**
         * Class to represent a custom undoable operation
         * @class CustomUndoableOperation
         * @augments UndoableOperation
         * @param args
         * @constructor
         */
        var CustomUndoableOperation = function(args){
            UndoableOperation.call(this, args);
            this._callBackContext = _.get(args, 'context');
            this._undoCallBack = _.get(args, 'undo');
            this._redoCallBack = _.get(args, 'redo');
        };

        CustomUndoableOperation.prototype = Object.create(UndoableOperation.prototype);
        CustomUndoableOperation.prototype.constructor = CustomUndoableOperation;

        CustomUndoableOperation.prototype.undo = function(){
           if(this.canUndo()){
               this._undoCallBack.call(this._callBackContext);
           }
        };
        CustomUndoableOperation.prototype.redo = function(){
            if(this.canRedo()) {
                this._redoCallBack.call(this._callBackContext);
            }
        };

        return CustomUndoableOperation;
    });

