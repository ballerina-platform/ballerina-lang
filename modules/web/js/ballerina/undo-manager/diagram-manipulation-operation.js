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
import log from 'log';
import UndoableOperation from './undoable-operation';

/**
 * Class to represent undoable operations done from design view
 * @class DiagramManipulationOperation
 * @augments UndoableOperation
 * @param args
 * @constructor
 */
class DiagramManipulationOperation extends UndoableOperation {
    constructor(args) {
        super(args);
    }

    undo() {
        let editor = this.getEditor(),
            file = editor.getFile(),
            sourceView = editor.getSourceView();
        sourceView.undo();
        file.setContent(sourceView.getContent())
            .setDirty(true)
            .save();
        let response = editor.parserBackend.parse({ content: sourceView.getContent() });
        if (response.error && !_.isEmpty(response.message)) {
            log.error(`Cannot render updated diagram due to syntax errors : ${  response.message}`);
        } else {
            let root = editor.deserializer.getASTModel(response);
            editor.setModel(root);
            editor.update();
        }
    }

    redo() {
        let editor = this.getEditor(),
            file = editor.getFile(),
            sourceView = editor.getSourceView();
        sourceView.redo();
        file.setContent(sourceView.getContent())
            .setDirty(true)
            .save();
        let response = editor.parserBackend.parse({ content: sourceView.getContent() });
        if (response.error && !_.isEmpty(response.message)) {
            log.error(`Cannot render updated diagram due to syntax errors : ${  response.message}`);
        } else {
            let root = editor.deserializer.getASTModel(response);
            editor.setModel(root);
            editor.update();
        }
    }
}

export default DiagramManipulationOperation;
