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
import EventChannel from 'event_channel';

/**
 * Class to represent undoable operation
 * @class UndoableOperation
 * @augments EventChannel
 * @param args
 * @constructor
 */
class UndoableOperation extends EventChannel {
    constructor(args) {
        super();
        this.setTitle(_.get(args, 'title', undefined));
        this.setEditor(_.get(args, 'editor', undefined));
    }

    prepareUndo(next) {
        next(true);
    }

    prepareRedo(next) {
        next(true);
    }

    getTitle() {
        return this._title;
    }

    setTitle(title) {
        this._title = title;
    }

    getEditor() {
        return this._editor;
    }

    setEditor(editor) {
        this._editor = editor;
    }

    undo() {}
    redo() {}
}

export default UndoableOperation;
