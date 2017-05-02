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
import log from 'log';
import EventChannel from 'event_channel';
import AbstractSourceGenVisitor from './abstract-source-gen-visitor';
import StatementVisitorFactory from './statement-visitor-factory';
import ConnectorDeclarationVisitor from './connector-declaration-visitor';
import VariableDeclarationVisitor from './variable-declaration-visitor';
import WorkerDeclarationVisitor from './worker-declaration-visitor';

/**
 * @param parent
 * @constructor
 */
class FunctionDefinitionVisitor extends AbstractSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitFunctionDefinition(functionDefinition) {
        return true;
    }

    canVisitCommentStatement(commentStatement) {
        return true;
    }

    canVisitStatement(statement) {
        return true;
    }

    canVisitConnectorDeclaration(statement) {
        return true;
    }

    canVisitVariableDeclaration(statement) {
        return true;
    }

    canVisitWorkerDeclaration(statement) {
        return true;
    }

    beginVisitFunctionDefinition(functionDefinition) {
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
    }

    visitFunctionDefinition(functionDefinition) {
        log.debug('Visit FunctionDefinition');
    }

    endVisitFunctionDefinition(functionDefinition) {
        this.appendSource("} \n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit FunctionDefinition');
    }

    visitStatement(statement) {
        var statementVisitorFactory = new StatementVisitorFactory();
        var statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    }

    /**
     * visits commentStatement
     * @param {Object} statement - comment statement
     */
    visitCommentStatement(statement) {
        var statementVisitorFactory = new StatementVisitorFactory();
        var statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    }

    visitConnectorDeclaration(connectorDeclaration) {
        var connectorDeclarationVisitor = new ConnectorDeclarationVisitor(this);
        connectorDeclaration.accept(connectorDeclarationVisitor);
    }

    visitVariableDeclaration(variableDeclaration) {
        var variableDeclarationVisitor = new VariableDeclarationVisitor(this);
        variableDeclaration.accept(variableDeclarationVisitor);
    }

    visitWorkerDeclaration(workerDeclaration) {
        var workerDeclarationVisitor = new WorkerDeclarationVisitor(this);
        workerDeclaration.accept(workerDeclarationVisitor);
    }
}

export default FunctionDefinitionVisitor;
