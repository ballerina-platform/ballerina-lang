/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

const fs = require('fs');
const path = require('path');
const { expect } = require('chai');
const { spawn } = require('child_process');

global.targetPath = path.join(__dirname, '..', '..', '..', 'target');

const testEnv = require('./environment');
const {findBalFilesInDirSync, parse} = require('./utils');
const debugPrint = require('./source-gen-debug-print');
const config = require('../resources/config');

const renderingSkip = config.skip["rendering"];
const sourceGenSkip = config.skip["source-gen"];

const testFilesDir = path.join(__dirname, '../resources/ballerina-examples');

describe('Ballerina Composer Test Suite', () => {
    let testFiles = [];
    if (fs.existsSync(testFilesDir)) {
        testFiles = findBalFilesInDirSync(testFilesDir);
    }

    let backEndProcess;

    before(function (beforeAllDone) {
        this.timeout(10000);
        const targetPath = path.join(global.targetPath, 'lib', `composer-server.jar`);
        backEndProcess = spawn('java', [`-Dballerina.home=${global.targetPath}`, '-jar', targetPath]);
        backEndProcess.stderr.pipe(process.stderr);
        let stdIndata = '';
        backEndProcess.stdout.on('data', (data) => {
            stdIndata += data;
            if (stdIndata.indexOf("Composer started successfully") < 0) {
                return;
            }
            stdIndata = '';
            console.log(stdIndata);
            testEnv.initialize();
            beforeAllDone();
        });
    });

    testFiles.forEach(testFile => {
        describe(path.basename(testFile), () => {
            let model;
            let content;

            before(done => {
                model = undefined;
                content = undefined;

                if (sourceGenSkip.includes(path.basename(testFile)) &&
                    renderingSkip.includes(path.basename(testFile))) {
                    done();
                } else {
                    fs.readFile(testFile, 'utf8', (err, fileContent) => {
                        parse(fileContent, testFile, parsedModel => {
                            content = fileContent;
                            model = parsedModel;
                            let error;

                            if (!model) {
                                error = new Error('Could not parse!');
                            }
                            done(error);
                        });
                    })
                }
            });

            it('renders', function () {
                if (renderingSkip.includes(path.basename(testFile))) {
                    this.skip();
                    return;
                }

                testEnv.render(model)
            });

            it('generates source', function () {
                if (sourceGenSkip.includes(path.basename(testFile))) {
                    this.skip();
                    return;
                }

                const generatedSource = testEnv.generateSource(model);
                expect(generatedSource).to.equal(content);
            });

            if (process.env.DEBUG === "true") {
                it('debug print', () => {
                    const tree = testEnv.buildTree(model);
                    debugPrint(tree);
                });
            }
        });
    });

    after(() => {
        backEndProcess.kill();
    });
});
