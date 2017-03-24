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
define(['lodash', 'log', 'event_channel', './abstract-source-gen-visitor', './variable-declaration-visitor'],
    function (_, log, EventChannel, AbstractSourceGenVisitor, VariableDeclarationVisitor) {

        /**
         * @param parent
         * @constructor
         */
        var StructDefinitionVisitor = function (parent) {
            AbstractSourceGenVisitor.call(this, parent);
        };

        StructDefinitionVisitor.prototype = Object.create(AbstractSourceGenVisitor.prototype);
        StructDefinitionVisitor.prototype.constructor = StructDefinitionVisitor;

        StructDefinitionVisitor.prototype.canVisitStructDefinition = function (structDefinition) {
            return true;
        };

        StructDefinitionVisitor.prototype.beginVisitStructDefinition = function (structDefinition) {
            var constructedSourceSegment = 'struct ' + structDefinition.getStructName() + "{ \n";
            _.forEach(structDefinition.getVariableDefinitions(), function (variable) {
                constructedSourceSegment = constructedSourceSegment + variable.getVariableDefinitionAsString() + "\n";
            });
            this.appendSource(constructedSourceSegment);
            log.debug('Begin Visit FunctionDefinition');
        };

        StructDefinitionVisitor.prototype.visitStructDefinition = function (structDefinition) {
            log.debug('Visit FunctionDefinition');
        };

        StructDefinitionVisitor.prototype.endVisitStructDefinition = function (structDefinition) {
            this.appendSource("} \n");
            this.getParent().appendSource(this.getGeneratedSource());
            log.debug('End Visit FunctionDefinition');
        };

        return StructDefinitionVisitor;
    });