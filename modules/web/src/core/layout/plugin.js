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
import ReactDOM from 'react-dom';
import PropTypes from 'prop-types';
import Plugin from './../plugin/plugin';
import { CONTRIBUTIONS } from './../plugin/constants';
import { MENU_DEF_TYPES as MENU_TYPES } from './../menu/constants';

import App from './components/App';

import { getCommandDefinitions } from './commands';
import { getHandlerDefinitions } from './handlers';
import { REGIONS, PLUGIN_ID, LABELS, MENUS as MENU_IDS, COMMANDS as CMD_IDS } from './constants';

/**
 * LayoutPlugin is responsible for loading view components in to the layout.
 *
 * @class LayoutPlugin
 */
class LayoutPlugin extends Plugin {

    /**
     * Bind this context for handlers
     */
    constructor() {
        super();
        this.dialogs = [];
        this.views = [];
        this.layout = {
            [REGIONS.HEADER]: [],
            [REGIONS.TOOL_AREA]: [],
            [REGIONS.ACTIVITY_BAR]: [],
            [REGIONS.LEFT_PANEL]: [],
            [REGIONS.RIGHT_PANEL]: [],
            [REGIONS.EDITOR_AREA]: [],
            [REGIONS.EDITOR_TABS]: [],
            [REGIONS.BOTTOM_PANEL]: [],
        };
        this.addViewToLayout = this.addViewToLayout.bind(this);
    }

    /**
     * @inheritdoc
     */
    getID() {
        return PLUGIN_ID;
    }

    /**
     * @inheritdoc
     */
    activate(appContext) {
        super.activate(appContext);
    }

    /**
     * Adds given view to the specified region.
     *
     * @param {Object} view definition
     */
    addViewToLayout(view) {
        this.views.push(view);
        this.layout[view.region].push(view);
    }

    /**
     * Register a dialog
     * @param {Object} dialog Dialog Definition
     */
    registerDialog(dialog) {
        this.dialogs.push(dialog);
    }

    /**
     * Render layout.
     */
    render() {
        const { layout, appContext } = this;
        const root = React.createElement(App, { layoutPlugin: this, layout, appContext }, null);
        ReactDOM.render(root, document.getElementById(this.config.container));
    }

    /**
     * @inheritdoc
     */
    getContributions() {
        const { COMMANDS, HANDLERS, MENUS } = CONTRIBUTIONS;
        return {
            [COMMANDS]: getCommandDefinitions(this),
            [HANDLERS]: getHandlerDefinitions(this),
            [MENUS]: [
                {
                    id: MENU_IDS.VIEW_MENU,
                    label: LABELS.VIEW,
                    isActive: (appContext) => {
                        return true;
                    },
                    icon: '',
                    order: 3,
                    type: MENU_TYPES.ROOT,
                },
                {
                    id: MENU_IDS.TOGGLE_BOTTOM_PANEL,
                    label: LABELS.TOGGLE_BOTTOM_PANLEL,
                    isActive: (appContext) => {
                        return true;
                    },
                    icon: '',
                    command: CMD_IDS.TOGGLE_BOTTOM_PANEL,
                    parent: MENU_IDS.VIEW_MENU,
                    type: MENU_TYPES.ITEM,
                },
            ],
        };
    }

}

LayoutPlugin.configTypes = {
    layout: PropTypes.object,
    container: PropTypes.string.isRequired,
};

export default LayoutPlugin;
