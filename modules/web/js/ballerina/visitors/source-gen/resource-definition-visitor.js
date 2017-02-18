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
define(['lodash', 'log', 'event_channel', './abstract-source-gen-visitor', './statement-visitor-factory',
        './expression-visitor-factory', './connector-declaration-visitor', './variable-declaration-visitor',
        './worker-declaration-visitor'],
    function (_, log, EventChannel, AbstractSourceGenVisitor, StatementVisitorFactory, ExpressionVisitorFactory,
              ConnectorDeclarationVisitor,VariableDeclarationVisitor, WorkerDeclarationVisitor) {

    /**
     * @param parent
     * @constructor
     */
    var ResourceDefinitionVisitor = function (parent) {
        AbstractSourceGenVisitor.call(this, parent);
    };

    ResourceDefinitionVisitor.prototype = Object.create(AbstractSourceGenVisitor.prototype);
    ResourceDefinitionVisitor.prototype.constructor = ResourceDefinitionVisitor;

    ResourceDefinitionVisitor.prototype.canVisitResourceDefinition = function (resourceDefinition) {
        return true;
    };

    ResourceDefinitionVisitor.prototype.beginVisitResourceDefinition = function (resourceDefinition) {
        /**
         * set the configuration start for the resource definition language construct
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        var self = this;
        _.forEach(resourceDefinition.getAnnotations(), function(annotation) {
            if (!_.isEmpty(annotation.value)) {
                var constructedPathAnnotation;
                if (annotation.key.indexOf(":") === -1) {
                    constructedPathAnnotation = '@' + annotation.key + '("' + annotation.value + '")\n';
                } else {
                    constructedPathAnnotation = '@' + annotation.key.split(":")[0] + ':' +
                        annotation.key.split(":")[1] + '("' + annotation.value + '")\n';
                }

                // Separately handling the HTTP method annotations.
                if (annotation.key == "Method") {
                    constructedPathAnnotation = "";
                    var methods = annotation.value.replace( /\n/g, " " ).split(/[\s,]+/);
                    _.forEach(methods, function(method){
                        var cleanedMethod = method.trim();
                        if (!_.isEmpty(cleanedMethod)) {
                            if (_.includes(cleanedMethod, "http:")) {
                                constructedPathAnnotation += "@" + cleanedMethod + " \n";
                            } else {
                                constructedPathAnnotation += "@http:" + cleanedMethod + " \n";
                            }
                        }
                    });
                }

                self.appendSource(constructedPathAnnotation);
            }
        });

        var constructedSourceSegment = 'resource ' + resourceDefinition.getResourceName() + '(';

        constructedSourceSegment += resourceDefinition.getParametersAsString() + ') {';
        this.appendSource(constructedSourceSegment);
        log.debug('Begin Visit ResourceDefinition');
    };

    ResourceDefinitionVisitor.prototype.visitResourceDefinition = function (resourceDefinition) {
        log.debug('Visit ResourceDefinition');
    };

    ResourceDefinitionVisitor.prototype.endVisitResourceDefinition = function (resourceDefinition) {
        this.appendSource("}\n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit ResourceDefinition');
    };

    ResourceDefinitionVisitor.prototype.visitStatement = function (statement) {
        var statementVisitorFactory = new StatementVisitorFactory();
        var statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    };

    ResourceDefinitionVisitor.prototype.visitExpression = function(expression){
        var expressionViewFactory = new ExpressionVisitorFactory();
        var expressionView = expressionViewFactory.getExpressionView({model:expression, parent:this});
        expression.accept(expressionView);
    };

    ResourceDefinitionVisitor.prototype.visitConnectorDeclaration = function(connectorDeclaration){
        var connectorDeclarationVisitor = new ConnectorDeclarationVisitor(this);
        connectorDeclaration.accept(connectorDeclarationVisitor);
    };

    ResourceDefinitionVisitor.prototype.visitVariableDeclaration = function(variableDeclaration){
        var varialeDeclarationVisitor = new VariableDeclarationVisitor(this);
        variableDeclaration.accept(varialeDeclarationVisitor);
    };

    ResourceDefinitionVisitor.prototype.visitWorkerDeclaration = function(workerDeclaration){
        var workerDeclarationVisitor = new WorkerDeclarationVisitor(this);
        workerDeclaration.accept(workerDeclarationVisitor);
    };

    return ResourceDefinitionVisitor;
});