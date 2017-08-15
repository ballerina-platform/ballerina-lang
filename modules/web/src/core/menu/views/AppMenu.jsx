import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { DropdownButton, MenuItem } from 'react-bootstrap';
import View from './../../view/view';
import { VIEW_IDS, MENU_DEF_TYPES } from './../constants';

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
            <DropdownButton noCaret key={id} title={label} id={id} >
                {children}
            </DropdownButton >
        );
    case MENU_DEF_TYPES.GROUP:
        return (
            <DropdownButton pullRight noCaret key={id} title={label} id={id} >
                {children}
            </DropdownButton >
        );
    case MENU_DEF_TYPES.ITEM:
        return (
            <MenuItem key={id}>
                <i className={`fw fw-${item.icon}`} style={{ marginRight: '5px' }} />
                {label}
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
                {roots}
            </div>
        );
    }
}

ApplicationMenu.propTypes = {
    menu: PropTypes.arrayOf(Object).isRequired,
};

export default ApplicationMenu;
