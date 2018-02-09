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
import { getServiceEndpoint } from 'api-client/api-client';
import LangserverChannel from './langserver-channel';
import RequestSession from './request-session';

const BUILT_IN_PACKAGES = 'ballerina/packages';

// holds the singleton of lang server client
let instance;

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
        this.options = options;
    }

    /**
     * Initialize LangServerClientController
     */
    init() {
        return new Promise((resolve, reject) => {
            this.langserverChannel = new LangserverChannel({
                /** fetch endpoint from util */
                endpoint: getServiceEndpoint('ballerina-langserver'),
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
            this.langserverChannel.on('close', () => {
                // call all pending requests
                this.requestSessions.forEach((session) => {
                    session.executeCallback();
                });
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
                params: {
                    capabilities: {
                        textDocument: {
                            completion: {
                                completionItem: {
                                    snippetSupport: true,
                                },
                            },
                        },
                    },
                },
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
                textDocument: {
                    uri: options.file.toURI(),
                    text: options.text,
                },
            },
        };

        this.langserverChannel.sendMessage(message);
    }

    /**
     * Document did change request notification processor
     * @param {object} options - document did change options
     */
    documentDidChangeNotification(options) {
        if (!this.isInitialized) {
            this.once('langserver-initialized', () => this.documentDidChangeNotification(options));
            return;
        }
        const message = {
            jsonrpc: '2.0',
            method: 'textDocument/didChange',
            params: {
                textDocument: {
                    uri: options.file.toURI(),
                },
                contentChanges: [{
                    text: options.text,
                }],
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
            params: {
                textDocument: {
                    uri: options.file.toURI(),
                },
                text: options.text,
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
                textDocument: {
                    uri: options.file.toURI(),
                },
            },
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
                fileName: options.fileName,
                textDocument: {
                    uri: options.fullPath,
                },
                uri: options.fullPath,
                packageName: options.packageName,
            },
        };

        return new Promise((resolve) => {
            session.setMessage(message);
            session.setCallback((responseMsg) => {
                callback ? callback(responseMsg) : resolve(responseMsg);
            });
            this.requestSessions.push(session);
            this.langserverChannel.sendMessage(message);
        });
    }

    /**
     * Get built-in packages request processor
     */
    getBuiltInPackages() {
        return new Promise((resolve, reject) => {
            const session = new RequestSession();
            const message = {
                id: session.getId(),
                jsonrpc: '2.0',
                method: BUILT_IN_PACKAGES,
                params: {},
            };
            session.setCallback((responseMsg) => {
                resolve(responseMsg);
            });
            session.setMessage(message);
            this.requestSessions.push(session);
            this.langserverChannel.on('error', (error) => {
                reject(error);
            });
            this.langserverChannel.sendMessage(message);
        });
    }

    /**
     * Get program packages request processor
     * @param {object} options - get program packages' options
     * @param {function} callback - callback function to set the program packages in the editor
     */
    getProgramPackages(options, callback) {
        const session = new RequestSession();
        const message = {
            id: session.getId(),
            jsonrpc: '2.0',
            method: 'programDirectory/packages',
            params: {
                text: options.textDocument,
                position: options.position,
                fileName: options.fileName,
                filePath: options.filePath,
                packageName: options.packageName,
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
        // ignore pong messages
        if (message.id === 'PONG') {
            return;
        }
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

    /**
     * Invoke close function of the langserver channel.
     */
    close() {
        this.langserverChannel.close();
    }

    // End language server response handlers
}

/**
 * Method to initialize the singleton of lang server client
 */
function initLangServerClientInstance(options) {
    instance = new LangServerClientController(options);
    return instance.init()
             .then(() => {
                 return instance;
             });
}

/**
 * method to fetch langserver client sigleton
 */
export function getLangServerClientInstance(options) {
    return instance !== undefined ? Promise.resolve(instance) : initLangServerClientInstance(options);
}
