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

import { EventEmitter } from 'events';
import * as WebSocket from 'ws';
// See http://tools.ietf.org/html/rfc6455#section-7.4.1
const WS_NORMAL_CODE = 1000;
const WS_SSL_CODE = 1015;
const WS_PING_INTERVAL = 15000;

interface WebSocketOnMessageEvt {
    data: WebSocket.Data; 
    type: string; 
    target: WebSocket
}

interface WebSocketOnCloseEvt {
    wasClean: boolean;
    code: number;
    reason: string;
    target: WebSocket
}

interface WebSocketOnOpenEvt {
    target: WebSocket
}

interface WebSocketOnErrorEvt {
    error: any;
    message: string;
    type: string;
    target: WebSocket;
}

/**
 * Handles websocket communitation with debugger backend
 * @class DebugChannel
 * @extends {EventEmitter}
 */
export class DebugChannel extends EventEmitter {
    private _ping: NodeJS.Timer | undefined;
    private _endpoint: string;
    private _websocket: WebSocket | undefined;

    /**
     * Creates an instance of DebugChannel.
     * @param {string} endpoint - endpoint
     *
     * @memberof DebugChannel
     */
    constructor(endpoint: string) {
        super();
        if (!endpoint) {
            throw new Error('Invalid Endpoint');
        }
        this._endpoint = endpoint;
    }

    /**
     * Connect to debugger backend
     *
     * @memberof DebugChannel
     */
    connect() {
        const websocket = new WebSocket(this._endpoint);
        websocket.onmessage = (message: WebSocketOnMessageEvt) => { this.parseMessage(message.data); };
        websocket.onopen = (event: WebSocketOnOpenEvt) => { this.onOpen(event); };
        websocket.onclose = (event: WebSocketOnCloseEvt) => { this.onClose(event); }
        websocket.onerror = (event: WebSocketOnErrorEvt) => { this.onError(event); };
        this._websocket = websocket;
    }
    /**
     * Parses string to JSON
     * @param {WebSocket.Data} msgData - message
     *
     * @memberof DebugChannel
     */
    parseMessage(msgData: WebSocket.Data) {
        const message = JSON.parse(<string> msgData);
        this.emit('onmessage', message);
    }
    /**
     * Sends message to backend
     * @param {Object} message - object to send
     *
     * @memberof DebugChannel
     */
    sendMessage(message: Object) {
        if(this._websocket) {
            this._websocket.send(JSON.stringify(message));
        }
    }
    /**
     * Handles websocket onClose event
     * @param {WebSocketOnCloseEvt} event - websocket onClose event
     *
     * @memberof DebugChannel
     */
    onClose(event: WebSocketOnCloseEvt) {
        this.stopPing();
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
        this.emit('session-terminated', { reason });
    }
    /**
     * Handles websocket onError event
     *
     * @memberof DebugChannel
     */
    onError(event: WebSocketOnErrorEvt) {
        this.stopPing();
        this.emit('session-error',event, this._endpoint);
    }
    /**
     * Handles websocket onOpen event
     *  @param {WebSocketOnOpenEvt} event - websocket onOpen event
     *
     * @memberof DebugChannel
     */
    onOpen(event: WebSocketOnOpenEvt) {
        this.emit('connected');
        //this.startPing();
    }

    /**
     * start a ping for the websocket.
     *
     * @memberof LaunchDebugChannel
     */
    startPing() {
        this._ping = setInterval(() => {
            this.sendMessage({ command: 'PING' });
        }, WS_PING_INTERVAL);
    }

    stopPing() {
        if (this._ping) {    
            clearInterval(this._ping);
        }
    }

    /**
     * Close websocket DebugChannel.
     *
     * @memberof LaunchDebugChannel
     */
    close() {
        this.stopPing();
        if (this._websocket) { 
            this._websocket.close();
        }
        this.emit('connection-closed');
    }
}

