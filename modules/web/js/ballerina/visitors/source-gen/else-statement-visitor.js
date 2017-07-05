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
 * Source Generation for the Else Statement
 */
class ElseStatementVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Can visit check for the Else statement
     * @return {boolean} true|false - whether the Else statement can visit or not
     */
    canVisitElseStatement() {
        return true;
    }

    /**
     * Begin visit for the Else statement
     * @param {ElseStatement} elseStatement - Else statement ASTNode
     */
    beginVisitElseStatement(elseStatement) {
        this.node = elseStatement;

        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        elseStatement.setLineNumber(lineNumber, { doSilently: true });

        /**
        * set the configuration start for the if statement definition language construct
        * If we need to add additional parameters which are dynamically added to the configuration start
        * that particular source generation has to be constructed here
        */
        const constructedSourceSegment = 'else' + elseStatement.getWSRegion(1) + '{' + elseStatement.getWSRegion(2)
            + ((elseStatement.whiteSpace.useDefault) ? this.getIndentation() : '');

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
     * End visit for the Else Statement
     * @param {ElseStatement} elseStatement - Else Statement ASTNode
     */
    endVisitElseStatement(elseStatement) {
        this.outdent();
        const constructedSourceSegment = '}' + elseStatement.getWSRegion(3);

        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default ElseStatementVisitor;
