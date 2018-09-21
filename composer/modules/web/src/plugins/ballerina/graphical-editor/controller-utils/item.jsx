/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import React from 'react';
import PropTypes from 'prop-types';
import './interactions.scss';

/**
 * Interaction menu item component
 */
class Item extends React.Component {
    /**
     * render hover area and button
     * @return {object} button rendering object
     */
    render() {
        return (
            <a
                className='interaction-menu-item'
                onClick={() => {
                    this.props.callback(this.props.data);
                    if (this.props.closeMenu) {
                        this.context.menuCloseCallback();
                    }
                }}
            >{this.props.icon !== '' && <i className={'button-icon ' + this.props.icon} />}{this.props.label}</a>);
    }
}

Item.propTypes = {
    icon: PropTypes.string,
    label: PropTypes.string.isRequired,
    callback: PropTypes.func,
    data: PropTypes.object,
    closeMenu: PropTypes.bool,
};

Item.defaultProps = {
    callback: () => {},
    data: {},
    icon: '',
    closeMenu: true,
};

Item.contextTypes = {
    menuCloseCallback: PropTypes.func,
};

export default Item;
