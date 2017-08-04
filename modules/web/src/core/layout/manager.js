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

import AppView from './views/App';

import { getCommandDefinitions } from './commands';
import { getHandlerDefinitions } from './handlers';
import { REGIONS } from './constants';

/**
 * LayoutManagerPlugin is responsible for loading view components in to the layout.
 *
 * @class LayoutManagerPlugin
 */
class LayoutManagerPlugin extends Plugin {

    /**
     * Bind this context for handlers
     */
    constructor() {
        super();
        this.addViewToLayout = this.addViewToLayout.bind(this);
    }

    /**
     * @inheritdoc
     */
    getID() {
        return 'composer.plugin.layout.manager';
    }

    /**
     * @inheritdoc
     */
    activate(appContext) {
        super.activate(appContext);
        this.render();
    }

    /**
     * Adds given view to the specified region.
     *
     * @param {ReactComponent} view
     * @param {String} region @see constants.js
     */
    addViewToLayout(view, region) {
        // Seperately handle views meant for static regions
        if (region === REGIONS.ACTIVITY_BAR || region === REGIONS.HEADER) {
        }
    }

    /**
     * Render layout.
     */
    render() {
        const root = React.createElement(AppView, {}, null);
        ReactDOM.render(root, document.getElementById(this.config.container));
    }

    /**
     * @inheritdoc
     */
    getCommandDefinitions() {
        return getCommandDefinitions();
    }

    /**
     * @inheritdoc
     */
    getCommandHandlerDefinitions() {
        return getHandlerDefinitions(this);
    }
}

LayoutManagerPlugin.configTypes = {
    layout: PropTypes.object,
    container: PropTypes.string.isRequired,
};

export default LayoutManagerPlugin;
