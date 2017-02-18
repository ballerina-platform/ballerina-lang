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

        var TypeMapperTypeCastExpressionVisitor = function(parent){
            AbstractExpressionSourceGenVisitor.call(this,parent);
        };

        TypeMapperTypeCastExpressionVisitor.prototype = Object.create(AbstractExpressionSourceGenVisitor.prototype);
        TypeMapperTypeCastExpressionVisitor.prototype.constructor = TypeMapperTypeCastExpressionVisitor;

        TypeMapperTypeCastExpressionVisitor.prototype.canVisitTypeCastExpression = function(expression){
            return true;
        };

        TypeMapperTypeCastExpressionVisitor.prototype.beginVisitTypeCastExpression = function(expression){
            this.appendSource('(' + expression.getName() + ')');
            log.debug('Begin Visit Type Mapper Type Cast Expression');
        };

        TypeMapperTypeCastExpressionVisitor.prototype.visitTypeCastExpression = function(expression){
            log.debug('Visit Type Mapper Ref Type Type Cast Expression');
        };

        TypeMapperTypeCastExpressionVisitor.prototype.endVisitTypeCastExpression = function(expression){
            this.getParent().appendSource(this.getGeneratedSource());
            log.debug('End Visit Type Mapper Type Cast Expression');
        };

        TypeMapperTypeCastExpressionVisitor.prototype.visitExpression = function (expression) {
            var TypeMapperExpressionVisitorFactory = require('./type-mapper-expression-visitor-factory');
            var expressionVisitorFactory = new TypeMapperExpressionVisitorFactory();
            var expressionVisitor = expressionVisitorFactory.getExpressionVisitor({model:expression, parent:this});
            expression.accept(expressionVisitor);
            log.debug('Visit Expression');
        };

        return TypeMapperTypeCastExpressionVisitor;
    });