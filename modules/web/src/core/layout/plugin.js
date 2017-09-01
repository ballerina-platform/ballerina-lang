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

import App from './components/App';

import { getCommandDefinitions } from './commands';
import { getHandlerDefinitions } from './handlers';
import { REGIONS, PLUGIN_ID } from './constants';

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
        this.layout = {
            [REGIONS.HEADER]: [],
            [REGIONS.ACTIVITY_BAR]: [],
            [REGIONS.LEFT_PANEL]: [],
            [REGIONS.RIGHT_PANEL]: [],
            [REGIONS.EDITOR_AREA]: [],
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
        const root = React.createElement(App, { layout, appContext }, null);
        ReactDOM.render(root, document.getElementById(this.config.container));
    }

    /**
     * @inheritdoc
     */
    getContributions() {
        const { COMMANDS, HANDLERS } = CONTRIBUTIONS;
        return {
            [COMMANDS]: getCommandDefinitions(this),
            [HANDLERS]: getHandlerDefinitions(this),
        };
    }

}

LayoutPlugin.configTypes = {
    layout: PropTypes.object,
    container: PropTypes.string.isRequired,
};

export default LayoutPlugin;
