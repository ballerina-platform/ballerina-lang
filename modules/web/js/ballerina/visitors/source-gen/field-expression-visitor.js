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
define(['require','lodash', 'log', 'event_channel', './abstract-expression-source-gen-visitor'],
    function(require, _, log, EventChannel, AbstractExpressionSourceGenVisitor) {

        var FieldExpressionVisitor = function(parent){
            AbstractExpressionSourceGenVisitor.call(this,parent);
        };

        FieldExpressionVisitor.prototype = Object.create(AbstractExpressionSourceGenVisitor.prototype);
        FieldExpressionVisitor.prototype.constructor = FieldExpressionVisitor;

        FieldExpressionVisitor.prototype.canVisitFieldExpression = function(expression){
            return true;
        };

        FieldExpressionVisitor.prototype.beginVisitFieldExpression = function(expression){
            this.appendSource('.');
            log.debug('Begin Visit Field Expression');
        };

        FieldExpressionVisitor.prototype.visitFieldExpression = function(expression){
            log.debug('Visit Field Expression');
        };

        FieldExpressionVisitor.prototype.endVisitFieldExpression = function(expression){
            this.getParent().appendSource(this.getGeneratedSource());
            log.debug('End Visit Field Expression');
        };

        FieldExpressionVisitor.prototype.visitExpression = function (expression) {
            var ExpressionVisitorFactory = require('./expression-visitor-factory');
            var expressionVisitorFactory = new ExpressionVisitorFactory();
            var expressionVisitor = expressionVisitorFactory.getExpressionView({model:expression, parent:this});
            expression.accept(expressionVisitor);
            log.debug('Visit Expression');
        };

        return FieldExpressionVisitor;
    });