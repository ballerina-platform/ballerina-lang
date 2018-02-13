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

import { fetchConfigs } from '@ballerina-lang/composer-api-client';
import { expect } from 'chai';
import TransformerTestUtils from './transformer-test-utils';
import TransformManager from '../../../../diagram/views/default/components/transformer/transformer-node-manager';
import environment from '../../../../env/environment';

let transformManager;

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
        const { testSource, expectedSource } = TransformerTestUtils
                                                    .getTestResources(testDir, 'primitive-vars');
        TransformerTestUtils.getTree(testSource)
            .then((tree) => {
                const transformer = TransformerTestUtils.getTransformer(tree, 0);
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
        const { testSource, expectedSource } = TransformerTestUtils
                                                    .getTestResources(testDir, 'cast-conversion');
        TransformerTestUtils.getTree(testSource)
            .then((tree) => {
                // Create implicit cast : any -> string
                const implitTransformer = TransformerTestUtils.getTransformer(tree, 0);
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
                const unsafeCastTransformer = TransformerTestUtils.getTransformer(tree, 1);
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
                const safeConversionTransformer = TransformerTestUtils.getTransformer(tree, 2);
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
                const unsafeConversionTransformer = TransformerTestUtils.getTransformer(tree, 3);
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
    }).timeout(15000);

    it('Direct mapping with structs', (done) => {
        const { testSource, expectedSource } = TransformerTestUtils.getTestResources(testDir, 'structs');
        TransformerTestUtils.getTree(testSource)
            .then((tree) => {
                const transformer = TransformerTestUtils.getTransformer(tree, 2);
                transformManager.setTransformStmt(transformer);

                // Create mapping : p.age -> e.age
                let connection = {
                    source: {
                        name: 'p.age',
                        type: 'int',
                        endpointKind: 'input',
                    },
                    target: {
                        name: 'e.age',
                        type: 'int',
                        endpointKind: 'output',
                    },
                };
                transformManager.createStatementEdge(connection);

                // Create mapping : p.firstName -> e.name
                connection = {
                    source: {
                        name: 'p.firstName',
                        type: 'string',
                        endpointKind: 'input',
                    },
                    target: {
                        name: 'e.name',
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

    it('Direct mapping with structs with params', (done) => {
        const { testSource, expectedSource } = TransformerTestUtils.getTestResources(testDir, 'structs-and-params');
        TransformerTestUtils.getTree(testSource)
            .then((tree) => {
                const transformer = TransformerTestUtils.getTransformer(tree, 2);
                transformManager.setTransformStmt(transformer);

                // Create mapping : address -> e.address
                const connection = {
                    source: {
                        name: 'address',
                        type: 'string',
                        endpointKind: 'input',
                    },
                    target: {
                        name: 'e.address',
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
});
