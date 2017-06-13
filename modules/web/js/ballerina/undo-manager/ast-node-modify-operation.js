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
import ASTManipulationOperation from './ast-manipulation-operation';

/**
 * Class to represent ast node modify operation
 * @class ASTNodeModifyOperation
 * @augments ASTManipulationOperation
 * @param args
 * @constructor
 */
class ASTNodeModifyOperation extends ASTManipulationOperation {
    constructor(args) {
        super(args);
        if (_.isNil(this.getTitle())) {
            this.setTitle(`Modify ${this._data.child.getType()}`);
        }
        this._clonedOriginNode = _.cloneDeep(this._originNode);
        this._parentOfOriginNode = this._originNode.getParent();
        this._originNodeIndex = this._parentOfOriginNode.getIndexOfChild(this._originNode);
    }

    redo() {
        if (this.canRedo()) {
            this._parentOfOriginNode.addChild(this._clonedOriginNode, this._originNodeIndex, true);
            this.getEditor().trigger('content-modified');
            this.getEditor().trigger('update-diagram');
        }
    }

    undo() {
        if (this.canUndo()) {
            this._originNode.remove({ ignoreTreeModifiedEvent: true });
            this.getEditor().trigger('content-modified');
            this.getEditor().trigger('update-diagram');
        }
    }
}

export default ASTNodeModifyOperation;
