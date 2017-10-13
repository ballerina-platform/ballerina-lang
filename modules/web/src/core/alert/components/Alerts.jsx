import React from 'react';
import NotificationSystem from 'react-notification-system';
import PropTypes from 'prop-types';

/**
 * React component for Alerts Component.
 *
 * @extends {React.Component}
 */
class AlertsComponet extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(...args) {
        super(...args);
        this.notificationSystem = undefined;
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div>
                <NotificationSystem
                    ref={(ref) => {
                        this.notificationSystem = ref;
                        this.props.onMount(ref);
                    }
                    }
                />
            </div>
        );
    }
}

AlertsComponet.propTypes = {
    onMount: PropTypes.func.isRequired,
};

export default AlertsComponet;
