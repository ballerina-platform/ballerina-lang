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
/* eslint-env es6 */

import { fetchConfigs, parseContent } from 'api-client/api-client';
import { expect } from 'chai';
import path from 'path';
import fs from 'fs';
import { getLangServerClientInstance } from './../../../langserver/lang-server-client-controller';
import { testCompletions } from './LanguageServerTestBase';

const directory = process.env.DIRECTORY ? process.env.DIRECTORY : '';

describe('Ballerina Composer Test Suite', () => {

    describe("Language server Tests", () => {

        // fetch configs before proceeding
        /* global before*/
        before((beforeAllDone) => {
            fetchConfigs()
                .then(() => beforeAllDone())
                .catch(beforeAllDone);
        });

        it("Global level completions", function(done){
            this.timeout(0);
            const testFilePath = path.join(directory, 'js', 'tests', 'resources', 'languageServer');
            const testFile = 'echoService.js';
            const expectedFile = path.resolve(path.join(directory, 'js', 'tests', 'resources', 'languageServer', 'expected', 'echoService_case1.js'));
            const cursorPosition = { row: 0, column: 0 };
            testCompletions(cursorPosition, testFilePath, testFile, expectedFile, done);
        });

        it("Service level completions", function(done){
            this.timeout(0);
            const testFilePath = path.join(directory, 'js', 'tests', 'resources', 'languageServer');
            const testFile = 'echoService.js';
            const expectedFile = path.resolve(path.join(directory, 'js', 'tests', 'resources', 'languageServer', 'expected', 'echoService_case2.js'));
            const cursorPosition = { row: 6, column: 4 };
            testCompletions(cursorPosition, testFilePath, testFile, expectedFile, done);
        });

        it("Resource level completions", function (done) {
            this.timeout(0);
            const testFilePath = path.join(directory, 'js', 'tests', 'resources', 'languageServer');
            const testFile = 'echoService.js';
            const expectedFile = path.resolve(path.join(directory, 'js', 'tests', 'resources', 'languageServer', 'expected', 'echoService_case3.js'));
            const cursorPosition = { row: 10, column: 0 };
            testCompletions(cursorPosition, testFilePath, testFile, expectedFile, done);
        });

    });

});

