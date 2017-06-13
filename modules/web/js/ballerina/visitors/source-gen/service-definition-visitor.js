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
 * @param parent
 * @constructor
 */
class ServiceDefinitionVisitor extends AbstractSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitServiceDefinition(serviceDefinition) {
        return true;
    }

    beginVisitServiceDefinition(serviceDefinition) {
        /**
         * set the configuration start for the service definition language construct
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        const useDefaultWS = serviceDefinition.whiteSpace.useDefault;
        if (useDefaultWS) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(`\n${this.getIndentation()}`);
        }
        let constructedSourceSegment = '';
        _.forEach(serviceDefinition.getChildrenOfType(serviceDefinition.getFactory().isAnnotation),
            (annotationNode) => {
                constructedSourceSegment += annotationNode.toString()
                      + ((annotationNode.whiteSpace.useDefault) ? this.getIndentation() : '');
            });
        constructedSourceSegment += `service${serviceDefinition.getWSRegion(0)
               }${serviceDefinition.getServiceName()
               }${serviceDefinition.getWSRegion(1)}{${
               serviceDefinition.getWSRegion(2)}`;
        this.appendSource(constructedSourceSegment);
        this.indent();
    }

    endVisitServiceDefinition(serviceDefinition) {
        this.outdent();
        this.appendSource(`}${serviceDefinition.getWSRegion(3)}`);
        this.appendSource((serviceDefinition.whiteSpace.useDefault) ?
                      this.currentPrecedingIndentation : '');
        this.getParent().appendSource(this.getGeneratedSource());
    }

    visitStatement(statement) {
        const statementVisitorFactory = new StatementVisitorFactory();
        const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    }

    visitResourceDefinition(resourceDefinition) {
        const resourceDefinitionVisitor = new ResourceDefinitionVisitor(this);
        resourceDefinition.accept(resourceDefinitionVisitor);
    }

    visitConnectorDeclaration(connectorDeclaration) {
        const connectorDeclarationVisitor = new ConnectorDeclarationVisitor(this);
        connectorDeclaration.accept(connectorDeclarationVisitor);
    }
}

export default ServiceDefinitionVisitor;
