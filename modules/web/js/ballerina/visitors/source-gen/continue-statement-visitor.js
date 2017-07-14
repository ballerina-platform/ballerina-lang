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

import AbstractStatementSourceGenVisitor from './abstract-statement-source-gen-visitor';
import ContinueStatement from '../../ast/statements/continue-statement';

/**
 * Source generation visitor for Continue statement
 */
class ContinueStatementVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Can visit check for the continue statement
     * @param {ContinueStatement} continueStatement - continue statementASTNode
     * @return {boolean} true| false - whether the continue statement can visit or not
     */
    canVisitContinueStatement(continueStatement) {
        return continueStatement instanceof ContinueStatement;
    }

    /**
     * Begin visit for continue statement
     * @param {ContinueStatement} continueStatement - continue statementASTNode
     */
    beginVisitContinueStatement(continueStatement) {
        this.node = continueStatement;
        if (continueStatement.whiteSpace.useDefault) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(this.getIndentation());
        }

        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        continueStatement.setLineNumber(lineNumber, { doSilently: true });

        const constructedSourceSegment = continueStatement.getStatementString();
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        // Increase the total number of lines
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
        this.appendSource(constructedSourceSegment);
    }

    /**
     * Visit the continue statement
     */
    visitContinueStatement() {
    }

    /**
     * End visit for continue statement
     * @param {ContinueStatement} continueStatement - continue statementASTNode
     */
    endVisitContinueStatement(continueStatement) {
        const constructedSourceSegment = continueStatement.getWSRegion(1) + ';' + continueStatement.getWSRegion(2)
            + ((continueStatement.whiteSpace.useDefault) ? this.currentPrecedingIndentation : '');

        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default ContinueStatementVisitor;
