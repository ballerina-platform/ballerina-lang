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
import { testCompletions, close } from './language-server-test-base';

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

        after((done) => {
            close(done);
        });

        // General tests

        it("Global level completions", function (done) {
            this.timeout(10000);
            const testFilePath = path.join(directory, 'js', 'tests', 'resources', 'languageServer');
            const testFile = 'echoService.bal';
            const expectedFile = path.resolve(path.join(directory, 'js', 'tests', 'resources', 'languageServer', 'expected', 'echoService_case1.js'));
            const expectedFileContent = fs.readFileSync(expectedFile, 'utf8');
            const compareCallback = compareWithOrderCallback(expectedFileContent, done);
            const cursorPosition = { row: 0, column: 0 };
            testCompletions(cursorPosition, testFilePath, testFile, done, compareCallback);
        });

        it("Service level completions", function (done) {
            this.timeout(10000);
            const testFilePath = path.join(directory, 'js', 'tests', 'resources', 'languageServer');
            const testFile = 'echoService.bal';
            const expectedFile = path.resolve(path.join(directory, 'js', 'tests', 'resources', 'languageServer', 'expected', 'echoService_case2.js'));
            const expectedFileContent = fs.readFileSync(expectedFile, 'utf8');
            const compareCallback = compareWithOrderCallback(expectedFileContent, done);
            const cursorPosition = { row: 6, column: 4 };
            testCompletions(cursorPosition, testFilePath, testFile, done, compareCallback);
        });

        it("Resource level completions", function (done) {
            this.timeout(10000);
            const testFilePath = path.join(directory, 'js', 'tests', 'resources', 'languageServer');
            const testFile = 'echoService.bal';
            const expectedFile = path.resolve(path.join(directory, 'js', 'tests', 'resources', 'languageServer', 'expected', 'echoService_case3.js'));
            const expectedFileContent = fs.readFileSync(expectedFile, 'utf8');
            const compareCallback = compareWithoutOrderCallback(expectedFileContent, done);
            const cursorPosition = { row: 10, column: 0 };
            testCompletions(cursorPosition, testFilePath, testFile, done, compareCallback);
        });

        // Import tests

        it("Import level-0 completions", function (done) {
            this.timeout(10000);
            const testFilePath = path.join(directory, 'js', 'tests', 'resources', 'languageServer');
            const testFile = 'import.bal';
            const expectedFile = path.resolve(path.join(directory, 'js', 'tests', 'resources', 'languageServer', 'expected', 'import-case1.js'));
            const expectedFileContent = fs.readFileSync(expectedFile, 'utf8');
            const compareCallback = compareWithOrderCallback(expectedFileContent, done);
            const cursorPosition = { row: 0, column: 7 };
            testCompletions(cursorPosition, testFilePath, testFile, done, compareCallback);
        });

        it("Import level-1 completions", function (done) {
            this.timeout(10000);
            const testFilePath = path.join(directory, 'js', 'tests', 'resources', 'languageServer');
            const testFile = 'import.bal';
            const expectedFile = path.resolve(path.join(directory, 'js', 'tests', 'resources', 'languageServer', 'expected', 'import-case2.js'));
            const expectedFileContent = fs.readFileSync(expectedFile, 'utf8');
            const compareCallback = compareWithOrderCallback(expectedFileContent, done);
            const cursorPosition = { row: 1, column: 17 };
            testCompletions(cursorPosition, testFilePath, testFile, done, compareCallback);
        });

        it("Import level-2 completions", function (done) {
            this.timeout(10000);
            const testFilePath = path.join(directory, 'js', 'tests', 'resources', 'languageServer');
            const testFile = 'import.bal';
            const expectedFile = path.resolve(path.join(directory, 'js', 'tests', 'resources', 'languageServer', 'expected', 'import-case3.js'));
            const expectedFileContent = fs.readFileSync(expectedFile, 'utf8');
            const compareCallback = compareWithOrderCallback(expectedFileContent, done);
            const cursorPosition = { row: 2, column: 22 };
            testCompletions(cursorPosition, testFilePath, testFile, done, compareCallback);
        });

        it("Sql connector completions", function (done) {
            this.timeout(10000);
            const testFilePath = path.join(directory, 'js', 'tests', 'resources', 'languageServer');
            const testFile = 'sqlConnector.bal';
            const expectedFile = path.resolve(path.join(directory, 'js', 'tests', 'resources', 'languageServer', 'expected', 'sql-connector-case1.js'));
            const expectedFileContent = fs.readFileSync(expectedFile, 'utf8');
            const compareCallback = compareWithOrderCallback(expectedFileContent, done);
            const cursorPosition = { row: 20, column: 14 };
            testCompletions(cursorPosition, testFilePath, testFile, done, compareCallback);
        });

        // returns a callback function to validate generated completions. Order of elements of two arrays can be different.
        function compareWithoutOrderCallback(expectedFileContent, done) {
            return function (x, completions) {
                function comparator(a, b) {
                    return (a.caption === b.caption) && (a.snippet === b.snippet) && (a.meta === b.meta);
                }
                const expectedJSON= JSON.parse(expectedFileContent);
                let intersection = _.intersectionWith(completions, expectedJSON, comparator);

                if (!((completions.length === expectedJSON.length) && (completions.length === intersection.length))) {
                    throw new Error("Fail - Incompatible content. \nExpect + Actual -\n" + "+  " + expectedFileContent + "\n -  " + JSON.stringify(completions));
                }
                done();
            }
        }

        // returns a callback function to validate generated completions. Order of elements of two arrays should be same.
        function compareWithOrderCallback(expectedFileContent, done) {
            return function (x, completions) {
                expect(JSON.parse(expectedFileContent)).to.deep.equal(completions);
                done();
            }
        }

    });

});

