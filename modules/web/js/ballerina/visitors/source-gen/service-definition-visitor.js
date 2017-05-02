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

    canVisitStatement(statement) {
        return true;
    }

    canVisitConnectorDeclaration(connectorDeclaration) {
        return true;
    }

    canVisitResourceDefinition(resourceDefinition) {
        return true;
    }

    canVisitVariableDeclaration(variableDeclaration) {
        return true;
    }

    beginVisitServiceDefinition(serviceDefinition) {
        /**
         * set the configuration start for the service definition language construct
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        var self = this;
        _.forEach(serviceDefinition.getAnnotations(), function(annotation) {
            if (!_.isEmpty(annotation.value)) {
                var constructedPathAnnotation = '';
                if (annotation.key.indexOf(':') !== -1) {
                    constructedPathAnnotation = '@' + annotation.key.split(':')[0] + ':'
                        + annotation.key.split(':')[1] + '{value:"' + annotation.value + '"}\n';
                }
                self.appendSource(constructedPathAnnotation);
            }
        });

        var constructedSourceSegment = 'service ' + serviceDefinition.getServiceName() + ' {\n';
        this.appendSource(constructedSourceSegment);
        log.debug('Begin Visit Service Definition');
    }

    visitServiceDefinition(serviceDefinition) {
        log.debug('Visit Service Definition');
    }

    endVisitServiceDefinition(serviceDefinition) {
        this.appendSource("}\n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Service Definition');
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

    visitVariableDeclaration(variableDeclaration) {
        var variableDeclarationVisitor = new VariableDeclarationVisitor(this);
        variableDeclaration.accept(variableDeclarationVisitor);
    }
}

export default ServiceDefinitionVisitor;
