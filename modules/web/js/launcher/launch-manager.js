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

import _ from 'lodash';
import EventChannel from 'event_channel';
import Console from 'console';
import LaunchChannel from './launch-channel';
/**
 * Launch Manager
 * @class LaunchManager
 * @extends {EventChannel}
 */
class LaunchManager extends EventChannel {
    /**
     * Creates an instance of LaunchManager.
     *
     * @memberof LaunchManager
     */
    constructor() {
        super();
        this.enable = false;
        this.channel = undefined;
        this.active = false;
    }
    /**
     *
     * @param {File} file - File instance
     *
     * @memberof LaunchManager
     */
    runApplication(file) {
        this.channel = new LaunchChannel({ endpoint: this.endpoint, launcher: this });
        this.openConsole();
        this.channel.on('connected', _.bindKey(this, 'sendRunApplicationMessage', file));
    }
    /**
     * Run Ballerina Service
     * @param {File} file - File instance
     *
     * @memberof LaunchManager
     */
    runService(file) {
        this.channel = new LaunchChannel({ endpoint: this.endpoint, launcher: this });
        this.openConsole();
        this.channel.on('connected', () => { this.sendRunServiceMessage(file); });
    }
    /**
     * Run Ballerina main
     * @param {File} file - File instance
     *
     * @memberof LaunchManager
     */
    debugApplication(file) {
        this.channel = new LaunchChannel({ endpoint: this.endpoint, launcher: this });
        this.openConsole();
        this.channel.on('connected', () => { this.sendDebugApplicationMessage(file); });
    }
    /**
     * Debug ballerina Service
     * @param {File} file - File instance
     *
     * @memberof LaunchManager
     */
    debugService(file) {
        this.channel = new LaunchChannel({ endpoint: this.endpoint, launcher: this });
        this.openConsole();
        this.channel.on('connected', () => { this.sendDebugServiceMessage(file); });
    }
    /**
     * Send message to ballerina program
     * @param {File} file - File instance
     *
     * @memberof LaunchManager
     */
    sendRunApplicationMessage(file) {
        const message = {
            command: 'RUN_PROGRAM',
            fileName: file.getName(),
            filePath: file.getPath(),
            commandArgs: this.getApplicationConfigs(file),
        };
        this.channel.sendMessage(message);
    }
    /**
     * Send message to ballerina service
     * @param {File} file - File instance
     *
     * @memberof LaunchManager
     */
    sendRunServiceMessage(file) {
        const message = {
            command: 'RUN_SERVICE',
            fileName: file.getName(),
            filePath: file.getPath(),
        };
        this.channel.sendMessage(message);
    }
    /**
     * Send message to debugging ballerina program
     * @param {File} file - File instance
     *
     * @memberof LaunchManager
     */
    sendDebugApplicationMessage(file) {
        const message = {
            command: 'DEBUG_PROGRAM',
            fileName: file.getName(),
            filePath: file.getPath(),
            commandArgs: this.getApplicationConfigs(file),
        };
        this.channel.sendMessage(message);
    }
    /**
     * Send message to debugging ballerina service
     * @param {File} file - File instance
     *
     * @memberof LaunchManager
     */
    sendDebugServiceMessage(file) {
        const message = {
            command: 'DEBUG_SERVICE',
            fileName: file.getName(),
            filePath: file.getPath(),
        };
        this.channel.sendMessage(message);
    }
    /**
     *
     * @param {Object} message - Process message from backend
     *
     * @memberof LaunchManager
     */
    processMesssage(message) {
        if (message.code === 'OUTPUT') {
            if (_.endsWith(message.message, this.debugPort)) {
                this.trigger('debug-active', this.debugEndpoint);
                return;
            }
        }
        if (message.code === 'EXECUTION_STARTED') {
            this.active = true;
            this.trigger('execution-started');
        }
        if (message.code === 'EXECUTION_STOPED' || message.code === 'EXECUTION_TERMINATED') {
            this.active = false;
            this.trigger('execution-ended');
            this.channel.close();
        }
        if (message.code === 'DEBUG_PORT') {
            this.debugPort = message.port;
            return;
        }
        if (message.code === 'EXIT') {
            this.active = false;
            this.trigger('session-ended');
            // close the current channel.
            this.channel.close();
        }
        if (message.code === 'PONG') {
            // if a pong message is received we will ignore.
            return;
        }
        if (message.code === 'INVALID_CMD') {
            // ignore and return.
            return;
        }
        Console.println(message);
    }
    /**
     * Clear and Open console view
     * @memberof LaunchManager
     */
    openConsole() {
        Console.clear();
        Console.show();
    }
    /**
     * Init launch manager
     * @param {any} options - Config options
     *
     * @memberof LaunchManager
     */
    init(options) {
        this.endpoint = _.get(options, 'application.config.services.launcher.endpoint');
        this.debugEndpoint = _.get(options, 'application.config.services.debugger.endpoint');
        this.enable = true;
        this.application = options.application;
        Console.setApplication(this.application);
    }
    /**
     * Stop ballerina program
     *
     * @memberof LaunchManager
     */
    stopProgram() {
        const message = {
            command: 'TERMINATE',
        };
        this.channel.sendMessage(message);
    }
    /**
     * Get application configs
     * @param {File} file - File instance
     * @returns {Object} application configs
     *
     * @memberof LaunchManager
     */
    getApplicationConfigs(file) {
        const args = this.application.browserStorage.get(`launcher-app-configs-${file.id}`);
        return args || '';
    }
}

export default new LaunchManager();
