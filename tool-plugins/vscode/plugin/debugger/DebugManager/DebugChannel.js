/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

const EventEmitter = require('events');
const WebSocket = require('ws');

// See http://tools.ietf.org/html/rfc6455#section-7.4.1
const WS_NORMAL_CODE = 1000;
const WS_SSL_CODE = 1015;
const WS_PING_INTERVAL = 15000;

/**
 * Handles websocket communitation with debugger backend
 * @class DebugChannel
 * @extends {EventEmitter}
 */
class DebugChannel extends EventEmitter {
    /**
     * Creates an instance of DebugChannel.
     * @param {Object} args - connection configurations
     *
     * @memberof DebugChannel
     */
    constructor(endpoint) {
        super();
        if (!endpoint) {
            throw new Error('Invalid Endpoint');
        }
        this.endpoint = endpoint;
    }

    /**
     * Connect to debugger backend
     *
     * @memberof DebugChannel
     */
    connect() {
        const websocket = new WebSocket(this.endpoint);
        websocket.onmessage = (strMessage) => { this.parseMessage(strMessage); };
        websocket.onopen = () => { this.onOpen(); };
        websocket.onclose = (event) => { this.onClose(event); };
        websocket.onerror = (e) => { this.onError(e); };
        this.websocket = websocket;
    }
    /**
     * Parses string to JSON
     * @param {String} strMessage - message
     *
     * @memberof DebugChannel
     */
    parseMessage(strMessage) {
        const message = JSON.parse(strMessage.data);
        this.emit('onmessage', message);
    }
    /**
     * Sends message to backend
     * @param {Object} message - object to send
     *
     * @memberof DebugChannel
     */
    sendMessage(message) {
        this.websocket.send(JSON.stringify(message));
    }
    /**
     * Handles websocket onClose event
     * @param {Object} event - websocket onClose event
     *
     * @memberof DebugChannel
     */
    onClose(event) {
        clearInterval(this.ping);
        this.emit('session-terminated');
        let reason;
        if (event.code === WS_NORMAL_CODE) {
            reason = 'Normal closure';
            this.emit('session-ended');
            return;
        } else if (event.code === WS_SSL_CODE) {
            reason = 'Certificate Issue';
        } else {
            reason = `Debug socket close with reason :${event.code}`;
        }
    }
    /**
     * Handles websocket onError event
     *
     * @memberof DebugChannel
     */
    onError(e) {
        clearInterval(this.ping);
        this.emit('session-error', e, this.endpoint);
    }
    /**
     * Handles websocket onOpen event
     *
     * @memberof DebugChannel
     */
    onOpen() {
        this.emit('connected');
        //this.startPing();
    }

    /**
     * start a ping for the websocket.
     *
     * @memberof LaunchDebugChannel
     */
    startPing() {
        this.ping = setInterval(() => {
            this.sendMessage({ command: 'PING' });
        }, WS_PING_INTERVAL);
    }

    /**
     * Close websocket DebugChannel.
     *
     * @memberof LaunchDebugChannel
     */
    close() {
        clearInterval(this.ping);
        this.websocket.close();
        this.emit('connection-closed');
    }
}

module.exports = DebugChannel;
