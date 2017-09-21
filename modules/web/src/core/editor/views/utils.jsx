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
        isActive: PropTypes.bool.isRequired,
        commandProxy: PropTypes.shape({
            on: PropTypes.func.isRequired,
            dispatch: PropTypes.func.isRequired,
            getCommands: PropTypes.func.isRequired,
        }).isRequired,
    };

    return EditorWrapper;
}
