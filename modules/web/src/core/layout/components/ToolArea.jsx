import React from 'react';
import PropTypes from 'prop-types';

/**
 * React component for ToolArea Region.
 *
 * @class ToolArea
 * @extends {React.Component}
 */
class ToolArea extends React.Component {

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div className="tool-area">
                { this.props.children }
            </div>
        );
    }
}

ToolArea.propTypes = {
    children: PropTypes.arrayOf(PropTypes.element),
};

export default ToolArea;
