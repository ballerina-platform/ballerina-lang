import React from 'react';
import PropTypes from 'prop-types';

/**
 * Editor for Bal Files
 */
class Editor extends React.Component {

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div>
                <p>{this.props.file.content}</p>
            </div>
        );
    }
}

Editor.propTypes = {
    file: PropTypes.objectOf(Object).isRequired,
};

export default Editor;
