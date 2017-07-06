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
 * Source Gen visitor for the Comment statement
 */
class CommentStatementVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Can visit check for the Comment Statement
     * @return {boolean} true|false whether the Comment statement can visit or not
     */
    canVisitCommentStatement() {
        return true;
    }

    /**
     * Begin visit for the Comment Statement
     * @param {CommentStatement} commentStatement - Comment Statement ASTNode
     */
    beginVisitCommentStatement(commentStatement) {
        if (commentStatement.whiteSpace.useDefault) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(this.getIndentation());
        }

        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        commentStatement.setLineNumber(lineNumber, { doSilently: true });
    }

    /**
     * Visit Comment Statement
     * @param {CommentStatement} commentStatement - comment Statement ASTNode
     */
    visitCommentStatement(commentStatement) {
        const constructedSourceSegment = commentStatement.getStatementString() + commentStatement.getWSRegion(1);

        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        // Increase the total number of lines
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
    }

    /**
     * End visit for the Comment Statement
     * @param {CommentStatement} commentStatement - Comment Statement ASTNode
     */
    endVisitCommentStatement(commentStatement) {
        const constructedSourceSegment = ((commentStatement.whiteSpace.useDefault)
            ? this.currentPrecedingIndentation : '');
        
        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default CommentStatementVisitor;
