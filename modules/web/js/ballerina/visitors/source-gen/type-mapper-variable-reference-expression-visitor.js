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
define(['require','lodash', 'log', 'event_channel', './abstract-expression-source-gen-visitor','./type-mapper-variable-definition-visitor'],
    function(require, _, log, EventChannel, AbstractExpressionSourceGenVisitor, TypeMapperVariableDefinitionVisitor) {

        var TypeMapperVariableReferenceExpressionVisitor = function(parent){
            AbstractExpressionSourceGenVisitor.call(this,parent);
        };

        TypeMapperVariableReferenceExpressionVisitor.prototype = Object.create(AbstractExpressionSourceGenVisitor.prototype);
        TypeMapperVariableReferenceExpressionVisitor.prototype.constructor = TypeMapperVariableReferenceExpressionVisitor;

        TypeMapperVariableReferenceExpressionVisitor.prototype.canVisitVariableReferenceExpression = function(expression){
            return true;
        };

        TypeMapperVariableReferenceExpressionVisitor.prototype.beginVisitVariableReferenceExpression = function(expression){
           log.debug('Begin Visit Type Mapper Variable Reference Expression');
        };

        TypeMapperVariableReferenceExpressionVisitor.prototype.visitVariableReferenceExpression = function(expression){
            log.debug('Visit Type Mapper Variable Reference Expression');
        };

        TypeMapperVariableReferenceExpressionVisitor.prototype.endVisitVariableReferenceExpression = function(expression){
            if (expression.getVariableReferenceName()) {
                this.appendSource(expression.getVariableReferenceName());
            }
            this.getParent().appendSource(this.getGeneratedSource());
            log.debug('End Visit Type Mapper Variable Reference Expression');
        };

        TypeMapperVariableReferenceExpressionVisitor.prototype.visitVariableDefinition = function(variableDefinition){
            var variableDefinitionVisitor = new TypeMapperVariableDefinitionVisitor(this);
            variableDefinition.accept(variableDefinitionVisitor);
        };

        return TypeMapperVariableReferenceExpressionVisitor;
    });