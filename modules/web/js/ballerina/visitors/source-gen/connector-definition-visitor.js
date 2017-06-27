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

    canVisitConnectorDefinition() {
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
        const useDefaultWS = connectorDefinition.whiteSpace.useDefault;
        if (useDefaultWS) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation('\n' + this.getIndentation());
        }
        let constructedSourceSegment = '';
        _.forEach(connectorDefinition.getChildrenOfType(connectorDefinition.getFactory().isAnnotation),
            (annotationNode) => {
                if (annotationNode.isSupported()) {
                    constructedSourceSegment += annotationNode.toString()
                        + ((annotationNode.whiteSpace.useDefault) ? this.getIndentation() : '');
                }
            });

        constructedSourceSegment += 'connector'
          + connectorDefinition.getWSRegion(0) + connectorDefinition.getConnectorName()
          + connectorDefinition.getWSRegion(1) + '(' + connectorDefinition.getArgumentsAsString() + ')'
          + connectorDefinition.getWSRegion(2) + '{' + connectorDefinition.getWSRegion(3);
        constructedSourceSegment += (useDefaultWS) ? this.getIndentation() : '';
        this.appendSource(constructedSourceSegment);
        this.indent();
        log.debug('Begin Visit Connector Definition');
    }

    visitConnectorDefinition() {
        log.debug('Visit Connector Definition');
    }

    /**
     * End visiting the connector definition
     * @param {ConnectorDefinition} connectorDefinition - Connector Definition
     */
    endVisitConnectorDefinition(connectorDefinition) {
        this.outdent();
        this.appendSource('}' + connectorDefinition.getWSRegion(4));
        this.appendSource((connectorDefinition.whiteSpace.useDefault) ?
                      this.currentPrecedingIndentation : '');
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Connector Definition');
    }

    /**
     * Visit Connector Action
     * @param {ConnectorAction} connectorAction
     */
    visitConnectorAction(connectorAction) {
        const connectorActionVisitor = new ConnectorActionVisitor(this);
        connectorAction.accept(connectorActionVisitor);
    }

    /**
     * Visit Connector Declaration
     * @param {ConnectorDeclaration} connectorDeclaration
     */
    visitConnectorDeclaration(connectorDeclaration) {
        const connectorDeclarationVisitor = new ConnectorDeclarationVisitor(this);
        connectorDeclaration.accept(connectorDeclarationVisitor);
    }

    /**
     * Visit Variable Declaration
     * @param {VariableDeclaration} variableDeclaration
     */
    visitVariableDeclaration(variableDeclaration) {
        const variableDeclarationVisitor = new VariableDeclarationVisitor(this);
        variableDeclaration.accept(variableDeclarationVisitor);
    }

    /**
     * Visit Statements
     * @param {Statement} statement
     */
    visitStatement(statement) {
        const statementVisitorFactory = new StatementVisitorFactory();
        const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    }
}

export default ConnectorDefinitionVisitor;
