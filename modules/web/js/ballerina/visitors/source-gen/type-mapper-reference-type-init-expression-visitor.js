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
define(['require','lodash', 'log', 'event_channel', './abstract-expression-source-gen-visitor'],
    function(require, _, log, EventChannel, AbstractExpressionSourceGenVisitor) {

        var TypeMapperRefTypeInitExpressionVisitor = function(parent){
            AbstractExpressionSourceGenVisitor.call(this,parent);
        };

        TypeMapperRefTypeInitExpressionVisitor.prototype = Object.create(AbstractExpressionSourceGenVisitor.prototype);
        TypeMapperRefTypeInitExpressionVisitor.prototype.constructor = TypeMapperRefTypeInitExpressionVisitor;

        TypeMapperRefTypeInitExpressionVisitor.prototype.canVisitReferenceTypeInitExpression = function(expression){
            return true;
        };

        TypeMapperRefTypeInitExpressionVisitor.prototype.beginVisitReferenceTypeInitExpression = function(expression){
            this.appendSource("{}");
            log.debug('Begin Visit Type Mapper Ref Type Init Expression');
        };

        TypeMapperRefTypeInitExpressionVisitor.prototype.visitReferenceTypeInitExpression = function(expression){
            log.debug('Visit Type Mapper Ref Type Init  Expression');
        };

        TypeMapperRefTypeInitExpressionVisitor.prototype.endVisitReferenceTypeInitExpression = function(expression){
            this.getParent().appendSource(this.getGeneratedSource());
            log.debug('End Visit Type Mapper Ref Type Init  Expression');
        };

        return TypeMapperRefTypeInitExpressionVisitor;
    });