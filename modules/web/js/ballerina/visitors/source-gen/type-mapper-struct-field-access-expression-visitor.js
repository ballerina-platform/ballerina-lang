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
define(['require','lodash', 'log', 'event_channel', './abstract-expression-source-gen-visitor', 
        '../../ast/struct-field-access-expression', '../../ast/left-operand-expression'],
    function(require, _, log, EventChannel, AbstractExpressionSourceGenVisitor, StructFieldAccessExpression,
             LeftOperandExpression) {

        var TypeMapperSructFieldAccessExpressionVisitor = function(parent){
            AbstractExpressionSourceGenVisitor.call(this,parent);
        };

        TypeMapperSructFieldAccessExpressionVisitor.prototype = Object.create(AbstractExpressionSourceGenVisitor.prototype);
        TypeMapperSructFieldAccessExpressionVisitor.prototype.constructor = TypeMapperSructFieldAccessExpressionVisitor;

        TypeMapperSructFieldAccessExpressionVisitor.prototype.canVisitStructFieldAccessExpression = function(expression){
            return expression instanceof StructFieldAccessExpression && this._generatedSource === "";
        };

        TypeMapperSructFieldAccessExpressionVisitor.prototype.beginVisitStructFieldAccessExpression = function(expression){
            if(expression.getParent() instanceof StructFieldAccessExpression){
                this.appendSource('.');
            }
            log.debug('Begin Visit Type Mapper Struct Field Access Expression');
        };

        TypeMapperSructFieldAccessExpressionVisitor.prototype.visitStructFieldAccessExpression = function(expression){
            log.debug('Visit Type Mapper Struct Field Access Expression');
        };

        TypeMapperSructFieldAccessExpressionVisitor.prototype.endVisitStructFieldAccessExpression = function(expression){
            this.getParent().appendSource(this.getGeneratedSource());
            log.debug('End Visit Type Mapper Struct Field Access Expression');
        };

        TypeMapperSructFieldAccessExpressionVisitor.prototype.visitExpression = function (expression) {
            var ExpressionVisitorFactory = require('./type-mapper-expression-visitor-factory');
            var expressionVisitorFactory = new ExpressionVisitorFactory();
            var expressionVisitor = expressionVisitorFactory.getExpressionVisitor({model:expression, parent:this});
            expression.accept(expressionVisitor);
            log.debug('Visit Type Mapper Expression');
        };

        return TypeMapperSructFieldAccessExpressionVisitor;
    });