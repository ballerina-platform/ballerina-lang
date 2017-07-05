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
import ReturnStatement from '../../ast/statements/return-statement';
import ExpressionVisitorFactory from './expression-visitor-factory';

/**
 * Source generation for return statement
 */
class ReturnStatementVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Can visit check for return statement
     * @param {ReturnStatement} returnStatement - return statement ASTNode
     * @return {boolean} true|false - whether the return statement can visit or not
     */
    canVisitReturnStatement(returnStatement) {
        return returnStatement instanceof ReturnStatement;
    }

    /**
     * Begin visit for return statement
     * @param {ReturnStatement} returnStatement - Return statement ASTNode
     */
    beginVisitReturnStatement(returnStatement) {
        /**
         * set the configuration start for the reply statement definition language construct
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        if (returnStatement.whiteSpace.useDefault) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(this.getIndentation());
        }

        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        returnStatement.setLineNumber(lineNumber, { doSilently: true });
        const constructedSourceSegment = returnStatement.getReturnExpression();
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        // Increase the total number of lines
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
        this.appendSource(constructedSourceSegment);
    }

    /**
     * End visit for return statement
     * @param {ReturnStatement} returnStatement - Return statement ASTNode
     */
    endVisitReturnStatement(returnStatement) {
        const constructedSourceSegment = ';' + returnStatement.getWSRegion(3)
            + ((returnStatement.whiteSpace.useDefault) ? this.currentPrecedingIndentation : '');

        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }

    /**
     * Visit expression
     * @param {Expression} expression - Expression ASTNode
     */
    visitExpression(expression) {
        const expressionVisitorFactory = new ExpressionVisitorFactory();
        const expressionVisitor = expressionVisitorFactory.getExpressionView({ model: expression, parent: this });
        expression.accept(expressionVisitor);
    }
}

export default ReturnStatementVisitor;
