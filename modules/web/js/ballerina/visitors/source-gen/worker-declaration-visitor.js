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

/**
 * @param parent
 * @constructor
 */
class WorkerDeclarationVisitor extends AbstractSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitWorkerDeclaration(workerDeclaration) {
        return workerDeclaration instanceof WorkerDeclaration && !workerDeclaration.isDefaultWorker();
    }

    beginVisitWorkerDeclaration(workerDeclaration) {
        var constructedSourceSegment =  this.getIndentation() + 'worker '
                  + workerDeclaration.getWorkerDeclarationStatement() + ' {\n';
        this.appendSource(constructedSourceSegment);
        this.indent();
    }

    visitWorkerDeclaration(workerDeclaration) {
    }

    endVisitWorkerDeclaration(workerDeclaration) {
        this.outdent();
        this.appendSource(this.getIndentation() + "}\n");
        this.getParent().appendSource(this.getGeneratedSource());
    }

    visitStatement(statement) {
        var statementVisitorFactory = new StatementVisitorFactory();
        var statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    }

    visitVariableDeclaration(variableDeclaration) {
        var variableDeclarationVisitor = new VariableDeclarationVisitor(this);
        variableDeclaration.accept(variableDeclarationVisitor);
    }
}

export default WorkerDeclarationVisitor;
