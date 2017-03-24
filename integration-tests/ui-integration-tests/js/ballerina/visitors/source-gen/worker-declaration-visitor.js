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
define(['lodash', 'log', 'event_channel', './abstract-source-gen-visitor', './statement-visitor-factory',
        './expression-visitor-factory', './connector-declaration-visitor', './variable-declaration-visitor', '../../ast/worker-declaration'],
    function (_, log, EventChannel, AbstractSourceGenVisitor, StatementVisitorFactory, ExpressionVisitorFactory,
              ConnectorDeclarationVisitor,VariableDeclarationVisitor, WorkerDeclaration) {

        /**
         * @param parent
         * @constructor
         */
        var WorkerDeclarationVisitor = function (parent) {
            AbstractSourceGenVisitor.call(this, parent);
        };

        WorkerDeclarationVisitor.prototype = Object.create(AbstractSourceGenVisitor.prototype);
        WorkerDeclarationVisitor.prototype.constructor = WorkerDeclarationVisitor;

        WorkerDeclarationVisitor.prototype.canVisitWorkerDeclaration = function (workerDeclaration) {
            return workerDeclaration instanceof WorkerDeclaration && !workerDeclaration.isDefaultWorker();
        };

        WorkerDeclarationVisitor.prototype.beginVisitWorkerDeclaration = function (workerDeclaration) {
            var constructedSourceSegment = 'worker ' + workerDeclaration.getWorkerDeclarationStatement() + ' {';
            this.appendSource(constructedSourceSegment);
            log.debug('Begin Visit Worker Declaration');
        };

        WorkerDeclarationVisitor.prototype.visitWorkerDeclaration = function (workerDeclaration) {
            log.debug('Visit Worker Declaration');
        };

        WorkerDeclarationVisitor.prototype.endVisitWorkerDeclaration = function (workerDeclaration) {
            this.appendSource("}\n");
            this.getParent().appendSource(this.getGeneratedSource());
            log.debug('End Visit Worker Declaration');
        };

        WorkerDeclarationVisitor.prototype.visitStatement = function (statement) {
            var statementVisitorFactory = new StatementVisitorFactory();
            var statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        };

        WorkerDeclarationVisitor.prototype.visitVariableDeclaration = function(variableDeclaration){
            var variableDeclarationVisitor = new VariableDeclarationVisitor(this);
            variableDeclaration.accept(variableDeclarationVisitor);
        };

        return WorkerDeclarationVisitor;
    });