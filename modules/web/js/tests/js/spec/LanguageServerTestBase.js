/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the 'License'); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import { fetchConfigs, parseContent } from 'api-client/api-client';
import { expect } from 'chai';
import path from 'path';
import fs from 'fs';
import { getLangServerClientInstance } from './../../../langserver/lang-server-client-controller';
import SourceViewCompleterFactory from 'ballerina/utils/source-view-completer-factory';

/**
 * Test completions
 * @param {object} cursorPosition 
 * @param {object} testFilePath 
 * @param {string} testFileName 
 * @param {object} expectedFile 
 * @param {function} done 
 */

export function testCompletions(cursorPosition, testFilePath, testFileName, expectedFile, done) {
    const completions = [];
    const testFile = path.resolve(path.join(testFilePath, testFileName));
    const expectedFileContent = readFile(expectedFile);
    const testFileContent = readFile(testFile);
    const fileData = { "fileName": testFileName, "filePath": testFilePath, "packageName": '.' };
    const sourceViewCompleterFactory = new SourceViewCompleterFactory();


    // callback function to validate generated completions.
    const test = function (x, completions) {
        expect(expectedFileContent).to.equal(JSON.stringify(completions));
        done();
    }

    let opts = {
        wsCloseEventHandler: wsCloseEventHandler
    }
    getLangServerClientInstance(opts)
        .then((langserverClient) => {
            sourceViewCompleterFactory.getCompletions(cursorPosition, testFileContent, fileData, langserverClient, test);
        })
        .catch(error => log.error(error));
}

/**
 * Invoke the close function of language server client, eventually it will close the web socket connection.
 */
export function close() {
    return getLangServerClientInstance()
        .then((langserverClient) => {
            langserverClient.close();
        })
}

/**
 * Read the given file and return the content
 * @param {*} filePath 
 */
function readFile(filePath) {
    return fs.readFileSync(filePath, 'utf8');
}

/**
 * web socket close event handler.
 * @param {Object} event 
 */
function wsCloseEventHandler(event) {
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
    clearInterval(this.ping);
}

