/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
/* eslint-env es6 */

import EventChannel from 'event_channel';
import LangserverChannel from './langserver-channel';
import RequestSession from './request-session';
import _ from 'lodash';
import log from 'log';


// ################ Handle the websocket closing and error as well #######################
class LangServerClientController extends EventChannel{
    constructor(options) {
        super();
        this.langserverChannel = undefined;
        this.endpoint = _.get(options, 'application.config.services.langserver.endpoint');
        this.requestSessions = [];
        this.isInitialized = false;
        this.init();
    }

    init() {
        this.langserverChannel = new LangserverChannel({ endpoint : this.endpoint, clientController: this });
        this.langserverChannel.on('connected', () => {
            this.initializeRequest();
        });
    }

    // Start Language server requests

    /**
     * Send the initialize request to the language server
     */
    initializeRequest() {
        let session = new RequestSession();
        var message = {
            id: session.getId(),
            jsonrpc: '2.0',
            method : "initialize"
        };
        session.setMessage(message);
        session.setCallback(() => {
            this.initializeResponseHandler(session);
        });
        this.requestSessions.push(session);
        this.langserverChannel.sendMessage(message);
    }

    /**
     * Send the workspace symbol request
     * @param {string} query
     * @param {function} callback Callback method for handling the response
     */
    workspaceSymbolRequest(query, callback) {
        let session = new RequestSession();
        var message = {
            id: session.getId(),
            jsonrpc: '2.0',
            method : "workspace/symbol",
            params: {
                query: query
            }
        };
        session.setMessage(message);
        session.setCallback((responseMsg) => {
            callback(responseMsg);
        });
        this.requestSessions.push(session);
        this.langserverChannel.sendMessage(message);
    }

    // End Language server requests

    // Start language server notifications

    documentDidOpenNotification(options) {
        var message = {
            jsonrpc: '2.0',
            method: 'textDocument/didOpen',
            params: {
                textDocument: options.textDocument
            }
        };

        this.langserverChannel.sendMessage(message);
    }

    documentDidCloseNotification(options) {
        var message = {
            jsonrpc: '2.0',
            method: 'textDocument/didClose',
            params: {
                textDocument: options.textDocument
            }
        };

        this.langserverChannel.sendMessage(message);
    }

    documentDidSaveNotification(options) {
        var message = {
            jsonrpc: '2.0',
            method: 'textDocument/didSave',
            params: options.didSaveParams
        };

        this.langserverChannel.sendMessage(message);
    }

    // End language server notifications

    processMessage(message) {
        let session = _.find(this.requestSessions, (session) => {
            return session.getId() === message.id;
        });
        session.executeCallback(message);
    }

    // Start Language server response handlers

    /**
     * Handle initialize response
     * @param {RequestSession} requestSession
     */
    initializeResponseHandler(requestSession) {
        // initialize response message received
        this.trigger('langserver-initialized');
        this.isInitialized = true;
    }

    initialized() {
        return this.isInitialized;
    }

    // End language server response handlers


}

export default LangServerClientController;