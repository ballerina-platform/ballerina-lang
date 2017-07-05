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
 * Join statement Source generation visitor
 */
class JoinStatementVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Can visit check for the Join Statement
     * @return {boolean} true|false
     */
    canVisitJoinStatement() {
        return true;
    }

    /**
     * Begin visit for the Join statement source generation
     * @param {JoinStatement} joinStatement - Join Statement ASTNode
     */
    beginVisitJoinStatement(joinStatement) {
        this.node = joinStatement;
        const JOIN_CONDITION_KEY = 'joinCondition';

        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        joinStatement.setLineNumber(lineNumber, { doSilently: true });

        const constructedSourceSegment = 'join' + joinStatement.getWSRegion(1) + '(' +
            joinStatement.getChildWSRegion(JOIN_CONDITION_KEY, 0) + joinStatement.getJoinConditionString() +
            joinStatement.getChildWSRegion(JOIN_CONDITION_KEY, 2) + ')' + joinStatement.getWSRegion(2) +
            '(' + joinStatement.getParameter().getWSRegion(0) +
            joinStatement.getParameter().getParameterDefinitionAsString() + ')' + joinStatement.getWSRegion(5) + '{' +
            joinStatement.getWSRegion(6) + ((joinStatement.whiteSpace.useDefault) ? this.getIndentation() : '');

        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        // Increase the total number of lines
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.indent();
    }

    /**
     * End visit for the Join statement source generation
     * @param {JoinStatement} joinStatement - Join Statement ASTNode
     */
    endVisitJoinStatement(joinStatement) {
        const constructedSourceSegment = '}' + joinStatement.getWSRegion(7);

        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }

    /**
     * Visit Statement
     * @param {Statement} statement - Statement ASTNode
     */
    visitStatement(statement) {
        if (!_.isEqual(this.node, statement)) {
            const statementVisitorFactory = new StatementVisitorFactory();
            const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        }
    }
}

export default JoinStatementVisitor;
