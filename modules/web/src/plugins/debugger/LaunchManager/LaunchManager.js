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

    init(endpoint) {
        this.endpoint = endpoint;
    }

    run(file, debug = false) {
        let command;
        if (debug) {
            command = 'DEBUG_PROGRAM';
        } else {
            command = 'RUN_PROGRAM';
        }
        this.channel = new LaunchChannel(this.endpoint);
        this.channel.on('connected', () => {
            this.sendRunApplicationMessage(file, command);
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
    sendRunApplicationMessage(file, command) {
        const message = {
            command,
            fileName: `${file.name}.${file.extension}`,
            filePath: file.path,
            // commandArgs: this.getApplicationConfigs(file),
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
                this.trigger('debug-active', this.debugPort);
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
            // this.tryItUrl = undefined;
        }
        if (message.code === 'PONG') {
            // if a pong message is received we will ignore.
            return;
        }
        if (message.code === 'INVALID_CMD') {
            // ignore and return.
            return;
        }
        this.trigger('print-message', message);
        // if (message.code === 'TRY_IT_URL') {
        //     this.tryItUrl = message.message;
        //     this.trigger('try-it-url-received', message.message);
        //     this.application.commandManager.dispatch('try-it-url-received', message.message);
        // return;
        // }
    }
}

export default new LaunchManager();
