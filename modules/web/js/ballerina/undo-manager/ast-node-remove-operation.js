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
 * Class to represent ast node remove operation
 * @class ASTNodeRemoveOperation
 * @augments ASTManipulationOperation
 * @param args
 * @constructor
 */
class ASTNodeRemoveOperation extends ASTManipulationOperation {
    constructor(args) {
        super(args);
        if (_.isNil(this.getTitle())) {
            this.setTitle(`Remove ${this._data.child.getType()}`);
        }
    }

    undo() {
        if (this.canUndo()) {
            this._originNode.addChild(this._data.child, this._data.index, true);
            this.getEditor().trigger('content-modified');
            this.getEditor().trigger('update-diagram');
        }
    }

    redo() {
        if (this.canRedo()) {
            this._data.child.remove({ ignoreTreeModifiedEvent: true });
            this.getEditor().trigger('content-modified');
            this.getEditor().trigger('update-diagram');
        }
    }
}

export default ASTNodeRemoveOperation;
