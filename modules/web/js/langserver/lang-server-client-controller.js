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

import _ from 'lodash';
import EventChannel from 'event_channel';
import LangserverChannel from './langserver-channel';
import RequestSession from './request-session';
import { getServiceEndpoint } from './../api-client/api-client';

//holds the singleton of lang server client
let instance = undefined;

/**
 * Class for language server client controller
 */
class LangServerClientController extends EventChannel {
    /**
     * Constructor for LangServerClientController
     * @param {object} options - constructor options
     * @constructor
     */
    constructor(options) {
        super();
        this.langserverChannel = undefined;
        this.requestSessions = [];
        this.isInitialized = false;
    }

    /**
     * Initialize LangServerClientController
     */
    init() {
        return new Promise((resolve, reject) => {
            this.langserverChannel = new LangserverChannel({ 
                /** fetch endpoint from util */
                endpoint: getServiceEndpoint('langserver'), 
                clientController: this });
            this.langserverChannel.on('connected', () => {
                this.initializeRequest()
                    .then(resolve)
                    .catch(reject);
            });
            this.langserverChannel.on('error', (error) => {
                instance = undefined;
                reject(error);
            });
        });
    }

    // Start Language server requests

    /**
     * Send the initialize request to the language server
     */
    initializeRequest() {
        return new Promise((resolve, reject) => {
            const session = new RequestSession();
            const message = {
                id: session.getId(),
                jsonrpc: '2.0',
                method: 'initialize',
            };
            session.setMessage(message);
            session.setCallback(() => {
                this.isInitialized = true;
                resolve();
            });
            this.requestSessions.push(session);
            this.langserverChannel.sendMessage(message);
        });
        
    }

    /**
     * Send the workspace symbol request
     * @param {string} query - query parameter for symbol
     * @param {function} callback Callback method for handling the response
     */
    workspaceSymbolRequest(query, callback) {
        if (!this.isInitialized) {
            this.once('langserver-initialized', () => this.workspaceSymbolRequest(query, callback));
            return;
        }
        const session = new RequestSession();
        const message = {
            id: session.getId(),
            jsonrpc: '2.0',
            method: 'workspace/symbol',
            params: {
                query,
            },
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

    /**
     * Document did open request notification processor
     * @param {object} options - document did open options
     */
    documentDidOpenNotification(options) {
        if (!this.isInitialized) {
            this.once('langserver-initialized', () => this.documentDidOpenNotification(options));
            return;
        }
        const message = {
            jsonrpc: '2.0',
            method: 'textDocument/didOpen',
            params: {
                textDocument: options.textDocument,
            },
        };

        this.langserverChannel.sendMessage(message);
    }

    /**
     * Document did close request notification processor
     * @param {object} options - document did close options
     */
    documentDidCloseNotification(options) {
        if (!this.isInitialized) {
            this.once('langserver-initialized', () => this.documentDidCloseNotification(options));
            return;
        }
        const message = {
            jsonrpc: '2.0',
            method: 'textDocument/didClose',
            params: {
                textDocument: options.textDocument,
            },
        };

        this.langserverChannel.sendMessage(message);
    }

    /**
     * Document did save request notification processor
     * @param {object} options - document did save options
     */
    documentDidSaveNotification(options) {
        if (!this.isInitialized) {
            this.once('langserver-initialized', () => this.documentDidSaveNotification(options));
            return;
        }
        const message = {
            jsonrpc: '2.0',
            method: 'textDocument/didSave',
            params: options.didSaveParams,
        };

        this.langserverChannel.sendMessage(message);
    }

    /**
     * Get completions request processor
     * @param {object} options - get completions' options
     * @param {function} callback - callback function to set the completions in the editor
     */
    getCompletions(options, callback) {
        if (!this.isInitialized) {
            this.once('langserver-initialized', () => this.getCompletions(options, callback));
            return;
        }
        const session = new RequestSession();
        const message = {
            id: session.getId(),
            jsonrpc: '2.0',
            method: 'textDocument/completion',
            params: {
                text: options.textDocument,
                position: options.position,
            },
        };

        session.setMessage(message);
        session.setCallback((responseMsg) => {
            callback(responseMsg);
        });
        this.requestSessions.push(session);
        this.langserverChannel.sendMessage(message);
    }

    // End language server notifications

    /**
     * Process the language server message
     * @param {object} message - response message
     */
    processMessage(message) {
        const session = _.find(this.requestSessions, (requestSession) => {
            return requestSession.getId() === message.id;
        });
        session.executeCallback(message);
    }


    /**
     * Check whether the language server is initialized
     * @return {boolean} true|false - whether server is initialized or not
     */
    initialized() {
        return this.isInitialized;
    }

    // End language server response handlers
}

/**
 * Method to initialize the singleton of lang server client
 */
function initLangServerClientInstance() {
    instance = new LangServerClientController();
    return new Promise((resolve, reject) => {
        instance.init()
            .then(resolve)
            .catch((error) => {
                instance = undefined;
                reject(error);
            })
    });
}

/**
 * method to fetch langserver client sigleton
 */
export function getLangServerClientInstance() {
    return new Promise((resolve, reject) => {
        if (instance !== undefined) {
            resolve(instance);
        } else {
            initLangServerClientInstance()
                .then(() => { resolve(instance) })
                .catch(reject);
        }
    })
}
