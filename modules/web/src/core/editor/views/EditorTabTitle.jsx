import React from 'react';
import PropTypes from 'prop-types';
import { EVENTS as WORKSPACE_EVENTS } from './../../workspace/constants';

/**
 * React component for Editor Tab Title
 */
class EditorTabTitle extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(props) {
        super(props);
        this.state = {
            isFileDirty: props.editor.file.isDirty,
        };
        this.onFileDirtyStateChange = this.onFileDirtyStateChange.bind(this);
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        this.props.editor.file
            .on(WORKSPACE_EVENTS.DIRTY_STATE_CHANGE, this.onFileDirtyStateChange);
    }

    /**
     * @inheritdoc
     */
    componentWillUnmount() {
        this.props.editor.file
            .off(WORKSPACE_EVENTS.DIRTY_STATE_CHANGE, this.onFileDirtyStateChange);
    }

    /**
     * @inheritdoc
     */
    componentWillReceiveProps(nextProps) {
        if (this.props.editor.file.id !== nextProps.editor.file.id) {
            this.props.editor.file
                .off(WORKSPACE_EVENTS.DIRTY_STATE_CHANGE, this.onFileDirtyStateChange);
            nextProps.editor.file
                .on(WORKSPACE_EVENTS.DIRTY_STATE_CHANGE, this.onFileDirtyStateChange);
            this.setState({
                isFileDirty: nextProps.editor.file.isDirty,
            });
        }
    }

    /**
     * When File's dirty state changed
     * @param {boolean} isFileDirty Flag to indicate dirty state
     */
    onFileDirtyStateChange(isFileDirty) {
        this.setState({
            isFileDirty,
        });
    }

    /**
     * @inheritdoc
     */
    render() {
        const { editor, editor: { file }, onTabClose, customClass } = this.props;
        return (
            <div data-extra="tab-bar-title" className={`tab-title-wrapper ${customClass}`}>
                <i className="fw fw-ballerina tab-icon" />
                {file.name}
                {this.state.isFileDirty && <span className="dirty-indicator">*</span> }
                <button
                    type="button"
                    className="close close-tab pull-right"
                    onClick={() => onTabClose(editor)}
                >
                    ×
                </button>
            </div>
        );
    }
}

EditorTabTitle.propTypes = {
    customClass: PropTypes.string,
    editor: PropTypes.objectOf(Object).isRequired,
    onTabClose: PropTypes.func.isRequired,
};

EditorTabTitle.defaultProps = {
    customClass: '',
};

export default EditorTabTitle;
