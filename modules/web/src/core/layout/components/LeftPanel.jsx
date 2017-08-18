import React from 'react';
import PropTypes from 'prop-types';

/**
 * React component for LeftPanel Region.
 *
 * @class LeftPanel
 * @extends {React.Component}
 */
class LeftPanel extends React.Component {

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div className="left-panel">
                {this.props.children}
            </div>
        );
    }
}

LeftPanel.propTypes = {
    children: PropTypes.arrayOf(PropTypes.element),
};

export default LeftPanel;
