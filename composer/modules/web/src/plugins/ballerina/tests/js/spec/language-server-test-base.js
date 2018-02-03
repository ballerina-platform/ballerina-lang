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

import log from 'log';
import path from 'path';
import fs from 'fs';
import SourceViewCompleterFactory from 'plugins/ballerina/utils/source-view-completer-factory';

/**
 * Read the given file and return the content
 * @param {*} filePath
 */
const readFile = (filePath) => {
    return fs.readFileSync(filePath, 'utf8');
};

/**
 * web socket close event handler.
 * @param {Object} event
 */
const wsCloseEventHandler = (event) => {
    // no need to do anything specially as this is a close event handler for a test case.
};

/**
 * Test completions
 * @param {object} cursorPosition
 * @param {object} testFilePath
 * @param {string} testFileName
 * @param {function} done
 */
export function testCompletions(cursorPosition, directory, testFileName, expectedFile, done, getCompareCallback) {
    const testFilePath = path.join(directory, 'src', 'plugins', 'ballerina', 'tests', 'resources', 'languageServer');
    const expectedFilePath = path.resolve(path.join(directory, 'src', 'plugins', 'ballerina', 'tests', 'resources',
                                                                        'languageServer', 'expected', expectedFile));
    const expectedFileContent = fs.readFileSync(expectedFilePath, 'utf8');
    const compareCallback = getCompareCallback(expectedFileContent, done);
    const testFile = path.resolve(path.join(testFilePath, testFileName));
    const testFileContent = readFile(testFile);
    const fileData = { fileName: testFileName, filePath: testFilePath, packageName: '.' };
    const sourceViewCompleterFactory = new SourceViewCompleterFactory();

    const opts = {
        wsCloseEventHandler,
    };
    getLangServerClientInstance(opts)
        .then((langserverClient) => {
            sourceViewCompleterFactory.getCompletions(cursorPosition, testFileContent, fileData, langserverClient,
                                                                                                    compareCallback);
        })
        .catch(error => log.error(error));
}

/**
 * Invoke the close function of language server client, eventually it will close the web socket connection.
 */
export function close(callback) {
    return getLangServerClientInstance()
        .then((langserverClient) => {
            langserverClient.close();
            callback();
        });
}

