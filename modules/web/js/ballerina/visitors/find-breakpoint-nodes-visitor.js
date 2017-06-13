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

class FindBreakpointNodesVisitor extends ASTVisitor {
    constructor() {
        super();
        this._breakpoints = [];
    }
    setBreakpoints(breakpoints = []) {
        this._breakpoints = breakpoints;
    }
    beginVisit(node) {
        const lineNumber = node.getLineNumber();
        const breakpointIndex = this._breakpoints.indexOf(lineNumber);
        if (breakpointIndex !== -1) {
            node.addBreakpoint();
            // to avoid setting isBreakpoint attribute to children
            this._breakpoints.splice(breakpointIndex, 1);
        }
        if (node.isBreakpoint && breakpointIndex === -1) {
            // breakpoint has removed but model is not updated
            node.removeBreakpoint();
        }
    }
    canVisit() {
        return true;
    }
}

export default FindBreakpointNodesVisitor;
