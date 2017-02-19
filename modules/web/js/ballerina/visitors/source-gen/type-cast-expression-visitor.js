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

        var TypeCastExpressionVisitor = function(parent){
            AbstractExpressionSourceGenVisitor.call(this,parent);
        };

        TypeCastExpressionVisitor.prototype = Object.create(AbstractExpressionSourceGenVisitor.prototype);
        TypeCastExpressionVisitor.prototype.constructor = TypeCastExpressionVisitor;

        TypeCastExpressionVisitor.prototype.canVisitTypeCastExpression = function(expression){
            return true;
        };

        TypeCastExpressionVisitor.prototype.beginVisitTypeCastExpression = function(expression){
            this.appendSource('(' + expression.getName() + ')');
            log.debug('Begin Visit Type Cast Expression');
        };

        TypeCastExpressionVisitor.prototype.visitTypeCastExpression = function(expression){
            log.debug('Visit Ref Type Type Cast Expression');
        };

        TypeCastExpressionVisitor.prototype.endVisitTypeCastExpression = function(expression){
            this.getParent().appendSource(this.getGeneratedSource());
            log.debug('End Visit Type Cast Expression');
        };

        TypeCastExpressionVisitor.prototype.visitExpression = function (expression) {
            var ExpressionVisitorFactory = require('./expression-visitor-factory');
            var expressionVisitorFactory = new ExpressionVisitorFactory();
            var expressionVisitor = expressionVisitorFactory.getExpressionView({model:expression, parent:this});
            expression.accept(expressionVisitor);
            log.debug('Visit Expression');
        };

        return TypeCastExpressionVisitor;
    });