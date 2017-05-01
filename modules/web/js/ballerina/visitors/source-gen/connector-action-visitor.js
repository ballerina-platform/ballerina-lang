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
 * @param {ASTVisitor} parent - Parent AST Visitor
 * @constructor
 */
class ConnectorActionVisitor extends AbstractSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitConnectorAction(connectorAction) {
        return true;
    }

    canVisitConnectorDeclaration(connectorDeclaration) {
        return true;
    }

    canVisitVariableDeclaration(variableDeclaration) {
        return true;
    }

    canVisitWorkerDeclaration(workerDeclaration) {
        return true;
    }

    /**
     * Begin visit of the connector action
     * @param {ConnectorAction} connectorAction
     */
    beginVisitConnectorAction(connectorAction) {
        /**
         * set the configuration start for the function definition language construct
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        var functionReturnTypes = connectorAction.getReturnTypesAsString();
        var connectorActionReturnTypesSource = "";
        if (!_.isEmpty(functionReturnTypes)) {
            connectorActionReturnTypesSource = '(' + connectorAction.getReturnTypesAsString() + ') ';
        }

        var constructedSourceSegment = 'action ' + connectorAction.getActionName() + '(' +
            connectorAction.getArgumentsAsString() + ') ' + connectorActionReturnTypesSource + '{';
        this.appendSource(constructedSourceSegment);
        log.debug('Begin Visit Connector Action');
    }

    visitConnectorAction(connectorAction) {
        log.debug('Visit Connector Action');
    }

    /**
     * End visit of the Connector Action
     * @param {ConnectorAction} connectorAction
     */
    endVisitConnectorAction(connectorAction) {
        this.appendSource("} \n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit FunctionDefinition');
    }

    /**
     * Visit Statements
     * @param {Statement} statement
     */
    visitStatement(statement) {
        var statementVisitorFactory = new StatementVisitorFactory();
        var statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    }

    /**
     * Visit Connector Declarations
     * @param {ConnectorDeclaration} connectorDeclaration
     */
    visitConnectorDeclaration(connectorDeclaration) {
        var connectorDeclarationVisitor = new ConnectorDeclarationVisitor(this);
        connectorDeclaration.accept(connectorDeclarationVisitor);
    }

    /**
     * Visit variable Declaration
     * @param {VariableDeclaration} variableDeclaration
     */
    visitVariableDeclaration(variableDeclaration) {
        var variableDeclarationVisitor = new VariableDeclarationVisitor(this);
        variableDeclaration.accept(variableDeclarationVisitor);
    }

    /**
     * Visit Worker Declaration
     * @param workerDeclaration
     */
    visitWorkerDeclaration(workerDeclaration) {
        var workerDeclarationVisitor = new WorkerDeclarationVisitor(this);
        workerDeclaration.accept(workerDeclarationVisitor);
    }
}

export default ConnectorActionVisitor;
