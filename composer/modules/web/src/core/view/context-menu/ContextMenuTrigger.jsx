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
 *
 */

import React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import uuid from 'uuid/v4';
import classnames from 'classnames';
import { ContextMenuTrigger as ReactContextMenuTrigger, ContextMenu, MenuItem } from 'react-contextmenu';

/**
 * Class to represent ContextMenuTrigger
 */
class ContextMenuTrigger extends React.Component {

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div>
                <ReactContextMenuTrigger
                    id={this.props.id}
                    holdToDisplay={1000}
                    collect={() => {}}
                >
                    {this.props.children}
                </ReactContextMenuTrigger>
                <ContextMenu id={this.props.id} onShow={this.props.onShow} onHide={this.props.onHide} >
                    {this.props.menu.map((menuItem, i) => {
                        const { divider, handler, label, isActive } = menuItem;
                        return (
                            divider
                                ? <MenuItem key={i} divider />
                                : <MenuItem
                                    key={i}
                                    disabled={_.isFunction(isActive) ? !isActive() : false}
                                    onClick={handler}
                                >
                                    {label}
                                </MenuItem>
                        );
                    })}
                </ContextMenu>
            </div>
        );
    }

}

const MenuDef = PropTypes.shape({
    divider: PropTypes.bool,
    icon: PropTypes.string,
    label: PropTypes.string,
    handler: PropTypes.func,
    isActive: PropTypes.func,
    children: PropTypes.arrayOf(Object),
});

ContextMenuTrigger.propTypes = {
    id: PropTypes.string,
    children: PropTypes.node,
    menu: PropTypes.arrayOf(MenuDef),
    onShow: PropTypes.func,
    onHide: PropTypes.func,
};

ContextMenuTrigger.defaultProps = {
    id: uuid(),
    children: [],
    menu: [],
    onShow: () => {},
    onHide: () => {},
};

export default ContextMenuTrigger;
