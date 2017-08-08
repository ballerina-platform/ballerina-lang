import React from 'react';
import PropTypes from 'prop-types';
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
        return (
            <div>
                Test Menu
            </div>
        );
    }
}

ApplicationMenu.propTypes = {
    menu: PropTypes.arrayOf(Object).isRequired,
};

export default ApplicationMenu;
