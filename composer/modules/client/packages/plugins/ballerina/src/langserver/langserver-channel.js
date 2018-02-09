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

class LangserverChannel extends EventChannel {
    constructor(args) {
        super();
        if (_.isNil(args.endpoint)) {
            throw 'Invalid Endpoint';
        }
        // assign provided ws close handler.
        if (!_.isNil(args.clientController.options) && !_.isNil(args.clientController.options.wsCloseEventHandler)) {
            this.wsCloseEventHandler = args.clientController.options.wsCloseEventHandler;
        }
        _.assign(this, args);

        this.connect();
    }

    connect() {
        const websocket = new WebSocket(this.endpoint);
        // bind functions
        websocket.onmessage = (strMessage) => { this.parseMessage(strMessage); };
        websocket.onopen = () => { this.onOpen(); };
        websocket.onclose = (event) => { this.onClose(event); };
        websocket.onerror = (error) => { this.onError(error); };
        this.websocket = websocket;
    }

    parseMessage(strMessage) {
        let json = strMessage.data;
        if (json.startsWith('Content-Length')) {
            json = json.split(/Content-Length:\s[\d]*\s\n/)[1];
        }
        const message = JSON.parse(json);
        this.clientController.processMessage(message);
    }

    sendMessage(msg) {
        this.websocket.send(JSON.stringify(msg));
    }

    onClose(event) {
        clearInterval(this.ping);
        if (this.wsCloseEventHandler) {
            this.wsCloseEventHandler(event);
        } else {
            // invoke default ws close event handler.
            this.defaultWsCloseEventHandler(event);
        }
        this.trigger('close');
    }

    onError(error) {
        this.clientController.active = false;
        this.clientController.trigger('session-error');
        this.trigger('error', error);
        clearInterval(this.ping);
    }

    onOpen() {
        this.clientController.active = true;
        this.trigger('connected');
        this.startPing();
    }

    close() {
        this.websocket.close();
    }

    /**
     * start a ping for the websocket.
     *
     * @memberof LangserverChannel
     */
    startPing() {
        this.ping = setInterval(() => {
            this.sendMessage({ method: 'PING' });
        }, WS_PING_INTERVAL);
    }

    /**
     * Default web socket close event handler. This will be called upon websocket onClose event, only if
     * there is no any other wscloseEventHandler is specified while LangserverChannel initialization.
     * @param {Object} event
     */
    defaultWsCloseEventHandler(event) {
        this.clientController.active = false;
        this.clientController.trigger('session-terminated');
        let reason;
        if (event.code === WS_NORMAL_CODE) {
            reason = 'Normal closure';
            this.trigger('session-ended');
            this.debugger.active = false;
            return;
        } else if (event.code === WS_SSL_CODE) {
            reason = 'Certificate Issue';
        } else {
            reason = `Unknown reason :${event.code}`;
        }
        log.debug(`Web socket closed, reason ${reason}`);
        // After the internal server errors, websocket is being closed immediately and we re initialize the connection
        this.connect();
    }
}

export default LangserverChannel;
