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
import AbstractSourceGenVisitor from './abstract-source-gen-visitor';
import ResourceDefinitionVisitor from './resource-definition-visitor';
import ConnectorDeclarationVisitor from './connector-declaration-visitor';
import StatementVisitorFactory from './statement-visitor-factory';

/**
 * Service Definition source generation visitor
 */
class ServiceDefinitionVisitor extends AbstractSourceGenVisitor {

    /**
     * Can visit check for the service source generation
     * @return {boolean} - true|false
     */
    canVisitServiceDefinition() {
        return true;
    }

    /**
     * Begin visit for the service definition source generation
     * @param {ASTNode} serviceDefinition - service definition node
     */
    beginVisitServiceDefinition(serviceDefinition) {
        /**
         * set the configuration start for the service definition language construct
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        const useDefaultWS = serviceDefinition.whiteSpace.useDefault;
        if (useDefaultWS) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation('\n' + this.getIndentation());
        }
        let constructedSourceSegment = '';
        _.forEach(serviceDefinition.getChildrenOfType(serviceDefinition.getFactory().isAnnotation),
            (annotationNode) => {
                constructedSourceSegment += annotationNode.toString()
                      + ((annotationNode.whiteSpace.useDefault) ? this.getIndentation() : '');
            });

        const lineNumber = this.getTotalNumberOfLinesInSource()
            + this.getEndLinesInSegment(constructedSourceSegment) + 1;
        serviceDefinition.setLineNumber(lineNumber, { doSilently: true });
        constructedSourceSegment += 'service' + serviceDefinition.getWSRegion(0)
              + '<' + serviceDefinition.getWSRegion(1) + serviceDefinition.getProtocolPkgName()
              + serviceDefinition.getWSRegion(2) + '>' + serviceDefinition.getWSRegion(3)
              + serviceDefinition.getServiceName()
              + serviceDefinition.getWSRegion(4) + '{'
              + serviceDefinition.getWSRegion(5);
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
        this.appendSource(constructedSourceSegment);
        this.indent();
    }

    /**
     * End visit for the service definition source generation
     * @param {ASTNode} serviceDefinition - service definition ast node
     */
    endVisitServiceDefinition(serviceDefinition) {
        this.outdent();
        const constructedSourceSegment = '}' + serviceDefinition.getWSRegion(6) +
            ((serviceDefinition.whiteSpace.useDefault) ? this.currentPrecedingIndentation : '');
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }

    /**
     * Visit service level statements
     * @param {ASTNode} statement - Statement Node
     */
    visitStatement(statement) {
        const statementVisitorFactory = new StatementVisitorFactory();
        const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    }

    /**
     * Visit Resource Definitions
     * @param {ASTNode} resourceDefinition - ResourceDefinition node
     */
    visitResourceDefinition(resourceDefinition) {
        const resourceDefinitionVisitor = new ResourceDefinitionVisitor(this);
        resourceDefinition.accept(resourceDefinitionVisitor);
    }

    /**
     * Visit service level connector declarations
     * @param {ASTNode} connectorDeclaration - Connector declaration node
      */
    visitConnectorDeclaration(connectorDeclaration) {
        const connectorDeclarationVisitor = new ConnectorDeclarationVisitor(this);
        connectorDeclaration.accept(connectorDeclarationVisitor);
    }
}

export default ServiceDefinitionVisitor;
