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
define(['lodash', 'log', 'event_channel', './abstract-source-gen-visitor', './resource-definition-visitor',
        './variable-declaration-visitor', './connector-declaration-visitor', './statement-visitor-factory'],
    function(_, log, EventChannel, AbstractSourceGenVisitor, ResourceDefinitionVisitor,
             VariableDeclarationVisitor, ConnectorDeclarationVisitor, StatementVisitorFactory) {

    /**
     * @param parent
     * @constructor
     */
    var ServiceDefinitionVisitor = function (parent) {
        AbstractSourceGenVisitor.call(this, parent);
    };

    ServiceDefinitionVisitor.prototype = Object.create(AbstractSourceGenVisitor.prototype);
    ServiceDefinitionVisitor.prototype.constructor = ServiceDefinitionVisitor;

    ServiceDefinitionVisitor.prototype.canVisitServiceDefinition = function(serviceDefinition){
        return true;
    };

    ServiceDefinitionVisitor.prototype.beginVisitServiceDefinition = function(serviceDefinition){
        /**
         * set the configuration start for the service definition language construct
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        var self = this;
        _.forEach(serviceDefinition.getAnnotations(), function(annotation) {
            if (!_.isEmpty(annotation.value)) {
                var constructedPathAnnotation;
                if (annotation.key.indexOf(":") === -1) {
                    constructedPathAnnotation = '@' + annotation.key + '("' + annotation.value + '")\n';
                } else {
                    constructedPathAnnotation = '@' + annotation.key.split(":")[0] + ":"
                        + annotation.key.split(":")[1] + '("' + annotation.value + '")\n';
                }
                self.appendSource(constructedPathAnnotation);
            }
        });

        var constructedSourceSegment = 'service ' + serviceDefinition.getServiceName() + ' {\n';
        this.appendSource(constructedSourceSegment);
        log.debug('Begin Visit Service Definition');
    };

    ServiceDefinitionVisitor.prototype.visitServiceDefinition = function(serviceDefinition){
        log.debug('Visit Service Definition');
    };

    ServiceDefinitionVisitor.prototype.endVisitServiceDefinition = function(serviceDefinition){
        this.appendSource("}\n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Service Definition');
    };

    ServiceDefinitionVisitor.prototype.visitStatement = function (statement) {
        var statementVisitorFactory = new StatementVisitorFactory();
        var statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        statement.accept(statementVisitor);
    };

    ServiceDefinitionVisitor.prototype.visitResourceDefinition = function(resourceDefinition){
        var resourceDefinitionVisitor = new ResourceDefinitionVisitor(this);
        resourceDefinition.accept(resourceDefinitionVisitor);
    };

    ServiceDefinitionVisitor.prototype.visitConnectorDeclaration = function(connectorDeclaration){
        var connectorDeclarationVisitor = new ConnectorDeclarationVisitor(this);
        connectorDeclaration.accept(connectorDeclarationVisitor);
    };

    ServiceDefinitionVisitor.prototype.visitVariableDeclaration = function(variableDeclaration){
        var variableDeclarationVisitor = new VariableDeclarationVisitor(this);
        variableDeclaration.accept(variableDeclarationVisitor);
    };

    return ServiceDefinitionVisitor;
});