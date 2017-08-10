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
import AbstractStatementSourceGenVisitor from './abstract-statement-source-gen-visitor';
import StatementVisitorFactory from './statement-visitor-factory';

/**
 * Source Gen visitor for the Failed statement
 */
class FailedStatementVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Can visit check for the Failed Statement
     * @return {boolean} true|false whether the Failed statement can visit or not
     */
    canVisitFailedStatement() {
        return true;
    }

    /**
     * Begin visit for the Aborted Statement
     * @param {AbortedStatement} failedStatement - Aborted Statement ASTNode
     */
    beginVisitFailedStatement(failedStatement) {
        this.node = failedStatement;

        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        failedStatement.setLineNumber(lineNumber, { doSilently: true });

        const constructedSourceSegment = 'failed' + failedStatement.getWSRegion(1) + '{'
            + failedStatement.getWSRegion(2)
            + ((failedStatement.whiteSpace.useDefault) ? this.getIndentation() : '');

        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        // Increase the total number of lines
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.indent();
    }

    /**
     * Visit Statements
     * @param {Statement} statement - statement ASTNode
     */
    visitStatement(statement) {
        if (!_.isEqual(this.node, statement)) {
            const statementVisitorFactory = new StatementVisitorFactory();
            const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        }
    }

    /**
     * End visit for the Aborted Statement
     * @param {AbortedStatement} failedStatement - Aborted Statement ASTNode
     */
    endVisitFailedStatement(failedStatement) {
        this.outdent();
        /* if using default ws, add a new line to end unless there are any
         committed statement available*/
        const parent = failedStatement.getParent();
        let tailingWS = failedStatement.getWSRegion(3);
        if (failedStatement.whiteSpace.useDefault
            && (_.isEmpty(parent.getCommittedStatement()) && _.isEmpty(parent.getAbortedStatement()))) {
            tailingWS = '\n';
        } else {
            const committedIndex = parent.children.indexOf(parent.getCommittedStatement());
            const abortedIndex = parent.children.indexOf(parent.getAbortedStatement());
            if (committedIndex !== -1 || abortedIndex !== -1) {
                tailingWS = ' ';
            } else {
                tailingWS = '\n';
            }
        }

        const constructedSourceSegment = '}' + tailingWS;

        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default FailedStatementVisitor;
