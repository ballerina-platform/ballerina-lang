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
import ConnectorActionVisitor from './connector-action-visitor';
import VariableDeclarationVisitor from './variable-declaration-visitor';
import ConnectorDeclarationVisitor from './connector-declaration-visitor';
import StatementVisitorFactory from './statement-visitor-factory';

/**
 * @param {ASTVisitor} parent - parent visitor
 * @constructor
 */
class ConnectorDefinitionVisitor extends AbstractSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitConnectorDefinition(connectorDefinition) {
        return true;
    }

    canVisitConnectorAction(connectorAction){
        return true;
    }

    /**
     * Begin the visit and generate the source
     * @param {ConnectorDefinition} connectorDefinition - Connector Definition
     */
    beginVisitConnectorDefinition(connectorDefinition) {
        /**
         * set the configuration start for the connector definition language construct
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        var self = this;
        var argumentsSrc = "";
        _.forEach(connectorDefinition.getAnnotations(), function(annotation) {
            if (!_.isEmpty(annotation.value)) {
                var constructedPathAnnotation;
                if (annotation.key.indexOf(":") === -1) {
                    constructedPathAnnotation = '@' + annotation.key + '("' + annotation.value + '")\n';
                } else {
                    constructedPathAnnotation = '@' + annotation.key.split(":")[0] + '(' + annotation.key.split(":")[1] +
                        ' = "' + annotation.value + '")\n';
                }
                self.appendSource(constructedPathAnnotation);
            }
        });

        _.forEach(connectorDefinition.getArguments(), function(argument, index){
            argumentsSrc += argument.type + " ";
            argumentsSrc += argument.identifier;
            if (connectorDefinition.getArguments().length - 1 != index) {
                argumentsSrc += ", ";
            }
        });

        var constructedSourceSegment = 'connector ' + connectorDefinition.getConnectorName() +
            ' (' + argumentsSrc + ')' + ' {\n';
        this.appendSource(constructedSourceSegment);
        log.debug('Begin Visit Connector Definition');
    }

    visitConnectorDefinition(connectorDefinition) {
        log.debug('Visit Connector Definition');
    }

    /**
     * End visiting the connector definition
     * @param {ConnectorDefinition} connectorDefinition - Connector Definition
     */
    endVisitConnectorDefinition(connectorDefinition) {
        this.appendSource("}\n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Connector Definition');
    }

    /**
     * Visit Connector Action
     * @param {ConnectorAction} connectorAction
     */
    visitConnectorAction(connectorAction) {
        var connectorActionVisitor = new ConnectorActionVisitor(this);
        connectorAction.accept(connectorActionVisitor);
    }

    /**
     * Visit Connector Declaration
     * @param {ConnectorDeclaration} connectorDeclaration
     */
    visitConnectorDeclaration(connectorDeclaration) {
        var connectorDeclarationVisitor = new ConnectorDeclarationVisitor(this);
        connectorDeclaration.accept(connectorDeclarationVisitor);
    }

    /**
     * Visit Variable Declaration
     * @param {VariableDeclaration} variableDeclaration
     */
    visitVariableDeclaration(variableDeclaration) {
        var variableDeclarationVisitor = new VariableDeclarationVisitor(this);
        variableDeclaration.accept(variableDeclarationVisitor);
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
}

export default ConnectorDefinitionVisitor;
    