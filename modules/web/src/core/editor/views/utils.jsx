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
 *
 */

import React from 'react';
import PropTypes from 'prop-types';
import { COMMANDS } from './../constants';

/**
 * Enable Undo/Redo support for editor component
 *
 * @param {EditorComponent} Editor Editor Component
 */
export function withUndoRedoSupport(Editor) {
    /**
     * Editor Wrapper HOC
     */
    class EditorWrapper extends React.Component {
         /**
         * @inheritdoc
         */
        constructor(...args) {
            super(...args);
            this.onUndoableOperation = this.onUndoableOperation.bind(this);
        }

        /**
         * @inheritdoc
         */
        componentDidMount() {
        }

        /**
         * @inheritdoc
         */
        componentWillUnmount() {
        }

        /**
         * On an undoable operation
         * @param {UndoableOperation} op instance
         */
        onUndoableOperation(op) {
            this.props.editorModel.undoManager.push(op);
        }

         /**
         * @inheritdoc
         */
        render() {
            return <Editor {...this.props} onUndoableOperation={this.onUndoableOperation} />;
        }
    }

    EditorWrapper.propTypes = {
        editorModel: PropTypes.objectOf(Object).isRequired,
        file: PropTypes.objectOf(Object).isRequired,
        isActive: PropTypes.func.isRequired,
        commandProxy: PropTypes.shape({
            on: PropTypes.func.isRequired,
            dispatch: PropTypes.func.isRequired,
            getCommands: PropTypes.func.isRequired,
        }).isRequired,
    };

    return EditorWrapper;
}
