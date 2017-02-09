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
define(['lodash', 'log', 'event_channel', './abstract-source-gen-visitor'],
    function (_, log, EventChannel, AbstractSourceGenVisitor) {

    /**
     * Visitor for a constant definition.
     * @param {ASTVisitor} parent - Parent visitor.
     * @constructor
     */
    var ConstantDefinitionVisitor = function (parent) {
        AbstractSourceGenVisitor.call(this, parent);
    };

    ConstantDefinitionVisitor.prototype = Object.create(AbstractSourceGenVisitor.prototype);
    ConstantDefinitionVisitor.prototype.constructor = ConstantDefinitionVisitor;

    ConstantDefinitionVisitor.prototype.canVisitConstantDefinition = function (constantDefinition) {
        return true;
    };

    /**
     * @param {ConstantDefinition} constantDefinition - The constant definition to start visiting.
     */
    ConstantDefinitionVisitor.prototype.beginVisitConstantDefinition = function (constantDefinition) {
        var constructedSourceSegment = constantDefinition.getConstantDefinitionAsString();
        this.appendSource(constructedSourceSegment);
        log.debug('Begin Visit ConstantDefinition');
    };

    ConstantDefinitionVisitor.prototype.visitConstantDefinition = function (constantDefinition) {
        log.debug('Visit ConstantDefinition');
    };

    ConstantDefinitionVisitor.prototype.endVisitConstantDefinition = function (constantDefinition) {
        this.appendSource(";\n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit ConstantDefinition');
    };

    return ConstantDefinitionVisitor;
});