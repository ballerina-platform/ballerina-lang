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

class LangserverChannel extends EventChannel {
    constructor(args) {
        super();
        if (_.isNil(args.endpoint)) {
            throw 'Invalid Endpoint';
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
        const message = JSON.parse(strMessage.data);
        this.clientController.processMessage(message);
    }

    sendMessage(msg) {
        this.websocket.send(JSON.stringify(msg));
    }

    onClose(event) {
        // this.clientController.active = false;
        // this.clientController.trigger('session-terminated');
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

    onError(error) {
        this.clientController.active = false;
        this.clientController.trigger('session-error');
        this.trigger('error', error);
    }

    onOpen() {
        this.clientController.active = true;
        this.trigger('connected');
    }
}

export default LangserverChannel;
