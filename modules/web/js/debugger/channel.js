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
import log from 'log';

// See http://tools.ietf.org/html/rfc6455#section-7.4.1
const WS_NORMAL_CODE = 1000;
const WS_SSL_CODE = 1015;
const WS_PING_INTERVAL = 15000;

/**
 * Handles websocket communitation with debugger backend
 * @class Channel
 * @extends {EventChannel}
 */
class Channel extends EventChannel {
    /**
     * Creates an instance of Channel.
     * @param {Object} args - connection configurations
     *
     * @memberof Channel
     */
    constructor(args) {
        super();
        if (_.isNil(args.endpoint)) {
            throw new Error('Invalid Endpoint');
        }
        _.assign(this, args);
    }

    /**
     * Connect to debugger backend
     *
     * @memberof Channel
     */
    connect() {
        const websocket = new WebSocket(this.endpoint);
        websocket.onmessage = (strMessage) => { this.parseMessage(strMessage); };
        websocket.onopen = () => { this.onOpen(); };
        websocket.onclose = (event) => { this.onClose(event); };
        websocket.onerror = () => { this.onError(); };
        this.websocket = websocket;
    }
    /**
     * Parses string to JSON
     * @param {String} strMessage - message
     *
     * @memberof Channel
     */
    parseMessage(strMessage) {
        const message = JSON.parse(strMessage.data);
        this.debugger.processMesssage(message);
    }
    /**
     * Sends message to backend
     * @param {Object} message - object to send
     *
     * @memberof Channel
     */
    sendMessage(message) {
        this.websocket.send(JSON.stringify(message));
    }
    /**
     * Handles websocket onClose event
     * @param {Object} event - websocket onClose event
     *
     * @memberof Channel
     */
    onClose(event) {
        clearInterval(this.ping);
        this.debugger.active = false;
        this.debugger.trigger('session-terminated');
        let reason;
        if (event.code === WS_NORMAL_CODE) {
            reason = 'Normal closure';
            this.trigger('session-ended');
            this.debugger.active = false;
            return;
        } else if (event.code === WS_SSL_CODE) {
            reason = 'Certificate Issue';
        } else {
            reason = `Debug socket close with reason :${event.code}`;
        }
        log.debug(reason);
    }
    /**
     * Handles websocket onError event
     *
     * @memberof Channel
     */
    onError() {
        clearInterval(this.ping);
        this.debugger.active = false;
        this.debugger.trigger('session-error');
    }
    /**
     * Handles websocket onOpen event
     *
     * @memberof Channel
     */
    onOpen() {
        this.debugger.active = true;
        this.debugger.trigger('session-started');
        this.startPing();
    }

    /**
     * start a ping for the websocket.
     *
     * @memberof LaunchChannel
     */
    startPing() {
        this.ping = setInterval(() => {
            this.sendMessage({ command: 'PING' });
        }, WS_PING_INTERVAL);
    }

    /**
     * Close websocket channel.
     *
     * @memberof LaunchChannel
     */
    close() {
        clearInterval(this.ping);
    }
}

export default Channel;
