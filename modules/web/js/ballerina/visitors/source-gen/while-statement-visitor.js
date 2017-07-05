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

import _ from 'lodash';
import AbstractStatementSourceGenVisitor from './abstract-statement-source-gen-visitor';
import StatementVisitorFactory from './statement-visitor-factory';

/**
 * Source Gen visitor for the While statement
 */
class WhileStatementVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Can visit check for the While Statement
     * @return {boolean} true|false - Check whether the while statement can visit or not
     */
    canVisitWhileStatement() {
        if (_.isNil(this.node)) {
            return true;
        }
        return false;
    }

    /**
     * Begin visit for the While statement
     * @param {WhileStatement} whileStatement - While statement ASTNode
     */
    beginVisitWhileStatement(whileStatement) {
        this.node = whileStatement;
        if (whileStatement.whiteSpace.useDefault) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(this.getIndentation());
        }

        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        whileStatement.setLineNumber(lineNumber, { doSilently: true });

        const constructedSourceSegment = 'while' + whileStatement.getWSRegion(1) + '('
            + whileStatement.getWSRegion(2) + whileStatement.getConditionString() + ')'
            + whileStatement.getWSRegion(3) + '{' + whileStatement.getWSRegion(4)
            + ((whileStatement.whiteSpace.useDefault) ? this.getIndentation() : '');

        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        // Increase the total number of lines
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.indent();
    }

    /**
     * Visit While Statement
     */
    visitWhileStatement() {
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
     * End visit for the While Statement
     * @param {WhileStatement} whileStatement - While Statement ASTNode
     */
    endVisitWhileStatement(whileStatement) {
        this.outdent();
        const constructedSourceSegment = '}' + whileStatement.getWSRegion(5)
            + ((whileStatement.whiteSpace.useDefault) ? this.currentPrecedingIndentation : '');

        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default WhileStatementVisitor;
