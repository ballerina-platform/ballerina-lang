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
import log from 'log';
import _ from 'lodash';
import AbstractSourceGenVisitor from './abstract-source-gen-visitor';
import StatementVisitorFactory from './statement-visitor-factory';
import ExpressionVisitorFactory from './expression-visitor-factory';
import ConnectorDeclarationVisitor from './connector-declaration-visitor';
import VariableDeclarationVisitor from './variable-declaration-visitor';
import WorkerDeclarationVisitor from './worker-declaration-visitor';

/**
 * @param parent
 * @constructor
 */
class ResourceDefinitionVisitor extends AbstractSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitResourceDefinition(resourceDefinition) {
        return true;
    }

    beginVisitResourceDefinition(resourceDefinition) {
        /**
         * set the configuration start for the resource definition language construct
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        let constructedSourceSegment = '';
        _.forEach(resourceDefinition.getChildrenOfType(resourceDefinition.getFactory().isAnnotation), annotationNode => {
            if (annotationNode.isSupported()) {
                constructedSourceSegment += annotationNode.toString() + '\n';
            }
        });

        constructedSourceSegment += 'resource ' + resourceDefinition.getResourceName() + '(';

        constructedSourceSegment += resourceDefinition.getParametersAsString() + ') {';
        this.appendSource(constructedSourceSegment);
        log.debug('Begin Visit ResourceDefinition');
    }

    visitResourceDefinition(resourceDefinition) {
        log.debug('Visit ResourceDefinition');
    }

    endVisitResourceDefinition(resourceDefinition) {
        this.appendSource("}\n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit ResourceDefinition');
    }

    visitStatement(statement) {
        var statementVisitorFactory = new StatementVisitorFactory();
        var statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    }

    visitExpression(expression) {
        var expressionViewFactory = new ExpressionVisitorFactory();
        var expressionView = expressionViewFactory.getExpressionView({model:expression, parent:this});
        expression.accept(expressionView);
    }

    visitConnectorDeclaration(connectorDeclaration) {
        var connectorDeclarationVisitor = new ConnectorDeclarationVisitor(this);
        connectorDeclaration.accept(connectorDeclarationVisitor);
    }

    visitVariableDeclaration(variableDeclaration) {
        var varialeDeclarationVisitor = new VariableDeclarationVisitor(this);
        variableDeclaration.accept(varialeDeclarationVisitor);
    }

    visitWorkerDeclaration(workerDeclaration) {
        var workerDeclarationVisitor = new WorkerDeclarationVisitor(this);
        workerDeclaration.accept(workerDeclarationVisitor);
    }
}

export default ResourceDefinitionVisitor;

