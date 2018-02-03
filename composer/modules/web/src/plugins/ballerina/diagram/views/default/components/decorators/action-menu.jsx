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
import cn from 'classnames';

const DEFAULT_MENU_ITEM_HEIGHT = '24';

/**
 * React component for the action menu.
 *
 * @class ActionMenu
 * @extends {React.Component}
 */
class ActionMenu extends React.Component {

    /**
     * Creates an instance of ActionMenu.
     * @param {Object} props Propeties for action menu.
     * @memberof ActionMenu
     */
    constructor(props) {
        super(props);
        this.actionMenuClick = this.actionMenuClick.bind(this);
        this.actionMenuWrapperRef = undefined;
        this.actionMenuRef = undefined;
    }

    /**
     * Event handler when menu open is clicked.(The hamburger icon.)
     *
     * @param {Event} e The click event.
     * @memberof ActionMenu
     */
    actionMenuClick(e) {
        this.actionMenuWrapperRef = e.currentTarget.parentNode;
        this.actionMenuRef = e.currentTarget;
        this.toggleActionMenuExpansion();
    }

    /**
     * Hide/Show the menu of the action menu.
     *
     * @param {boolean} expand true to show, else false.
     * @memberof ActionMenu
     */
    toggleActionMenuExpansion(expand) {
        if (this.actionMenuWrapperRef) {
            if (expand === undefined) {
                if (this.actionMenuWrapperRef.className.includes('expanded')) {
                    this.actionMenuWrapperRef.className.replace(' expanded', '');
                    this.actionMenuWrapperRef.style.height = '';
                } else {
                    this.actionMenuWrapperRef.className = `${this.actionMenuWrapperRef.className} expanded`;
                    this.actionMenuRef.style.height = (DEFAULT_MENU_ITEM_HEIGHT * this.props.items.length) + 'px';
                }
            } else if (expand === true) {
                this.actionMenuWrapperRef.className.replace(' expanded', '');
                this.actionMenuWrapperRef.className = `${this.actionMenuWrapperRef.className} expanded`;
                this.actionMenuRef.style.height = DEFAULT_MENU_ITEM_HEIGHT * this.props.items.length;
            } else if (expand === false) {
                this.actionMenuWrapperRef.className = this.actionMenuWrapperRef.className.replace(' expanded', '');
                this.actionMenuRef.style.height = '';
            }
        }
    }

    /**
     * Renders menu items for action menu.
     *
     * @returns {ReactElement[]} The list of menu item views.
     * @memberof ActionMenu
     */
    renderMenuItems() {
        return this.props.items.map((item) => {
            return (
                <li
                    key={`${item.key}-action-menu-item-${item.text.toLowerCase().replace(/\s/g, '')}`}
                    onClick={e => item.onClick(e)}
                >
                    <i className={cn('icon fw ', item.icon, { [`${item.className}`]: item.className !== undefined })} />
                    {item.text}
                </li>);
        });
    }

    /**
     * Renders the view of the action menu.
     *
     * @returns {ReactElement} The view.
     * @memberof ActionMenu
     */
    render() {
        if (this.props.items.length !== 0) {
            if (this.props.items.length === 1) {
                const item = this.props.items[0];
                return (<div
                    className={`action-menu ${this.props.wrapperClassName}`}
                    onClick={this.actionMenuClick}
                >
                    <div className='icon action-menu-icon-wrapper' onClick={e => item.onClick(e)}>
                        <i className={cn('icon fw ',
                            item.icon,
                            { [`${item.className}`]: item.className !== undefined })}
                        />
                    </div>
                </div>);
            } else {
                const menuItems = this.renderMenuItems();
                return (<div
                    className={`action-menu ${this.props.wrapperClassName}`}
                    onClick={this.actionMenuClick}
                >
                    <div className='icon action-menu-icon-wrapper'>
                        <i className='fw fw-menu' />
                    </div>
                    <div
                        className='menu action-menu-items-wrapper'
                        onMouseLeave={() => { this.toggleActionMenuExpansion(false); }}
                    >
                        <ul>
                            {menuItems}
                        </ul>
                    </div>
                </div>);
            }
        } else {
            return (null);
        }
    }
}

ActionMenu.propTypes = {
    items: PropTypes.arrayOf(PropTypes.shape({
        key: PropTypes.string.isRequired,
        className: PropTypes.string,
        icon: PropTypes.string.isRequired,
        text: PropTypes.string.isRequired,
        onClick: PropTypes.func.isRequired,
    })),
    wrapperClassName: PropTypes.string,
};

ActionMenu.defaultProps = {
    items: [],
    wrapperClassName: '',
};

export default ActionMenu;
