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
define(['lodash', './ast-manipulation-operation'],
    function (_, ASTManipulationOperation) {

        /**
         * Class to represent ast node add operation
         * @class ASTNodeAddOperation
         * @augments ASTManipulationOperation
         * @param args
         * @constructor
         */
        var ASTNodeAddOperation = function(args){
            ASTManipulationOperation.call(this, args);
            if(_.isNil(this.getTitle())){
                this.setTitle("Add " + this._data.child.getType())
            }
        };

        ASTNodeAddOperation.prototype = Object.create(ASTManipulationOperation.prototype);
        ASTNodeAddOperation.prototype.constructor = ASTNodeAddOperation;

        ASTNodeAddOperation.prototype.redo = function(){
            if(this.canUndo()) {
                this._originNode.addChild(this._data.child, this._data.index, true);
            }
        };
        ASTNodeAddOperation.prototype.undo = function(){
            if(this.canRedo()) {
                this._data.child.remove({ignoreTreeModifiedEvent: true});
            }
        };

        return ASTNodeAddOperation;
    });

