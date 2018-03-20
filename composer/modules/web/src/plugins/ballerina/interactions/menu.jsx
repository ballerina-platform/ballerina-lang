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

/**
 * Interaction menu component
 */
class Menu extends React.Component {
    /**
     * render hover area and button
     * @return {object} button rendering object
     */
    render() {
        const maxHeight = this.props.maxHeight;
        let divStyle = {};
        if (maxHeight > 0) {
            divStyle = { maxHeight: this.props.maxHeight + 'px', 'overflow-y': 'auto' };
        }
        return (
            <nav style={divStyle} className='interaction-menu'>
                {this.props.children}
            </nav>
        );
    }
}

Menu.propTypes = {
    children: PropTypes.node.isRequired,
    maxHeight: PropTypes.number,
};

Menu.defaultProps = {
    maxHeight: 0,
};
export default Menu;
