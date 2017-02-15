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
         * Class to represent ast node modify operation
         * @class ASTNodeModifyOperation
         * @augments ASTManipulationOperation
         * @param args
         * @constructor
         */
        var ASTNodeModifyOperation = function(args){
            ASTManipulationOperation.call(this, args);
            if(_.isNil(this.getTitle())){
                this.setTitle("Modify " + this._data.child.getType())
            }
            this._clonedOriginNode = _.cloneDeep(this._originNode);
            this._parentOfOriginNode = this._originNode.getParent();
            this._originNodeIndex = this._parentOfOriginNode.getIndexOfChild(this._originNode);
        };

        ASTNodeModifyOperation.prototype = Object.create(ASTManipulationOperation.prototype);
        ASTNodeModifyOperation.prototype.constructor = ASTNodeModifyOperation;

        ASTNodeModifyOperation.prototype.redo = function(){
            if(this.canRedo()) {
                this._parentOfOriginNode.addChild(this._clonedOriginNode, this._originNodeIndex, true);
            }
        };

        ASTNodeModifyOperation.prototype.undo = function(){
            if(this.canUndo()) {
                this._originNode.remove({ignoreTreeModifiedEvent: true});
            }
        };

        return ASTNodeModifyOperation;
    });

