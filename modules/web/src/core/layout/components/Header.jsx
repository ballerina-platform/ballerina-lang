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
    render() {
        const { width, height } = this.props;
        return (
            <header className="header header-default">
                <div id="header-container">
                    <div className="pull-left brand">
                        <span>Ballerina</span><span className="appname">Composer </span>
                    </div>
                    <div className="pull-left">
                        {this.props.views.map((viewDef) => {
                            return createViewFromViewDef(viewDef, { width, height });
                        })}
                    </div>
                </div>
            </header>
        );
    }
}

Header.propTypes = {
    views: PropTypes.arrayOf(Object).isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
};

export default Header;
