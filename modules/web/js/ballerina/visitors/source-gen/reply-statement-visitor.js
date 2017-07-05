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
 * Source generation visitor for reply statement
 */
class ReplyStatementVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Can visit check for reply statement
     * @return {boolean} true|false - whether the reply statement can visit or not
     */
    canVisitReplyStatement() {
        return true;
    }

    /**
     * Begin visit for reply statement
     * @param {ReplyStatement} replyStatement - Reply statement ASTNode
     */
    beginVisitReplyStatement(replyStatement) {
        if (replyStatement.whiteSpace.useDefault) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(this.getIndentation());
        }

        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        replyStatement.setLineNumber(lineNumber, { doSilently: true });

        const constructedSourceSegment = 'reply' + replyStatement.getWSRegion(1) + replyStatement.getReplyMessage();
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        // Increase the total number of lines
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
        this.appendSource(constructedSourceSegment);
    }

    /**
     * Visit reply statement
     */
    visitReplyStatement() {
    }

    /**
     * End visit for reply statement
     * @param {ReplyStatement} replyStatement - Reply statement ASTNode
     */
    endVisitReplyStatement(replyStatement) {
        const constructedSourceSegment = ';' + replyStatement.getWSRegion(2)
            + ((replyStatement.whiteSpace.useDefault) ? this.currentPrecedingIndentation : '');

        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default ReplyStatementVisitor;
