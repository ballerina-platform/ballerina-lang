/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import UndoableOperation from 'core/editor/undo-manager/undoable-operation';
import { EVENTS } from 'core/editor/constants';

/**
 * Class to represent an undoable operation in text file editor
 *
 * @class TextEditorUndoableOperation
 * @augments UndoableOperation
 */
class TextEditorUndoableOperation extends UndoableOperation {

    /**
     * @inheritdoc
     */
    constructor(args) {
        super({
            oldContent: args.changeEvent.oldContent,
            newContent: args.changeEvent.newContent,
            file: args.file,
        });
        this.originEvt = args.changeEvent;
        this.monacoEditorInstance = args.monacoEditorInstance;
        this.setTitle(this.originEvt.title);
    }

    undo() {
        this.monacoEditorInstance.trigger(this.getTitle(), 'undo');
        this.getFile().setContent(this.monacoEditorInstance.getValue(), {
            type: EVENTS.UNDO_EVENT,
            originEvt: this.originEvt,
        });
    }

    prepareUndo(next) {
        next(true);
    }

    redo() {
        this.monacoEditorInstance.trigger(this.getTitle(), 'redo');
        this.getFile().setContent(this.monacoEditorInstance.getValue(), {
            type: EVENTS.REDO_EVENT,
            originEvt: this.originEvt,
        });
    }

    prepareRedo(next) {
        next(true);
    }
}

export default TextEditorUndoableOperation;
