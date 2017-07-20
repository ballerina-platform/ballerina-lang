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
import BallerinaASTDeserializer from 'ballerina/ast/ballerina-ast-deserializer';
import BallerinaASTRootVisitor from 'ballerina/visitors/source-gen/ballerina-ast-root-visitor';
import fs from 'fs';
import { expect } from 'chai';
import path from 'path';

// Ballerina AST Deserializer
function ballerinaASTDeserializer(fileContent) {
    return new Promise((resolve, reject) => {
        parseContent(fileContent)
            .then((parsedJson) => {
                const ASTModel = BallerinaASTDeserializer.getASTModel(parsedJson);
                const sourceGenVisitor = new BallerinaASTRootVisitor();
                ASTModel.accept(sourceGenVisitor);
                resolve(sourceGenVisitor.getGeneratedSource());
            })
            .catch(reject);
    });
}

function readFile(filePath) {
    return fs.readFileSync(filePath, 'utf8');
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

/* global describe*/
// Reference : https://stackoverflow.com/a/39158329/1403246
describe('Ballerina Composer Test Suite', () => {
    // fetch configs before proceeding
    /* global before*/
    before((beforeAllDone) => {
        fetchConfigs()
            .then(() => beforeAllDone())
            .catch(beforeAllDone);
    });
    const testResDir = path.resolve(path.join('js', 'tests', 'resources'));
    const testFiles = findBalFilesInDirSync(testResDir);
    testFiles.forEach((testFile) => {
        /* global it */
        it(`${testFile.replace(testResDir, '')} file serialize/deserialize test`, (testCaseDone) => {
            const expectedSource = readFile(testFile);
            ballerinaASTDeserializer(expectedSource)
                .then((generatedSource) => {
                    expect(generatedSource).to.equal(expectedSource);
                    testCaseDone();
                })
                .catch(testCaseDone);
        });
    });
});

