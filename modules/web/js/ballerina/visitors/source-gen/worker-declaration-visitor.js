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

import AbstractSourceGenVisitor from './abstract-source-gen-visitor';
import StatementVisitorFactory from './statement-visitor-factory';
import VariableDeclarationVisitor from './variable-declaration-visitor';
import WorkerDeclaration from '../../ast/worker-declaration';
import ConnectorDeclarationVisitor from './connector-declaration-visitor';
/**
 * Source generation for the worker Declaration
 */
class WorkerDeclarationVisitor extends AbstractSourceGenVisitor {

    /**
     * Can visit check for the worker declaration
     * @param {WorkerDeclaration} workerDeclaration - Worker DeclarationASTNode
     * @return {boolean} true|false - whether the worker declaration can visit or not
     */
    canVisitWorkerDeclaration(workerDeclaration) {
        return workerDeclaration instanceof WorkerDeclaration && !workerDeclaration.isDefaultWorker();
    }

    /**
     * Begin visit for the Worker Declaration
     * @param {WorkerDeclaration} workerDeclaration - worker Declaration ASTNode
     */
    beginVisitWorkerDeclaration(workerDeclaration) {
        const useDefaultWS = workerDeclaration.whiteSpace.useDefault;
        if (useDefaultWS) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(this.getIndentation());
        }

        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        workerDeclaration.setLineNumber(lineNumber, { doSilently: true });

        const constructedSourceSegment = 'worker' + workerDeclaration.getWSRegion(1)
            + workerDeclaration.getWorkerDeclarationStatement() + workerDeclaration.getWSRegion(2) + '{'
            + workerDeclaration.getWSRegion(3);

        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        // Increase the total number of lines
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.indent();
    }

    /**
     * Visit Worker Declaration
     */
    visitWorkerDeclaration() {
    }

    /**
     * End visit for the While Statement
     * @param {WorkerDeclaration} workerDeclaration - Worker Declaration ASTNode
     */
    endVisitWorkerDeclaration(workerDeclaration) {
        this.outdent();
        let constructedSourceSegment = '';
        if (workerDeclaration.whiteSpace.useDefault) {
            constructedSourceSegment += this.getIndentation();
        }

        constructedSourceSegment += '}' + workerDeclaration.getWSRegion(4);

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
        const statementVisitorFactory = new StatementVisitorFactory();
        const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    }

    /**
     * Visit variable declaration
     * @param {VariableDeclaration} variableDeclaration - variable declaration ASTNode
     */
    visitVariableDeclaration(variableDeclaration) {
        const variableDeclarationVisitor = new VariableDeclarationVisitor(this);
        variableDeclaration.accept(variableDeclarationVisitor);
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

export default WorkerDeclarationVisitor;
