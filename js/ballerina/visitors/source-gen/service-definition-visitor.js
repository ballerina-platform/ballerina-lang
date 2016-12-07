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
define(['lodash', 'log', 'event_channel', './abstract-source-gen-visitor', './resource-definition-visitor'], function(_, log, EventChannel, AbstractSourceGenVisitor, ResourceDefinitionVisitor) {

    var ServiceDefinitionVisitor = function() {
        AbstractSourceGenVisitor.call(this);
    };

    ServiceDefinitionVisitor.prototype = Object.create(AbstractSourceGenVisitor.prototype);
    ServiceDefinitionVisitor.prototype.constructor = ServiceDefinitionVisitor;

    ServiceDefinitionVisitor.prototype.canVisitServiceDefinition = function(serviceDefinition){
        return true;
    };

    ServiceDefinitionVisitor.prototype.beginVisitServiceDefinition = function(serviceDefinition){
        log.info('Begin Visit Service Definition');
    };

    ServiceDefinitionVisitor.prototype.visitServiceDefinition = function(serviceDefinition){
        log.info('Visit Service Definition');
    };

    ServiceDefinitionVisitor.prototype.endVisitServiceDefinition = function(serviceDefinition){
        log.info('End Visit Service Definition');
    };

    ServiceDefinitionVisitor.prototype.visitResourceDefinition = function(resourceDefinition){
        var resourceDefinitionVisitor = new ResourceDefinitionVisitor();
        resourceDefinition.accept(resourceDefinitionVisitor);
    };

    return ServiceDefinitionVisitor;
});