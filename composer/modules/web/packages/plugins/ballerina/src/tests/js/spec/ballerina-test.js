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

// Reference : https://stackoverflow.com/a/39158329/1403246
/* global describe */
/* eslint-env es6 */
/* global it */
/* global after */
/* global before */

import { fetchConfigs, parseContent } from 'api-client/api-client';
import fs from 'fs';
import { expect } from 'chai';
import _ from 'lodash';
import path from 'path';
import chalk from 'chalk';
import TreeBuilder from 'plugins/ballerina/model/tree-builder';

const directory = process.env.DIRECTORY ? process.env.DIRECTORY : '';

function readFile(filePath) {
    return fs.readFileSync(filePath, 'utf8');
}

const subScriptChars = ['₀', '₁', '₂', '₃', '₄', '₅', '₆', '₇', '₈', '₉'];

const promisers = {};

function debugPrint(node, name = '', l = 1) {
    const wsSimbols = { '"': '', ' ': '\u2022', '\n': '\u21B5', '\t': '\u21E5' };
    const wsStr = (node.ws || []).map(ws => (ws.static ? chalk.gray : chalk.magenta)((ws.ws ?
            ws.ws.replace(/\s/g, c => (wsSimbols[c]) || c.codePointAt(0)) :
            '\u26AC') +
        JSON.stringify(ws.text).slice(1, -1) +
        String(ws.i).split('')
            .map(i => subScriptChars[Number(i)])
            .join('')))
        .join(' ');

    // eslint-disable-next-line
    const children = _.groupBy([...node], ([_, c]) => c && c.kind === 'Identifier');
    console.log(new Array(l).join(' '),
        chalk.green(name),
        chalk.blue(node.kind),
        node.entries().map(([key, val]) => {
            let valPritty = val;
            if (val === true) {
                valPritty = '\u2713';
            } else if (val === false) {
                return chalk.green.dim.strikethrough(key);
            }
            return chalk.green(key) + ':' + chalk.cyan(_.isObject(valPritty) ? JSON.stringify(valPritty) : valPritty);
        }).join(),
        (children.true || []).map(([key, val]) => {
            return val.value && chalk.green(key) + ':' + chalk.cyan.bold(val.value);
        }).join(),
        wsStr);
    for (const [childName, child] of children.false || []) {
        debugPrint(child, childName, l + 2);
    }
}

// Ballerina AST Deserializer
function ballerinaASTDeserializer(filePath, pretty, content) {
    let cached = promisers[filePath];
    if (!cached) {
        const fileContent = content || readFile(filePath);
        cached = new Promise((resolve, reject) => {
            parseContent(fileContent)
                .then((parsedJson) => {
                    if (!parsedJson.model) {
                        console.log(parsedJson);
                    }
                    const tree = parsedJson.model ? TreeBuilder.build(parsedJson.model) : null;
                    if (process.env.SOURCE_DEBUG === 'true') {
                        debugPrint(tree);
                    }
                    let source = '';
                    let err = null;
                    try {
                        source = tree ? tree.getSource(pretty) : '';
                    } catch (e) {
                        err = e;
                    }
                    resolve({ tree, fileContent, generatedSource: source, err, parsedJson });
                })
                .catch(reject);
        });
        promisers[filePath] = cached;
    }
    return cached;
}

// List all files in a directory in Node.js recursively in a synchronous fashion
function findBalFilesInDirSync(dir, filelist) {
    const files = fs.readdirSync(dir);
    filelist = filelist || [];
    files.forEach((file) => {
        if (fs.statSync(path.join(dir, file)).isDirectory()) {
            filelist = findBalFilesInDirSync(path.join(dir, file), filelist);
        } else if (path.extname(file) === '.bal') {
            filelist.push(path.join(dir, file));
        }
    });
    return filelist;
}

describe('Ballerina Composer Test Suite', () => {
    // fetch configs before proceeding
    before((beforeAllDone) => {
        fetchConfigs()
            .then(() => beforeAllDone())
            .catch(beforeAllDone);
    });
    const testResDir = path.resolve(path.join(directory, 'src', 'plugins', 'ballerina', 'tests', 'resources',
                                                                                                            'parser'));
    const testFiles = findBalFilesInDirSync(testResDir);
    _.sortBy(testFiles, f => fs.statSync(f).size).slice(0, 500).forEach((testFile) => {
        const relPath = path.relative('.', testFile);
        it(relPath + ' parse', (done) => {
            ballerinaASTDeserializer(testFile)
                .then(({ tree, err }) => {
                    if (!err && tree && tree.topLevelNodes) {
                        done();
                    } else {
                        done(err || new Error('Backend parser error'));
                    }
                })
                .catch((error) => {
                    done(error);
                });
        });
        it(relPath + ' file serialize/deserialize test', function (done) {
            ballerinaASTDeserializer(testFile)
                .then(({ generatedSource, fileContent, tree, err }) => {
                    if (err || !tree || !tree.topLevelNodes) {
                        this.skip();
                        return;
                    }
                    if (generatedSource !== '') {
                        if (process.env.IGNORE_WS === 'true') {
                            if (generatedSource.replace(/\s/g, '') !== fileContent.replace(/\s/g, '')) {
                                expect(generatedSource).to.equal(fileContent);
                            }
                        } else {
                            expect(generatedSource).to.equal(fileContent);
                        }
                    } else {
                        this.skip();
                    }
                    done();
                })
                .catch((error) => {
                    if (error.message === 'Network Error') {
                        this.skip();
                    } else {
                        done(error);
                    }
                });
        });

        it(relPath + ' default ws (formatting) test', function (done) {
            ballerinaASTDeserializer(testFile, true)
                .then(({ generatedSource, fileContent, tree, err }) => {
                    if (err || !tree.topLevelNodes ||
                        generatedSource.replace(/\s/g, '') !== fileContent.replace(/\s/g, '')) {
                        this.skip();
                        return;
                    }
                    const formattedPath = testFile.replace(/\bparser\b/, 'formatted');

                    if (generatedSource !== '' && fs.existsSync(formattedPath)) {
                        if (generatedSource.replace(/\s/g, '') !== fileContent.replace(/\s/g, '')) {
                            this.skip();
                        } else {
                            const formatted = readFile(formattedPath);
                            expect(generatedSource.replace(/^\s*\n/mg, '').replace(/\s?,\s?/g, ',')
                                .replace(/return ;/g, 'return;').trim())
                                .to.equal(formatted.replace(/^\s*\n/mg, '').replace(/\s?,\s?/g, ',').trim());
                        }
                    } else {
                        this.skip();
                    }
                    done();
                })
                .catch((error) => {
                    done(error);
                });
        });
    });

    after((done) => {
        done();
    });
});

