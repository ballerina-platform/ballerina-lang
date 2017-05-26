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
import AbstractSourceGenVisitor from './abstract-source-gen-visitor';
import ResourceDefinitionVisitor from './resource-definition-visitor';
import VariableDeclarationVisitor from './variable-declaration-visitor';
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
        let useDefaultWS = serviceDefinition.whiteSpace.useDefault;
        let constructedSourceSegment = (useDefaultWS) ? ('\n' + this.getIndentation()) : '' ;
        _.forEach(serviceDefinition.getChildrenOfType(serviceDefinition.getFactory().isAnnotation),
            annotationNode => {
                constructedSourceSegment +=
                      ((useDefaultWS) ? this.getIndentation() : '')
                      + annotationNode.toString()
                      + ((useDefaultWS) ? '\n' : '');
            });
        constructedSourceSegment += ((useDefaultWS) ? this.getIndentation() : '')
              + 'service' + serviceDefinition.getWSRegion(0)
              + serviceDefinition.getServiceName()
              + serviceDefinition.getWSRegion(1) + '{'
              + serviceDefinition.getWSRegion(2);
        this.appendSource(constructedSourceSegment);
        this.indent();
    }

    endVisitServiceDefinition(serviceDefinition) {
        this.outdent();
        this.appendSource('}' + serviceDefinition.getWSRegion(3));
        this.getParent().appendSource(this.getGeneratedSource());
    }

    visitStatement(statement) {
        var statementVisitorFactory = new StatementVisitorFactory();
        var statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    }

    visitResourceDefinition(resourceDefinition) {
        var resourceDefinitionVisitor = new ResourceDefinitionVisitor(this);
        resourceDefinition.accept(resourceDefinitionVisitor);
    }

    visitConnectorDeclaration(connectorDeclaration) {
        var connectorDeclarationVisitor = new ConnectorDeclarationVisitor(this);
        connectorDeclaration.accept(connectorDeclarationVisitor);
    }
}

export default ServiceDefinitionVisitor;
