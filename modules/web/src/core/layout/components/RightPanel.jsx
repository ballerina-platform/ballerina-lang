import React from 'react';
import PropTypes from 'prop-types';

/**
 * React component for RightPanel Region.
 *
 * @class RightPanel
 * @extends {React.Component}
 */
class RightPanel extends React.Component {

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div className="right-panel">
                {this.props.children}
            </div>
        );
    }
}

RightPanel.propTypes = {
    children: PropTypes.arrayOf(PropTypes.element),
};

export default RightPanel;
