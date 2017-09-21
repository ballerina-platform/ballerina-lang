import React from 'react';
import PropTypes from 'prop-types';
import { COMMANDS } from './../constants';
import UndoManager from './../undo-manager/undo-manager';

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
            this.undoManager = new UndoManager();
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
            this.undoManager.push(op);
        }

         /**
         * @inheritdoc
         */
        render() {
            return <Editor {...this.props} onUndoableOperation={this.onUndoableOperation} />;
        }
    }

    EditorWrapper.contextTypes = {
        command: PropTypes.shape({
            on: PropTypes.func,
            dispatch: PropTypes.func,
        }).isRequired,
    };

    return EditorWrapper;
}
