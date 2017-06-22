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
 * Debug hit visitor class for indicate debug hit
 * @class FindDebugHitVisitor
 * @extends {ASTVisitor}
 */
class FindDebugHitVisitor extends ASTVisitor {
    /**
     * Creates an instance of FindDebugHitVisitor.
     * @memberof FindDebugHitVisitor
     */
    constructor() {
        super();
        this._position = {};
    }
    /**
     * @param {object} position - object with line number and file name properties
     * @memberof FindDebugHitVisitor
     */
    setPosition(position) {
        this._position = position;
    }
    /**
     * @inheritdoc
     */
    beginVisit(node) {
        if (node.getLineNumber() === this._position.lineNumber) {
            node.addDebugHit();
            this.hasToRerender = true;
        }
        if (node.isDebugHit && node.getLineNumber() !== this._position.lineNumber) {
            // debughit has removed but model is not updated
            node.removeDebugHit();
            this.hasToRerender = true;
        }
    }
    /**
     * @inheritdoc
     */
    canVisit() {
        return true;
    }
}

export default FindDebugHitVisitor;
