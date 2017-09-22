import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import View from './../../view/view';
import { VIEW_IDS } from './../constants';
import './tool-bar.scss';

/**
 * ToolBar
 */
class ToolBar extends View {

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
            <div className="tool-bar">
                Tool Bar
            </div>
        );
    }
}

ToolBar.propTypes = {
    dispatch: PropTypes.func.isRequired,
    tools: PropTypes.arrayOf(Object).isRequired,
};

export default ToolBar;
