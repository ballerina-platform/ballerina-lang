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
import SwitchToSourceViewConfirmDialog from './../../dialog/switch-to-source-confirm-dialog';

/**
 * Class to represent an undoable operation in ballerina file editor
 * 
 * @class UndoableBalEditorOperation
 * @augments UndoableOperation
 */
class UndoableBalEditorOperation extends UndoableOperation {
    constructor(args) {
        super({
            oldContent: args.changeEvent.oldContent,
            newContent: args.changeEvent.newContent,
            file: args.file,
        });
        if (args.changeEvent.originEvt.type === 'tree-modified') {
            this.setTitle(args.changeEvent.originEvt.originEvt.title);
        } else if (args.changeEvent.originEvt.type === 'source-modified') {
            this.setTitle('Modify Source');
        }
    }

    undo() {
        
    }

    prepareUndo(next) {
        if (!this.getEditor().isInSourceView()) {
            SwitchToSourceViewConfirmDialog.askConfirmation('undo', (continueUndo) => {
                if (continueUndo) {
                    this.getEditor().activateSourceView();
                    next(true);
                } else {
                    next(false);
                }
            });
        } else {
            next(true);
        }
    }

    redo() {
    }

    prepareRedo(next) {
        if (!this.getEditor().isInSourceView()) {
            SwitchToSourceViewConfirmDialog.askConfirmation('redo', (continueRedo) => {
                if (continueRedo) {
                    this.getEditor().activateSourceView();
                    next(true);
                } else {
                    next(false);
                }
            });
        } else {
            next(true);
        }
    }
}

export default UndoableBalEditorOperation;
