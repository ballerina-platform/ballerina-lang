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
define(['lodash', 'log', 'event_channel', './abstract-source-gen-visitor', './statement-visitor-factory',
        './connector-declaration-visitor', './variable-declaration-visitor', './worker-declaration-visitor'],
    function(_, log, EventChannel, AbstractSourceGenVisitor, StatementVisitorFactory, ConnectorDeclarationVisitor,
             VariableDeclarationVisitor, WorkerDeclarationVisitor) {

        /**
         * @param parent
         * @constructor
         */
        var FunctionDefinitionVisitor = function (parent) {
            AbstractSourceGenVisitor.call(this, parent);
        };

        FunctionDefinitionVisitor.prototype = Object.create(AbstractSourceGenVisitor.prototype);
        FunctionDefinitionVisitor.prototype.constructor = FunctionDefinitionVisitor;

        FunctionDefinitionVisitor.prototype.canVisitFunctionDefinition = function(functionDefinition){
            return true;
        };

        FunctionDefinitionVisitor.prototype.beginVisitFunctionDefinition = function(functionDefinition){
            /**
             * set the configuration start for the function definition language construct
             * If we need to add additional parameters which are dynamically added to the configuration start
             * that particular source generation has to be constructed here
             */
            var functionReturnTypes = functionDefinition.getReturnTypesAsString();
            var functionReturnTypesSource = "";
            if (!_.isEmpty(functionReturnTypes)) {
                functionReturnTypesSource = '(' + functionDefinition.getReturnTypesAsString() + ') ';
            }

            var constructedSourceSegment = 'function ' + functionDefinition.getFunctionName() + '(' +
                functionDefinition.getArgumentsAsString() + ') ' + functionReturnTypesSource + '{';
            this.appendSource(constructedSourceSegment);
            log.debug('Begin Visit FunctionDefinition');
        };

        FunctionDefinitionVisitor.prototype.visitFunctionDefinition = function(functionDefinition){
            log.debug('Visit FunctionDefinition');
        };

        FunctionDefinitionVisitor.prototype.endVisitFunctionDefinition = function(functionDefinition){
            this.appendSource("} \n");
            this.getParent().appendSource(this.getGeneratedSource());
            log.debug('End Visit FunctionDefinition');
        };

        FunctionDefinitionVisitor.prototype.visitStatement = function (statement) {
            var statementVisitorFactory = new StatementVisitorFactory();
            var statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        };

        /**
         * visits commentStatement
         * @param {Object} statement - comment statement
         */
        FunctionDefinitionVisitor.prototype.visitCommentStatement = function (statement) {
            var statementVisitorFactory = new StatementVisitorFactory();
            var statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        };

        FunctionDefinitionVisitor.prototype.visitConnectorDeclaration = function(connectorDeclaration){
            var connectorDeclarationVisitor = new ConnectorDeclarationVisitor(this);
            connectorDeclaration.accept(connectorDeclarationVisitor);
        };

        FunctionDefinitionVisitor.prototype.visitVariableDeclaration = function(variableDeclaration){
            var variableDeclarationVisitor = new VariableDeclarationVisitor(this);
            variableDeclaration.accept(variableDeclarationVisitor);
        };

        FunctionDefinitionVisitor.prototype.visitWorkerDeclaration = function(workerDeclaration){
            var workerDeclarationVisitor = new WorkerDeclarationVisitor(this);
            workerDeclaration.accept(workerDeclarationVisitor);
        };

        return FunctionDefinitionVisitor;
    });