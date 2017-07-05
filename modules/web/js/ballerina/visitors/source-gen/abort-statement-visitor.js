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
import AbortStatement from '../../ast/statements/abort-statement';

/**
 * Source generation visitor for abort statement
 */
class AbortStatementVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Can visit Abort Statement.
     * @param {AbortStatement} abortStatement - abort statement ast visitor
     * @return {boolean} true if instance of AbortStatement, else false.
     */
    canVisitAbortStatement(abortStatement) {
        return abortStatement instanceof AbortStatement;
    }

    /**
     * Begin Visit Abort Statement.
     * @param {AbortStatement} abortStatement - abort statement ASTNode
     */
    beginVisitAbortStatement(abortStatement) {
        this.node = abortStatement;
        if (abortStatement.whiteSpace.useDefault) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(this.getIndentation());
        }

        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        abortStatement.setLineNumber(lineNumber);
        const constructedSourceSegment = abortStatement.getStatementString();
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);

        // Increase the total number of lines
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
        this.appendSource(constructedSourceSegment);
    }

    /**
     * Visit Abort Statement.
     */
    visitAbortStatement() {
    }

    /**
     * End visit Abort Statement.
     * @param {AbortStatement} abortStatement - abort statement ASTNode
     */
    endVisitAbortStatement(abortStatement) {
        const constructedSourceSegment = abortStatement.getWSRegion(1) + ';' + abortStatement.getWSRegion(2)
            + ((abortStatement.whiteSpace.useDefault) ? this.currentPrecedingIndentation : '');

        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default AbortStatementVisitor;
