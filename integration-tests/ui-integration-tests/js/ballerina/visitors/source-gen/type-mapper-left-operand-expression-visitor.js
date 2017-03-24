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
define(['require', 'lodash', 'log', 'event_channel', './abstract-statement-source-gen-visitor', './type-mapper-expression-visitor-factory'],
    function (require, _, log, EventChannel, AbstractStatementSourceGenVisitor, TypeMapperExpressionVisitorFactory) {

        var TypeMapperLeftOperandExpressionVisitor = function (parent) {
            AbstractStatementSourceGenVisitor.call(this, parent);
        };

        TypeMapperLeftOperandExpressionVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
        TypeMapperLeftOperandExpressionVisitor.prototype.constructor = TypeMapperLeftOperandExpressionVisitor;

        TypeMapperLeftOperandExpressionVisitor.prototype.canVisitLeftOperandExpression = function (leftOperandExpression) {
            return true;
        };

        TypeMapperLeftOperandExpressionVisitor.prototype.beginVisitLeftOperandExpression = function (leftOperandExpression) {
            log.debug('Begin Visit Type Mapper Left Operand Expression');
        };

        TypeMapperLeftOperandExpressionVisitor.prototype.endVisitLeftOperandExpression = function (leftOperandExpression) {
            this.getParent().appendSource(this.getGeneratedSource());
            log.debug('End Visit Type Mapper Left Operand Expression');
        };

        TypeMapperLeftOperandExpressionVisitor.prototype.visitExpression = function (expression) {
            var expressionVisitorFactory = new TypeMapperExpressionVisitorFactory();
            var expressionVisitor = expressionVisitorFactory.getExpressionVisitor({model: expression, parent: this});
            expression.accept(expressionVisitor);
            log.debug('Visit Type Mapper Expression');
        };

        return TypeMapperLeftOperandExpressionVisitor;
    });