import React from 'react';
import PropTypes from 'prop-types';

/**
 * React component for BottomPanel Region.
 *
 * @class BottomPanel
 * @extends {React.Component}
 */
class BottomPanel extends React.Component {

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div className="bottom-panel">
                {this.props.children}
            </div>
        );
    }
}

BottomPanel.propTypes = {
    children: PropTypes.arrayOf(PropTypes.element),
};

export default BottomPanel;
