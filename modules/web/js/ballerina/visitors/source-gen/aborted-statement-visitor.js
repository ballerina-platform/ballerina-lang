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
import ConnectorDeclarationVisitor from './connector-declaration-visitor';

/**
 * Source Gen visitor for the Aborted statement
 */
class AbortedStatementVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Can visit check for the Aborted Statement
     * @return {boolean} true|false whether the Aborted statement can visit or not
     */
    canVisitAbortedStatement() {
        return true;
    }

    /**
     * Begin visit for the Aborted Statement
     * @param {AbortedStatement} abortedStatement - Aborted Statement ASTNode
     */
    beginVisitAbortedStatement(abortedStatement) {
        this.node = abortedStatement;

        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        abortedStatement.setLineNumber(lineNumber, { doSilently: true });

        const constructedSourceSegment = 'aborted' + abortedStatement.getWSRegion(1) + '{'
            + abortedStatement.getWSRegion(2)
            + ((abortedStatement.whiteSpace.useDefault) ? this.getIndentation() : '');

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
     * @param {AbortedStatement} abortedStatement - Aborted Statement ASTNode
     */
    endVisitAbortedStatement(abortedStatement) {
        this.outdent();
        /* if using default ws, add a new line to end unless there are any
         committed statement available*/
        const parent = abortedStatement.getParent();
        let tailingWS = abortedStatement.getWSRegion(3);
        if (abortedStatement.whiteSpace.useDefault
            && (_.isEmpty(parent.getCommittedStatement()))) {
            tailingWS = '\n';
        } else {
            const abortedIndex = parent.children.indexOf(abortedStatement);
            const committedIndex = parent.children.indexOf(parent.getCommittedStatement());
            if (committedIndex < abortedIndex) {
                tailingWS = '\n';
            } else {
                tailingWS = ' ';
            }
        }

        const constructedSourceSegment = '}' + tailingWS;

        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }

    /**
     * Visit connector declarations
     * @param {ConnectorDeclaration} connectorDeclaration - connector declaration AST node
     */
    visitConnectorDeclaration(connectorDeclaration) {
        const connectorDeclarationVisitor = new ConnectorDeclarationVisitor(this);
        connectorDeclaration.accept(connectorDeclarationVisitor);
    }
}

export default AbortedStatementVisitor;
