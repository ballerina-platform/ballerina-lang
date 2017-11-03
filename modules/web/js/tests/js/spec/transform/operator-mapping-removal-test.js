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

import { fetchConfigs } from 'api-client/api-client';
import { expect } from 'chai';
import TransformManager from '../../../../ballerina/diagram2/views/default/components/transform/transform-node-manager';
import TransformTestUtils from './transform-test-utils';
import environment from '../../../../ballerina/env/environment';

let transformManager;

/* global describe */
/* global it */
describe('Transform Operator Mapping Removal', () => {
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
        const testSource = TransformTestUtils.readSource(testDir, 'direct-with-primitive-vars');
        const expectedSource = TransformTestUtils.readSource(testDir, 'direct-with-primitive-vars-expected');
        TransformTestUtils.getTree(testSource)
            .then((tree) => {
                const transformStmt = TransformTestUtils.getTransformStmt(tree, 2);
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

});
