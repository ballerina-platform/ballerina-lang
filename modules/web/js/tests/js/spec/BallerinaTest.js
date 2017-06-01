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

var getModelBackend = 'http://localhost:8289/ballerina/model/content';

//Ballerina AST Deserializer
function ballerinaASTDeserializer(fileContent){
    var backend = new Backend({'url' : getModelBackend});
    var response = backend.parse(fileContent);
    var ASTModel = BallerinaASTDeserializer.getASTModel(response);
    var sourceGenVisitor = new BallerinaASTRootVisitor();
    ASTModel.accept(sourceGenVisitor);
    var source = sourceGenVisitor.getGeneratedSource();
    return source;
}

function readFile(filePath){
    return fs.readFileSync(filePath, 'utf8');
}

// List all files in a directory in Node.js recursively in a synchronous fashion
function findBalFilesInDirSync(dir, filelist) {
    let files = fs.readdirSync(dir);
    filelist = filelist || [];
    files.forEach(function(file) {
        if (fs.statSync(path.join(dir, file)).isDirectory()) {
            filelist = findBalFilesInDirSync(path.join(dir, file), filelist);
        }
        else if (path.extname(file) === '.bal') {
            filelist.push(path.join(dir, file));
        }
    });
    return filelist;
}

describe ('Ballerina Composer Test Suite', function() {
    let testResDir = path.resolve(path.join('js', 'tests', 'resources'));
    let testFiles = findBalFilesInDirSync(testResDir);
    testFiles.forEach(function(testFile) {
        it (testFile.replace(/^.*[\\\/]/, '') + ' file serialize/deserialize test', function() {
            var expectedSource = readFile(testFile);
            var generatedSource = ballerinaASTDeserializer(expectedSource);
            if (generatedSource !== expectedSource) {
                log.error('error');
            }
            expect(generatedSource).to.equal(expectedSource);
        });
    });
});
