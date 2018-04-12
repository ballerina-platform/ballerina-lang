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
import LaunchChannel from './LaunchChannel';

class LaunchManager extends EventChannel {
    constructor() {
        super();
        this.active = false;
        this._messages = [];
    }

    set active(active) {
        this._active = active;
    }

    get active() {
        return this._active;
    }

    set messages(messages) {
        this._messages = messages;
    }

    get messages() {
        return this._messages;
    }

    init(endpoint) {
        this.endpoint = endpoint;
    }

    run(file, debug = false, configs) {
        this._messages = [];
        let command;
        if (debug) {
            command = 'DEBUG_PROGRAM';
        } else {
            command = 'RUN_PROGRAM';
        }
        this.channel = new LaunchChannel(this.endpoint);
        this.channel.on('connected', () => {
            this.sendRunApplicationMessage(file, command, configs);
        });
        this.channel.on('onmessage', (message) => {
            this.processMesssage(message);
        });
    }

    stop() {
        const message = {
            command: 'TERMINATE',
        };
        this.channel.sendMessage(message);
    }

    /**
     * Send message to ballerina program
     * @param {File} file - File instance
     *
     * @memberof LaunchManager
     */
    sendRunApplicationMessage(file, command, configs) {
        const message = {
            command,
            fileName: `${file.name}.${file.extension}`,
            filePath: file.path,
            commandArgs: configs,
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
        if (message.code === 'PONG') {
            // if a pong message is received we will ignore.
            return;
        }
        if (message.code === 'INVALID_CMD') {
            // ignore and return.
            return;
        }
        this._messages.push(message);
        if (message.code === 'OUTPUT') {
            if (_.endsWith(message.message, this.debugPort)) {
                this.trigger('debug-active', this.debugPort);
                return;
            }
        }
        if (message.code === 'EXECUTION_STARTED') {
            this.active = true;
            this.trigger('execution-started');
            this._messages = [message];
        }
        if (message.code === 'EXECUTION_STOPPED' || message.code === 'EXECUTION_TERMINATED') {
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
            // this.tryItUrl = undefined;
        }
        if (message.type === 'TRACE') {
            message.message = JSON.parse(message.message);
            message.message.meta = JSON.parse(message.message.meta);
            this.trigger('print-trace-message', message);
            return;
        }
        this.trigger('print-message', message);
    }
}

export default new LaunchManager();
