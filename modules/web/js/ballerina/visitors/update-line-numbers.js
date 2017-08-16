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
 * @class UpdateLineNumbers
 * @extends {ASTVisitor}
 */
class UpdateLineNumbers extends ASTVisitor {
    /**
     * Creates an instance of UpdateLineNumbers.
     */
    constructor() {
        super();
        this.nodeTolineNumbers = {};
    }

    /**
     *
     * @param {object} nodeTolineNumbers - node to line numbers map
     * @memberof UpdateLineNumbers
     */
    setLineNumbers(nodeTolineNumbers = {}) {
        this.nodeTolineNumbers = nodeTolineNumbers;
    }

    /**
     * @inheritdoc
     */
    beginVisit(node) {
        const pathVector = [];
        node.getPathToNode(node, pathVector);
        const nodePath = this.stringfifyPathNode(pathVector);

        const position = this.nodeTolineNumbers[nodePath];
        if (position === '' || position === null || position === undefined) {
            return;
        }
        const lineNumber = this.nodeTolineNumbers[nodePath].startLine;
        node._lineNumber = lineNumber;
        node.position = this.nodeTolineNumbers[nodePath];
    }
    /**
     * @inheritdoc
     */
    canVisit() {
        return true;
    }

    stringfifyPathNode(pathVector = []) {
        return pathVector.join('-');
    }
}

export default UpdateLineNumbers;
