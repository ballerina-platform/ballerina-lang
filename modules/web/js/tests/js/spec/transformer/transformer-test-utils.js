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
import fs from 'fs';
import path from 'path';
import { parseContent } from 'api-client/api-client';
import TreeBuilder from '../../../../ballerina/model/tree-builder';

const directory = process.env.DIRECTORY ? process.env.DIRECTORY : '';
const transformerBalDir = path.join(directory, 'js', 'tests', 'resources', 'transformer');

/**
 * Transform utils for tests
 * @class TransformTestUtils
 */
class TransformerTestUtils {

    /**
     * Converts a ballerina source to JSON model
     *
     * @param {string} fileContent The ballerina file content.
     * @returns {Object} A JSON model.
     */
    static getTree(fileContent) {
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
    static getTransformer(tree, index) {
        return tree.topLevelNodes[index];
    }

    /**
     * Read ballerina source
     * @param {any} testDir test directory
     * @param {any} balFile bal file
     * @returns ballerina source
     */
    static readSource(testDir, balFile) {
        const file = path.resolve(transformerBalDir, testDir, balFile + '.bal');
        return fs.readFileSync(file, 'utf8');
    }
}
export default TransformerTestUtils;
