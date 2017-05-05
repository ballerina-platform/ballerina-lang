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
        var self = this;
        _.forEach(resourceDefinition.getAnnotations(), function(annotation) {
            if (!_.isEmpty(annotation.value)) {

                // At the moment we support only the http:Method and http:Path

                var constructedPathAnnotation;
                if (annotation.key.indexOf(":") !== -1) {
                    constructedPathAnnotation = '@' + annotation.key + '("' + annotation.value + '")\n';
                    // Separately handling the HTTP method annotations.
                    if (_.isEqual(annotation.key, "http:Method")) {
                        constructedPathAnnotation = "";
                        var methods = annotation.value.replace( /\n/g, " " ).split(/[\s,]+/);
                        _.forEach(methods, function(method){
                            var cleanedMethod = method.trim();
                            if (!_.isEmpty(cleanedMethod)) {
                                constructedPathAnnotation += '@http:' + cleanedMethod + " {}\n";
                            }
                        });
                    }
                    else if (_.isEqual(annotation.key, "http:Path")) {
                        constructedPathAnnotation = '@http:Path { value : "' + annotation.value +'" }';
                    }
                }
                self.appendSource(self.getParent().getIndentation() + constructedPathAnnotation);
            }
        });

        var constructedSourceSegment = 'resource ' + resourceDefinition.getResourceName() + ' (';

        constructedSourceSegment += resourceDefinition.getParametersAsString() + ') {\n';
        this.appendSource(this.getIndentation() + constructedSourceSegment);
        this.indent();
    }

    visitResourceDefinition(resourceDefinition) {
    }

    visitStatement(statement) {
        var statementVisitorFactory = new StatementVisitorFactory();
        var statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    }

    visitConnectorDeclaration(connectorDeclaration) {
        var connectorDeclarationVisitor = new ConnectorDeclarationVisitor(this);
        connectorDeclaration.accept(connectorDeclarationVisitor);
    }

    visitWorkerDeclaration(workerDeclaration) {
        var workerDeclarationVisitor = new WorkerDeclarationVisitor(this);
        workerDeclaration.accept(workerDeclarationVisitor);
    }

    endVisitResourceDefinition(resourceDefinition) {
        this.outdent();
        this.appendSource(this.getIndentation() + "}\n");
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default ResourceDefinitionVisitor;
