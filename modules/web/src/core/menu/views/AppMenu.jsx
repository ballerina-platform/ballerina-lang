import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import Menu, { SubMenu, MenuItem } from 'rc-menu';
import View from './../../view/view';
import { VIEW_IDS, MENU_DEF_TYPES } from './../constants';
import 'rc-menu/assets/index.css';

/**
 * Render a menu item
 * @param {Object} item Menu Definition
 * @returns {React.Component}
 */
function renderMenuNode(item) {
    const { type, id, label } = item;
    const children = [];
    if (!_.isNil(item.children) && !_.isEqual(type, MENU_DEF_TYPES.ITEM)) {
        item.children.forEach((child) => {
            children.push(renderMenuNode(child));
        });
    }

    switch (type) {
    case MENU_DEF_TYPES.ROOT:
        return (
            <SubMenu disabled={!item.isActive()} title={label} key={id}>
                {children}
            </SubMenu >
        );
    case MENU_DEF_TYPES.GROUP:
        return (
            <SubMenu
                disabled={!item.isActive()}
                title={
                    <div style={{ display: 'iniline-block' }}>
                        <div style={{ float: 'left', width: '33%' }}>
                            <i className={`fw fw-${item.icon}`} />
                        </div>
                        <div className="menu-label" style={{ display: 'inline-block', width: '33%' }}>
                            {label}
                        </div>
                        <div style={{ float: 'right', width: '33%' }}>
                            <i className={'fw fw-right'} style={{ marginRight: '5px' }} />
                        </div>
                    </div>
                }
                key={id}
            >
                {children}
            </SubMenu >
        );
    case MENU_DEF_TYPES.ITEM:
        return (
            <MenuItem disabled={!item.isActive()} key={id}>
                <div style={{ minWidth: '100px', display: 'inline' }}>
                    <i className={`fw fw-${item.icon}`} style={{ marginRight: '5px' }} />
                </div>
                <span className="menu-label">
                    {label}
                </span>
            </MenuItem>
        );
    default:
        return (<div />);
    }
}

/**
 * Application Menu Controller
 */
class ApplicationMenu extends View {

    /**
     * @inheritdoc
     */
    getID() {
        return VIEW_IDS.APP_MENU;
    }

    /**
     * @inheritdoc
     */
    render() {
        const roots = [];
        this.props.menu.forEach((root) => {
            roots.push(renderMenuNode(root));
        });

        return (
            <div className="application-menu">
                <Menu mode="horizontal" openAnimation="slide-up">
                    {roots}
                </Menu>
            </div>
        );
    }
}

ApplicationMenu.propTypes = {
    menu: PropTypes.arrayOf(Object).isRequired,
};

export default ApplicationMenu;
