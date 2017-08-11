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
import ConnectorDeclarationVisitor from './connector-declaration-visitor';

/**
 * Source Gen visitor for the Catch statement
 */
class CatchStatementVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Can visit check for the Catch Statement
     * @return {boolean} true|false whether the Catch statement can visit or not
     */
    canVisitCatchStatement() {
        return true;
    }

    /**
     * Begin visit for the Catch statement
     * @param {TryStatement} catchStatement - Catch statement ASTNode
     */
    beginVisitCatchStatement(catchStatement) {
        this.node = catchStatement;

        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        catchStatement.setLineNumber(lineNumber, { doSilently: true });

        /**
         * set the configuration start for the catch statement
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        const constructedSourceSegment = 'catch' + catchStatement.getWSRegion(1) + '('
            + catchStatement.getWSRegion(2)
            + catchStatement.getParameterDefString() // FIXME fix the model to support different catches
            + catchStatement.getWSRegion(4) + ')'
            + catchStatement.getWSRegion(5) + '{'
            + catchStatement.getWSRegion(6)
            + ((catchStatement.whiteSpace.useDefault) ? this.getIndentation() : '');

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
     * End visit for the Catch Statement
     * @param {CatchStatement} catchStatement - Catch Statement ASTNode
     */
    endVisitCatchStatement(catchStatement) {
        this.outdent();
        const constructedSourceSegment = '}' + catchStatement.getWSRegion(7);

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

export default CatchStatementVisitor;
