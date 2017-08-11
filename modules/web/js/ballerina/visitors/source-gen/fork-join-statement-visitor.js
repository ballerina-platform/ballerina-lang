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
import WorkerDeclarationVisitor from './worker-declaration-visitor';

/**
 * Fork Join Statement Source Generation visitor
 */
class ForkJoinStatementVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Can visit check for the ForkJoin statement
     * @return {boolean} true| false - whether the ForkJoin statement can visit or not
     */
    canVisitForkJoinStatement() {
        return true;
    }

    /**
     * Begin visit for ForkJoin Statement
     * @param {ForkJoinStatement} forkJoinStatement - forkJoin Statement statementASTNode
     */
    beginVisitForkJoinStatement(forkJoinStatement) {
        this.forkClosed = false;
        this.node = forkJoinStatement;
        if (forkJoinStatement.whiteSpace.useDefault) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(this.getIndentation());
        }

        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        forkJoinStatement.setLineNumber(lineNumber, { doSilently: true });

        const constructedSourceSegment = 'fork' + forkJoinStatement.getWSRegion(1) + '{'
            + forkJoinStatement.getWSRegion(2);

        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        // Increase the total number of lines
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.indent();
    }

    /**
     * Visit worker Declaration
     * @param {WorkerDeclaration} workerDeclaration - Worker Declaration ASTNode
     */
    visitWorkerDeclaration(workerDeclaration) {
        const workerDeclarationVisitor = new WorkerDeclarationVisitor(this);
        workerDeclaration.accept(workerDeclarationVisitor);
    }

    /**
     * End visit for forkJoin Statement
     * @param {ForkJoinStatement} forkJoinStatement - forkJoin Statement statementASTNode
     */
    endVisitForkJoinStatement(forkJoinStatement) {
        const constructedSourceSegment = (forkJoinStatement.whiteSpace.useDefault)
            ? this.currentPrecedingIndentation : '';
        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }

    /**
     * Visit Statement
     * @param {Statement} statement - statement ASTNode
     */
    visitStatement(statement) {
        if (!_.isEqual(this.node, statement)) {
            if (!this.forkClosed) {
                const forkJoinStatement = statement.getParent();
                this.outdent();

                let constructedSourceSegment = '';
                if (forkJoinStatement.whiteSpace.useDefault) {
                    constructedSourceSegment += this.getIndentation();
                }
                constructedSourceSegment += '}' + statement.getWSRegion(0);

                // Add the increased number of lines
                const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
                this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

                this.appendSource(constructedSourceSegment);
                this.forkClosed = true;
            }
            const statementVisitorFactory = new StatementVisitorFactory();
            const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        }
    }
}

export default ForkJoinStatementVisitor;
