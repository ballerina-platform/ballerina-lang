import React from 'react';
import PropTypes from 'prop-types';
import BallerinaFileEditor from 'ballerina/views/ballerina-file-editor';
import { EVENTS as EDITOR_EVENTS } from 'core/editor/constants';
import { withUndoRedoSupport } from 'core/editor/views/utils';
import { EVENTS as WORKSPACE_EVENTS } from 'core/workspace/constants';
import UndoableBalEditorOperation from './../model/undoable-bal-editor-operation';


/**
 * Editor for Bal Files
 */
class Editor extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(...args) {
        super(...args);
        this.onFileContentModified = this.onFileContentModified.bind(this);
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        this.props.file.on(WORKSPACE_EVENTS.CONTENT_MODIFIED, this.onFileContentModified);
    }

    /**
     * @inheritdoc
     */
    shouldComponentUpdate(nextProps, nextState) {
        return nextProps.isActive;
    }

    /**
     * On File Modifications
     */
    onFileContentModified(changeEvent) {
        if (changeEvent.originEvt.type !== EDITOR_EVENTS.UNDO_EVENT
            && changeEvent.originEvt.type !== EDITOR_EVENTS.REDO_EVENT) {
            const undoableOp = new UndoableBalEditorOperation({
                file: this.props.file,
                changeEvent,
            });
            this.props.onUndoableOperation(undoableOp);
        }
    }

    /**
     * @inheritdoc
     */
    componetWillUnmount() {
        this.props.file.off(WORKSPACE_EVENTS.CONTENT_MODIFIED, this.onFileContentModified);
    }

    /**
     * @inheritdoc
     */
    render() {
        const { width, height } = this.props;
        return (
            <div className="ballerina-editor" style={{ width, height }}>
                <BallerinaFileEditor {...this.props} />
            </div>
        );
    }
}

Editor.propTypes = {
    editorModel: PropTypes.objectOf(Object).isRequired,
    file: PropTypes.objectOf(Object).isRequired,
    panelResizeInProgress: PropTypes.bool.isRequired,
    isActive: PropTypes.bool.isRequired,
    commandProxy: PropTypes.shape({
        on: PropTypes.func.isRequired,
        dispatch: PropTypes.func.isRequired,
        getCommands: PropTypes.func.isRequired,
    }).isRequired,
    isPreviewViewEnabled: PropTypes.bool.isRequired,
    ballerinaPlugin: PropTypes.objectOf(Object).isRequired,
    onUndoableOperation: PropTypes.func.isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
};

Editor.defaultProps = {
    isPreviewViewEnabled: false,
};

export default withUndoRedoSupport(Editor);
