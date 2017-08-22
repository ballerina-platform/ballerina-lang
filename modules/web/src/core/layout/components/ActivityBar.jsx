import React from 'react';
import PropTypes from 'prop-types';

/**
 * React component for Activity Bar Region.
 *
 * @class ActivityBar
 * @extends {React.Component}
 */
class ActivityBar extends React.Component {

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div className="activity-bar">
                { this.props.children }
            </div>
        );
    }
}

ActivityBar.propTypes = {
    children: PropTypes.node,
};

export default ActivityBar;
