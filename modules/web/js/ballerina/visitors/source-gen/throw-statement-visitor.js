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

/**
 * Source generation for throw statement
 */
class ThrowStatementVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Can visit check for the throw statement
     * @return {boolean} true|false - whether the throw statement can visit or not
     */
    canVisitThrowStatement() {
        return true;
    }

    /**
     * Begin visit for the throw statement
     * @param {ThrowStatement} throwStatement - throw statement ASTNode
     */
    beginVisitThrowStatement(throwStatement) {
        if (throwStatement.whiteSpace.useDefault) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(this.getIndentation());
        }

        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        throwStatement.setLineNumber(lineNumber, { doSilently: true });

        const constructedSourceSegment = throwStatement.getStatementString();
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        // Increase the total number of lines
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
        this.appendSource(constructedSourceSegment);
    }

    /**
     * Visit the throw statement
     */
    visitThrowStatement() {
    }

    /**
     * End visit for the throw statement
     * @param {ThrowStatement} throwStatement - throw statement ASTNode
     */
    endVisitThrowStatement(throwStatement) {
        const constructedSourceSegment = ';' + throwStatement.getWSRegion(2)
            + ((throwStatement.whiteSpace.useDefault) ? this.currentPrecedingIndentation : '');

        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
        
        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default ThrowStatementVisitor;
