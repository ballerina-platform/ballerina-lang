/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import AbstractStatementSourceGenVisitor from './abstract-statement-source-gen-visitor';

/**
 * Source generation visitor for function invocation statement
 */
class FunctionInvocationVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Can visit check for function invocation statement
     * @return {boolean} true|false
     */
    canVisitFuncInvocationStatement() {
        return true;
    }

    /**
     * Begin visit for function invocation statement
     * @param {FunctionInvocationStatement} functionInvocation - function invocation statement ASTNode
     */
    beginVisitFuncInvocationStatement(functionInvocation) {
        if (functionInvocation.whiteSpace.useDefault) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(this.getIndentation());
        }
        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        functionInvocation.setLineNumber(lineNumber, { doSilently: true });
    }

    /**
     * Visit Function Invocation Expression
     * @param {FunctionInvocationExpression} functionInvocation - function invocation expression ASTNode
     */
    visitFuncInvocationExpression(functionInvocation) {
        const constructedSourceSegment = functionInvocation.getExpressionString();
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);

        // Increase the total number of lines
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
        this.appendSource(constructedSourceSegment);
    }

    /**
     * End visit for function invocation statement
     * @param {FunctionInvocationStatement} functionInvocation - function invocation statement ASTNode
     */
    endVisitFuncInvocationStatement(functionInvocation) {
        const constructedSourceSegment = ';' + functionInvocation.getWSRegion(4)
            + ((functionInvocation.whiteSpace.useDefault) ? this.currentPrecedingIndentation : '');

        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default FunctionInvocationVisitor;
