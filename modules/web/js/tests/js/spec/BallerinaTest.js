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

import Backend from 'ballerina/views/backend';
import BallerinaASTDeserializer from 'ballerina/ast/ballerina-ast-deserializer';
import BallerinaASTRootVisitor from 'ballerina/visitors/source-gen/ballerina-ast-root-visitor';
import log from 'log';
import fs from 'fs';
import { expect } from 'chai';
import path from 'path';

const getModelBackend = 'http://localhost:8289/ballerina/model/content';

// Ballerina AST Deserializer
function ballerinaASTDeserializer(fileContent) {
    const backend = new Backend({ url: getModelBackend });
    const response = backend.parse({ name: 'test.bal', path: '/temp', content: fileContent, package: 'test' });
    const ASTModel = BallerinaASTDeserializer.getASTModel(response);
    const sourceGenVisitor = new BallerinaASTRootVisitor();
    ASTModel.accept(sourceGenVisitor);
    const source = sourceGenVisitor.getGeneratedSource();
    return source;
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

describe('Ballerina Composer Test Suite', () => {
    const testResDir = path.resolve(path.join('js', 'tests', 'resources'));
    const testFiles = findBalFilesInDirSync(testResDir);
    testFiles.forEach((testFile) => {
        it(`${testFile.replace(testResDir, '')} file serialize/deserialize test`, () => {
            const expectedSource = readFile(testFile);
            const generatedSource = ballerinaASTDeserializer(expectedSource);
            if (generatedSource !== expectedSource) {
                log.error('error');
            }
            expect(generatedSource).to.equal(expectedSource);
        });
    });
});
