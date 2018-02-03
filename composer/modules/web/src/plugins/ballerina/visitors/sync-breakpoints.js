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

import _ from 'lodash';

/**
 * Finds line numbers in new ASTmodel and replaces in oldASTModel
 * @class SyncBreakpoints
 */
class SyncBreakpoints {
    constructor() {
        this._breakpoints = [];
    }
    /**
     * @inheritdoc
     */
    beginVisit(node, newNode) {
        if (!node.isBreakpoint) {
            return;
        }
        newNode.isBreakpoint = node.isBreakpoint;
        const lineNumber = newNode.position.startLine;
        this._breakpoints.push(lineNumber);
    }
    getBreakpoints() {
        return _.sortedUniq(this._breakpoints);
    }
    /**
     * @inheritdoc
     */
    canVisit() {
        return true;
    }

    /**
 * At the end of node visit.
 * @returns {any} Visit output.
 * @memberof SyncBreakpoints
 */
    endVisit() {
        return undefined;
    }
}

export default SyncBreakpoints;
