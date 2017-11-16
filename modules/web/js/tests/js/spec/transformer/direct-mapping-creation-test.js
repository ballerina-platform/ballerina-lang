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
const transformBalDir = path.join(directory, 'js', 'tests', 'resources', 'transformer');
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
 * Get transformer from tree
 * @param {any} tree node tree
 * @param {any} index index of transformer
 * @returns transformer
 */
function getTransformer(tree, index) {
    return tree.topLevelNodes[index];
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
                const transformer = getTransformer(tree, 0);
                transformManager.setTransformStmt(transformer);
                const connection = {
                    source: {
                        name: 's1',
                        type: 'string',
                        endpointKind: 'input',
                    },
                    target: {
                        name: 's2',
                        type: 'string',
                        endpointKind: 'output',
                    },
                };
                transformManager.createStatementEdge(connection);
                expect(tree.getSource()).to.equal(expectedSource);
                done();
            }).catch((error) => {
                done(error);
            });
    }).timeout(5000);

    it('Direct mapping with casting and conversion', (done) => {
        const testSource = readSource(testDir, 'direct-with-cast-conversion');
        const expectedSource = readSource(testDir, 'direct-with-cast-conversion-expected');
        getTree(testSource)
            .then((tree) => {
                // Create implicit cast : any -> string
                const implitTransformer = getTransformer(tree, 0);
                transformManager.setTransformStmt(implitTransformer);
                let connection = {
                    source: {
                        name: 's',
                        type: 'string',
                        endpointKind: 'input',
                    },
                    target: {
                        name: 'a',
                        type: 'any',
                        endpointKind: 'output',
                    },
                };
                transformManager.createStatementEdge(connection);

                // Create unsafe cast : any -> boolean
                const unsafeCastTransformer = getTransformer(tree, 1);
                transformManager.setTransformStmt(unsafeCastTransformer);
                connection = {
                    source: {
                        name: 'a',
                        type: 'any',
                        endpointKind: 'input',
                    },
                    target: {
                        name: 'b',
                        type: 'boolean',
                        endpointKind: 'output',
                    },
                };
                transformManager.createStatementEdge(connection);

                // Create safe conversion : float -> int
                const safeConversionTransformer = getTransformer(tree, 2);
                transformManager.setTransformStmt(safeConversionTransformer);
                connection = {
                    source: {
                        name: 'f',
                        type: 'float',
                        endpointKind: 'input',
                    },
                    target: {
                        name: 'i',
                        type: 'int',
                        endpointKind: 'output',
                    },
                };
                transformManager.createStatementEdge(connection);

                // Create safe unsafe conversion : string -> float
                const unsafeConversionTransformer = getTransformer(tree, 3);
                transformManager.setTransformStmt(unsafeConversionTransformer);
                connection = {
                    source: {
                        name: 's',
                        type: 'string',
                        endpointKind: 'input',
                    },
                    target: {
                        name: 'f',
                        type: 'float',
                        endpointKind: 'output',
                    },
                };
                transformManager.createStatementEdge(connection);

                expect(tree.getSource()).to.equal(expectedSource);
                done();
            }).catch((error) => {
                done(error);
            });
    }).timeout(13000);
});
