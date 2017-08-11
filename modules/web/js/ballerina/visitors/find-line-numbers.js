/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import ASTVisitor from './ast-visitor';

/**
 * Finds line numbers of breakpoints in the model
 * @class FindLineNumbers
 * @extends {ASTVisitor}
 */
class FindLineNumbers extends ASTVisitor {
    /**
     * Creates an instance of FindLineNumbers.
     */
    constructor() {
        super();
        this.nodeTolineNumbers = {};
    }
    /**
     * @inheritdoc
     */
    beginVisit(node) {
        if (!node.position) {
            return;
        }
        const { position } = node;
        const pathVector = [];
        node.getPathToNode(node, pathVector);
        const nodePath = this.stringfifyPathNode(pathVector);
        if (nodePath === '' || nodePath === undefined || nodePath === null) {
            throw new Error('node path not found');
        }
        this.nodeToLineNumbers = this.nodeToLineNumbers || {};
        this.nodeToLineNumbers[nodePath] = position;
    }
    /**
     * @inheritdoc
     */
    canVisit() {
        return true;
    }

    /**
     * @param {array} pathVector
     * @returns {string} path vector string
     * @memberof FindLineNumbers
     */
    stringfifyPathNode(pathVector = []) {
        return pathVector.join('-');
    }

    getLineNumbers() {
        return this.nodeToLineNumbers;
    }
}

export default FindLineNumbers;
