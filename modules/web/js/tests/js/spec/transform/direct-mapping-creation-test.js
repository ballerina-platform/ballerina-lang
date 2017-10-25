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

import fs from 'fs';
import path from 'path';
import { fetchConfigs, parseContent } from 'api-client/api-client';
import { expect } from 'chai';
import TreeBuilder from '../../../../ballerina/model/tree-builder';
import TransformManager from '../../../../ballerina/diagram2/views/default/components/transform/transform-node-manager';
import environment from '../../../../ballerina/env/environment';

const directory = process.env.DIRECTORY ? process.env.DIRECTORY : '';
const transformBalDir = path.join(directory, 'js', 'tests', 'resources', 'transform');
let transformManager;

/**
 * Converts a ballerina source to JSON model
 *
 * @param {string} fileContent The ballerina file content.
 * @returns {Object} A JSON model.
 */
function getTree(fileContent) {
    return new Promise((resolve, reject) => {
        parseContent(fileContent)
            .then((parsedJson) => {
                const tree = TreeBuilder.build(parsedJson.model);
                resolve(tree);
            })
            .catch(reject);
    });
}

/**
 * Get transform stmt from tree
 * @param {any} tree node tree
 * @param {any} index index of transform statement
 * @returns transform statement
 */
function getTransformStmt(tree, index) {
    return tree.topLevelNodes[0].body.statements[index];
}

/**
 * Read ballerina source
 * @param {any} testDir test directory
 * @param {any} balFile bal file
 * @returns ballerina source
 */
function readSource(testDir, balFile) {
    const file = path.resolve(transformBalDir, testDir, balFile + '.bal');
    return fs.readFileSync(file, 'utf8');
}

/* global describe */
/* global it */
describe('Transform Direct Mapping Creation', () => {
    // fetch configs before proceeding
    /* global before */
    before((done) => {
        fetchConfigs()
            .then(() => {
                return environment.initialize();
            })
            .then(() => {
                transformManager = new TransformManager({
                    environment,
                    typeLattice: environment.getTypeLattice(),
                });
                done();
            }).catch(done);
    });

    const testDir = 'direct-mapping-creation';
    it('Direct mapping with primitive variables', (done) => {
        const testSource = readSource(testDir, 'direct-with-primitive-vars');
        const expectedSource = readSource(testDir, 'direct-with-primitive-vars-expected');
        getTree(testSource)
            .then((tree) => {
                const transformStmt = getTransformStmt(tree, 2);
                transformManager.setTransformStmt(transformStmt);
                const connection = {
                    source: {
                        name: 'b',
                        type: 'int',
                        endpointKind: 'input',
                    },
                    target: {
                        name: 'a',
                        type: 'int',
                        endpointKind: 'output',
                    },
                };
                transformManager.createStatementEdge(connection);
                expect(tree.getSource()).to.equal(expectedSource);
                done();
            }).catch((error) => {
                done(error);
            });
    });
    it('Direct mapping with function args', (done) => {
        const testSource = readSource(testDir, 'direct-with-args');
        const expectedSource = readSource(testDir, 'direct-with-args-expected');
        getTree(testSource)
            .then((tree) => {
                const transformStmt = getTransformStmt(tree, 0);
                transformManager.setTransformStmt(transformStmt);
                const connection = {
                    source: {
                        name: 'b',
                        type: 'int',
                        endpointKind: 'input',
                    },
                    target: {
                        name: 'a',
                        type: 'int',
                        endpointKind: 'output',
                    },
                };
                transformManager.createStatementEdge(connection);
                expect(tree.getSource()).to.equal(expectedSource);
                done();
            }).catch((error) => {
                done(error);
            });
    });
    it('Direct mapping with casting and conversion', (done) => {
        const testSource = readSource(testDir, 'direct-with-cast-conversion');
        const expectedSource = readSource(testDir, 'direct-with-cast-conversion-expected');
        getTree(testSource)
            .then((tree) => {
                const transformStmt = getTransformStmt(tree, 7);
                transformManager.setTransformStmt(transformStmt);
                let connection = {
                    source: {
                        name: 's1',
                        type: 'string',
                        endpointKind: 'input',
                    },
                    target: {
                        name: 'any1',
                        type: 'any',
                        endpointKind: 'output',
                    },
                };
                // create implicit cast : any to string
                transformManager.createStatementEdge(connection);

                connection = {
                    source: {
                        name: 'any2',
                        type: 'any',
                        endpointKind: 'input',
                    },
                    target: {
                        name: 'b2',
                        type: 'boolean',
                        endpointKind: 'output',
                    },
                };

                // create explicit unsafe cast : any to boolean
                transformManager.createStatementEdge(connection);

                connection = {
                    source: {
                        name: 'f2',
                        type: 'float',
                        endpointKind: 'input',
                    },
                    target: {
                        name: 'i1',
                        type: 'int',
                        endpointKind: 'output',
                    },
                };

                // create safe safe conversion : float to int
                transformManager.createStatementEdge(connection);

                connection = {
                    source: {
                        name: 's2',
                        type: 'string',
                        endpointKind: 'input',
                    },
                    target: {
                        name: 'f1',
                        type: 'float',
                        endpointKind: 'output',
                    },
                };

                // create safe unsafe conversion : string to float
                transformManager.createStatementEdge(connection);

                expect(tree.getSource()).to.equal(expectedSource);
                done();
            }).catch((error) => {
                done(error);
            });
    }).timeout(10000);
});
