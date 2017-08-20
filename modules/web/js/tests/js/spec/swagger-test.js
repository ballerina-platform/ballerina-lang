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
import { fetchConfigs, parseContent } from 'api-client/api-client';
import SourceGenVisitor from 'ballerina/visitors/source-gen/ballerina-ast-root-visitor';
import { expect } from 'chai';
import SwaggerImportDialog from 'dialog/swagger-import-dialog';
import SwaggerParser from '../../../swagger-parser/swagger-parser';

/* global describe*/
/* global it*/

const directory = process.env.DIRECTORY ? process.env.DIRECTORY : '';

/**
 * Get the source of a ballerina ast root using source-gen-visitor.
 *
 * @param {BallerinaASTRoot} ballerinaAstRoot The ballerina ast root.
 * @returns {string} - Ballerina source.
 */
function getSource(ballerinaAstRoot) {
    const sourceGenVisitor = new SourceGenVisitor();
    ballerinaAstRoot.accept(sourceGenVisitor);
    return sourceGenVisitor.getGeneratedSource();
}

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
        /* global before*/
        before((beforeAllDone) => {
            fetchConfigs()
                .then(() => beforeAllDone())
                .catch(beforeAllDone);
        });

        const swaggerParserTestDir = path.resolve(path.join(directory, 'js', 'tests', 'resources', 'swagger-parser'));
        const swaggerDirectory = path.resolve(path.join(swaggerParserTestDir, 'swagger-jsons'));
        const expectedBalDir = path.resolve(path.join(swaggerParserTestDir, 'expected-ballerina-source'));
        const swaggerImportDialog = new SwaggerImportDialog({
            config: {
                dialog: {
                    container: 'mock-container',
                },
            },
        });

        fs.readdirSync(swaggerDirectory).forEach((file) => {
            it(`${file} - swagger v2.0.0 import test`, (done) => {
                const jsonFile = path.resolve(path.join(swaggerDirectory, file));
                const jsonAsString = fs.readFileSync(jsonFile, 'utf8');
                const swaggerJson = JSON.parse(jsonAsString);
                const swaggerParser = new SwaggerParser(swaggerJson);
                const { ballerinaAstRoot, serviceDefinition } = swaggerImportDialog.getNewAstSkeleton();

                swaggerParser.mergeToService(serviceDefinition);

                const balFileName = `${path.basename(jsonFile, '.json')}.bal`;
                const balFile = path.resolve(path.join(expectedBalDir, balFileName));
                const balContent = fs.readFileSync(balFile, 'utf8');

                ballerinaASTDeserializer(getSource(ballerinaAstRoot))
                    .then((actualGeneratedSource) => {
                        ballerinaASTDeserializer(balContent)
                            .then((expectedGeneratedSource) => {
                                expect(expectedGeneratedSource).to.deep.equal(actualGeneratedSource);
                                done();
                            }).catch((error) => {
                                done(error);
                            });
                    }).catch((error) => {
                        done(error);
                    });
            });
        });
    });
});
