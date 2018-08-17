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
import EventChannel from 'event_channel';

/**
 * An undo/redo manager wrapping the functionality of monaco editor's native
 * implementation.
 *
 * @class UndoManager
 * @augments EventChannel
 */
class MonacoBasedUndoManager extends EventChannel {

    /**
     * @inheritdoc
     */
    constructor(sourceEditor) {
        super();
        this.sourceEditor = sourceEditor;
    }

    /**
     * @returns {boolean} true if has undoable operations
     */
    hasUndo() {
        const currentModel = this.sourceEditor.getCurrentModel();
        return currentModel && (currentModel._commandManager.past.length > 0
                || currentModel._commandManager.currentStackElement !== undefined);
    }

    /**
     * Undo most recent operation
     */
    undo() {
        this.sourceEditor.undo();
    }

    /**
     * @returns {boolean} true if has redoable operations
     */
    hasRedo() {
        const currentModel = this.sourceEditor.getCurrentModel();
        return currentModel && currentModel._commandManager.future.length > 0;
    }

    /**
     * Redo most recent undone operation
     */
    redo() {
        this.sourceEditor.redo();
    }
}

export default MonacoBasedUndoManager;
