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
        './type-mapper-block-statement-visitor'],
    function(_, log, EventChannel, AbstractSourceGenVisitor, StatementVisitorFactory, TypeMapperBlockStatementVisitor) {

        /**
         * @param parent
         * @constructor
         */
        var TypeMapperDefinitionVisitor = function (parent) {
            AbstractSourceGenVisitor.call(this, parent);
        };

        TypeMapperDefinitionVisitor.prototype = Object.create(AbstractSourceGenVisitor.prototype);
        TypeMapperDefinitionVisitor.prototype.constructor = TypeMapperDefinitionVisitor;

        TypeMapperDefinitionVisitor.prototype.canVisitTypeMapperDefinition = function(typeMapperDefinition){
            return true;
        };

        TypeMapperDefinitionVisitor.prototype.canVisitBlockStatementView = function (blockStatementView) {
            return true;
        };

        TypeMapperDefinitionVisitor.prototype.beginVisitTypeMapperDefinition = function(typeMapperDefinition){
            /**
             * set the configuration start for the type mapper definition language construct
             * If we need to add additional parameters which are dynamically added to the configuration start
             * that particular source generation has to be constructed here
             */

            var constructedSourceSegment = 'typemapper ' + typeMapperDefinition.getTypeMapperName() +
                ' (' + typeMapperDefinition.getInputParamAndIdentifier() + ' )(' + typeMapperDefinition.getReturnType() +
                ' ) {';
            this.appendSource(constructedSourceSegment);
            log.debug('Begin Visit TypeMapperDefinition');
        };

        TypeMapperDefinitionVisitor.prototype.visitTypeMapperDefinition = function(typeMapperDefinition){
            log.debug('Visit TypeMapperDefinition');
        };

        TypeMapperDefinitionVisitor.prototype.endVisitTypeMapperDefinition = function(typeMapperDefinition){
            this.appendSource("} \n");
            this.getParent().appendSource(this.getGeneratedSource());
            log.debug('End Visit TypeMapperDefinition');
        };

       TypeMapperDefinitionVisitor.prototype.visitBlockStatement = function (blockStatement) {
           var blockStatementVisitor = new TypeMapperBlockStatementVisitor(this);
           blockStatement.accept(blockStatementVisitor);
       };

        return TypeMapperDefinitionVisitor;
    });