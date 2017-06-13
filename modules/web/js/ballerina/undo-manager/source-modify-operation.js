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

import UndoableOperation from './undoable-operation';

/**
 * Class to represent an undoable source modify operation
 * @class SourceModifyOperation
 * @augments UndoableOperation
 * @param args
 * @constructor
 */
class SourceModifyOperation extends UndoableOperation {
    constructor(args) {
        super(args);
    }

    undo() {
        if (this.canUndo()) {
            this.getEditor().getSourceView().undo();
        }
    }

    redo() {
        if (this.canRedo()) {
            this.getEditor().getSourceView().redo();
        }
    }

    canUndo() {
        return this.getEditor().isInSourceView();
    }

    canRedo() {
        return this.getEditor().isInSourceView();
    }
}

export default SourceModifyOperation;
