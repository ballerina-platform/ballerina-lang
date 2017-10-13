import React from 'react';
import PropTypes from 'prop-types';
import { createViewFromViewDef } from './utils';

/**
 * React component for Header Region.
 *
 * @class Header
 * @extends {React.Component}
 */
class Header extends React.Component {

    /**
     * @inheritdoc
     */
    shouldComponentUpdate(nextProps, nextState) {
        return !nextProps.panelResizeInProgress;
    }

    /**
     * @inheritdoc
     */
    render() {
        const { width, height, panelResizeInProgress } = this.props;
        const viewProps = { width, height, panelResizeInProgress };
        return (
            <header className="header header-default">
                <div id="header-container">
                    <div className="pull-left brand">
                        <span>Ballerina</span><span className="appname">Composer </span>
                    </div>
                    <div className="pull-left">
                        {this.props.views.map((viewDef) => {
                            return createViewFromViewDef(viewDef, viewProps);
                        })}
                    </div>
                </div>
            </header>
        );
    }
}

Header.propTypes = {
    views: PropTypes.arrayOf(Object).isRequired,
    panelResizeInProgress: PropTypes.bool.isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
};

export default Header;
