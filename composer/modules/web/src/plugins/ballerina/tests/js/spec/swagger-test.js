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

import path from 'path';
import fs from 'fs';
import { expect } from 'chai';
import { fetchConfigs, parseContent } from 'api-client/api-client';
import SwaggerImportPlugin from 'plugins/import-swagger/plugin';
import SwaggerParser from 'plugins/ballerina/swagger-parser/swagger-parser';
import TreeBuilder from 'plugins/ballerina/model/tree-builder';

/* global describe */
/* global it */

const directory = process.env.DIRECTORY ? process.env.DIRECTORY : '';

/**
 * Converts a ballerina code to b-lang-json-model.
 *
 * @param {string} fileContent The ballerina file content.
 * @returns {Object} A json model.
 */
function ballerinaASTDeserializer(fileContent) {
    return new Promise((resolve, reject) => {
        parseContent(fileContent)
            .then((parsedJson) => {
                resolve(parsedJson);
            })
            .catch(reject);
    });
}

/**
 * Executes tests related to swagger parser.
 */
describe('Ballerina Composer Test Suite', () => {
    describe('Swagger to Ballerina Tests', () => {
        // fetch configs before proceeding
        /* global before */
        before((beforeAllDone) => {
            fetchConfigs()
                .then(() => beforeAllDone())
                .catch(beforeAllDone);
        });

        const swaggerParserTestDir = path.resolve(path.join(directory, 'src', 'plugins', 'ballerina', 'tests',
                                                                                        'resources', 'swagger-parser'));
        const swaggerDirectory = path.resolve(path.join(swaggerParserTestDir, 'swagger-jsons'));
        const expectedBalDir = path.resolve(path.join(swaggerParserTestDir, 'expected-ballerina-source'));

        fs.readdirSync(swaggerDirectory).forEach((file) => {
            it(`${file} - swagger v2.0.0 import test`, (done) => {
                const jsonFile = path.resolve(path.join(swaggerDirectory, file));
                const jsonAsString = fs.readFileSync(jsonFile, 'utf8');
                const swaggerJson = JSON.parse(jsonAsString);
                const swaggerParser = new SwaggerParser(swaggerJson);
                const { serviceNode, rootNode } = new SwaggerImportPlugin().getNewAstSkeleton();

                swaggerParser.mergeToService(serviceNode);

                const balFileName = `${path.basename(jsonFile, '.json')}.bal`;
                const balFile = path.resolve(path.join(expectedBalDir, balFileName));
                const balContent = fs.readFileSync(balFile, 'utf8');

                ballerinaASTDeserializer(rootNode.getSource())
                    .then((actualGeneratedSource) => {
                        ballerinaASTDeserializer(balContent)
                            .then((expectedGeneratedSource) => {
                                const expectedAST = TreeBuilder.build(expectedGeneratedSource);
                                const actualAST = TreeBuilder.build(actualGeneratedSource);
                                expect(expectedAST.model.getSource(true)).to.equal(actualAST.model.getSource(true));
                                done();
                            }).catch((error) => {
                                done(error);
                            });
                    }).catch((error) => {
                        done(error);
                    });
            }).timeout(6000);
        });
    });
});
