'use strict';

import React from 'react';
import fs from 'fs';
import path from 'path';
import renderer from 'react-test-renderer';
import {fetchConfigs, parseContent} from 'api-client/api-client';
import BallerinaDiagram from './../../js/ballerina/components/diagram';
import BallerinaASTDeserializer from './../../js/ballerina/ast/ballerina-ast-deserializer';

let files = [];
const directory = process.env.DIRECTORY ? process.env.DIRECTORY : '';

function getFile(name) {

}

function loadFiles(dir) {
    const fileList = fs.readdirSync(dir);
    fileList.forEach((file) => {
        if (fs.statSync(path.join(dir, file)).isDirectory()) {
            files = loadFiles(path.join(dir, file), files);
        } else if (path.extname(file) === '.bal') {
            files.push(path.join(dir, file));
        }
    });
    return files;
}

function generatedDiagram(fileContent) {
    return new Promise((resolve, reject) => {
        console.log(fileContent);
        parseContent(fileContent)
            .then((parsedJson) => {
                const ASTModel = BallerinaASTDeserializer.getASTModel(parsedJson);
                const isTransformActive = true;
                const tree = renderer
                    .create(
                        <BallerinaDiagram
                            model={ASTModel}
                        />
                    )
                    .toJSON();
                resolve(tree);
            })
            .catch(reject);
    });
}

function readFile(filePath) {
    return fs.readFileSync(filePath, 'utf8');
}

describe('Source Generation Test Suite', function () {
    // beforeEach((beforeAllDone) => {
    //
    //         .then(() => beforeAllDone())
    //         .catch(beforeAllDone);
    // });
    fetchConfigs();
    const testResDir = path.resolve(path.join(directory, 'js', 'tests', 'resources'));
    loadFiles(testResDir);

    it('renders correctly', () => {
        const fileContent = readFile(files[0]);
        generatedDiagram(fileContent)
            .then((tree) => {
                expect(tree).toMatchSnapshot();
            });
    });
});
