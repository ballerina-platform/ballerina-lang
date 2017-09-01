import _ from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';
/**
 * React component for EditorArea Region.
 *
 * @class EditorArea
 * @extends {React.Component}
 */
class EditorArea extends React.Component {
    /**
     * @inheritdoc
     */
    render() {
        return (
            <div className="editor-area" >
                { this.props.children }
            </div>
        );
    }
}

EditorArea.propTypes = {
    children: PropTypes.arrayOf(PropTypes.element),
};

export default EditorArea;
