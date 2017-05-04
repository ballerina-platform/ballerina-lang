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
import log from 'log';
import EventChannel from 'event_channel';
import AbstractSourceGenVisitor from './abstract-source-gen-visitor';
import StatementVisitorFactory from './statement-visitor-factory';
import ExpressionVisitorFactory from './expression-visitor-factory';
import ConnectorDeclarationVisitor from './connector-declaration-visitor';
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

    canVisitStatement(statement) {
        return true;
    }

    canVisitVariableDeclaration(variableDeclaration) {
        return true;
    }

    beginVisitWorkerDeclaration(workerDeclaration) {
        var constructedSourceSegment = 'worker ' + workerDeclaration.getWorkerDeclarationStatement() + ' {';
        this.appendSource(constructedSourceSegment);
        log.debug('Begin Visit Worker Declaration');
    }

    visitWorkerDeclaration(workerDeclaration) {
        log.debug('Visit Worker Declaration');
    }

    endVisitWorkerDeclaration(workerDeclaration) {
        this.appendSource("}\n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Worker Declaration');
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
