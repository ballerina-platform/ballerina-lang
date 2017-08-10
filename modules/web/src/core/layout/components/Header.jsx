import React from 'react';
import PropTypes from 'prop-types';

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
        return (
            <header className="header header-default">
                <div id="header-container" className="container-fluid">
                    <div className="pull-left brand">
                        <a>
                            <span>Ballerina</span><span className="appname">Composer </span>
                        </a>
                    </div>
                    { this.props.children }
                </div>
            </header>
        );
    }
}

Header.propTypes = {
    children: PropTypes.arrayOf(PropTypes.element),
};

export default Header;
