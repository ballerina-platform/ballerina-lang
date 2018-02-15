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
import { CONTRIBUTIONS } from '../plugin/constants';
import log from '../log/log';

import { getCommandDefinitions } from './commands';
import { getHandlerDefinitions } from './handlers';
import { PLUGIN_ID, TYPES } from './constants';

import Alerts from './components/Alerts';

/**
 * AlertPlugin is responsible for showing alerts.
 *
 * @class AlertPlugin
 */
class AlertPlugin extends Plugin {

    /**
     * @inheritdoc
     */
    constructor() {
        super();
        this.notificationSystem = undefined;
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
    init(...args) {
        super.init(...args);
        return {
            showInfo: this.showAlert.bind(this, TYPES.INFO),
            showSuccess: this.showAlert.bind(this, TYPES.SUCCESS),
            showWarning: this.showAlert.bind(this, TYPES.WARNING),
            showError: this.showAlert.bind(this, TYPES.ERROR),
        };
    }

    /**
     * @inheritdoc
     */
    activate(...args) {
        super.activate(...args);
        const root = React.createElement(Alerts, { onMount: (ref) => {
            this.notificationSystem = ref;
        } }, null);
        ReactDOM.render(root, document.getElementById(this.config.container));
    }

    /**
     * Show an alert
     * @param {String} level Alert type
     * @param {String} message Alert message
     */
    showAlert(level, message) {
        this.notificationSystem.addNotification({
            message,
            level,
        });
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

AlertPlugin.configTypes = {
    container: PropTypes.string.isRequired,
};

export default AlertPlugin;
