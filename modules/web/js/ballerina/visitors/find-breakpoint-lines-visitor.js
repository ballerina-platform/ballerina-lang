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
import ASTVisitor from './ast-visitor';

/**
 * Finds line numbers of breakpoints in the model
 * @class FindBreakpointLinesVisitor
 * @extends {ASTVisitor}
 */
class FindBreakpointLinesVisitor extends ASTVisitor {
    /**
     * Creates an instance of FindBreakpointLinesVisitor.
     */
    constructor() {
        super();
        this._breakpoints = [];
    }
    /**
     * Returns the array of breakpoint line numbers
     * @returns {int[]}
     *
     * @memberof FindBreakpointLinesVisitor
     */
    getBreakpoints() {
        return _.sortedUniq(this._breakpoints);
    }
    /**
     * @inheritdoc
     */
    beginVisit(node) {
        if (node.isBreakpoint) {
            const lineNumber = node.getLineNumber();
            this._breakpoints.push(lineNumber);
        }
    }
    /**
     * @inheritdoc
     */
    canVisit() {
        return true;
    }
}

export default FindBreakpointLinesVisitor;
