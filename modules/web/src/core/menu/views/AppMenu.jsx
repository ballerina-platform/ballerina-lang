import React from 'react';
import PropTypes from 'prop-types';
import { DropdownButton, MenuItem } from 'react-bootstrap';
import View from './../../view/view';
import { VIEW_IDS } from './../constants';

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
            const items = [];
            root.children.forEach((item, index) => {
                items.push(
                    <MenuItem eventKey={index}>{item.label}</MenuItem>
                );
            });
            roots.push(
                <DropdownButton noCaret title={root.label} id={root.id} >
                    {items}
                </DropdownButton >
            );
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
