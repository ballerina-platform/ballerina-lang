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
import RetryStatement from '../../ast/statements/retry-statement';

/**
 * Source generation for retry statement
 */
class RetryStatementVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Can visit check for retry statement
     * @param {ReturnStatement} retryStatement - retry statement ASTNode
     * @return {boolean} true|false - whether the retry statement can visit or not
     */
    canVisitRetryStatement(retryStatement) {
        return retryStatement instanceof RetryStatement;
    }

    /**
     * Begin visit for retry statement
     * @param {ReturnStatement} retryStatement - Retry statement ASTNode
     */
    beginVisitRetryStatement(retryStatement) {
        /**
         * set the configuration start for the retry statement definition language construct
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        if (retryStatement.whiteSpace.useDefault) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(this.getIndentation());
        }

        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        retryStatement.setLineNumber(lineNumber, { doSilently: true });
        let constructedSourceSegment = '';
        this.appendSource(`retry${retryStatement.getWSRegion(1)}`);

        const children = retryStatement.children;
        const spaces = '';
        if (children.length <= 0) {
            constructedSourceSegment += '3';
        } else {
            constructedSourceSegment += children[0].getExpressionString();
        }
        this.appendSource(constructedSourceSegment);
        constructedSourceSegment = `retry ${constructedSourceSegment}${spaces}`;

        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        // Increase the total number of lines
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
    }

    /**
     * End visit for retry statement
     * @param {RetryStatement} retryStatement - Retry statement ASTNode
     */
    endVisitRetryStatement(retryStatement) {
        const constructedSourceSegment = ';' + retryStatement.getWSRegion(2)
            + ((retryStatement.whiteSpace.useDefault) ? this.currentPrecedingIndentation : '');

        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        const generatedSource = this.getGeneratedSource();
        this.getParent().appendSource(generatedSource);
        retryStatement.viewState.source = generatedSource;
    }
}

export default RetryStatementVisitor;
