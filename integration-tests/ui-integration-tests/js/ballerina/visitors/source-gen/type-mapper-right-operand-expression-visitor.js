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
define(['require', 'lodash', 'log', 'event_channel', './abstract-statement-source-gen-visitor', '../../ast/module',
        './type-mapper-expression-visitor-factory', './type-mapper-function-invocation-expression-visitor'],
    function (require, _, log, EventChannel, AbstractStatementSourceGenVisitor, AST, TypeMapperExpressionVisitorFactory,
              FunctionInvocationExpressionVisitor) {

        var TypeMapperRightOperandExpressionVisitor = function (parent) {
            AbstractStatementSourceGenVisitor.call(this, parent);
        };

        TypeMapperRightOperandExpressionVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
        TypeMapperRightOperandExpressionVisitor.prototype.constructor = TypeMapperRightOperandExpressionVisitor;

        TypeMapperRightOperandExpressionVisitor.prototype.canVisitRightOperandExpression = function (rightOperandExpression) {
            return true;
        };

        TypeMapperRightOperandExpressionVisitor.prototype.beginVisitRightOperandExpression = function (rightOperandExpression) {
            this.appendSource(" = ");
            log.debug('Begin Visit Type Mapper Right Operand Expression');
        };

        TypeMapperRightOperandExpressionVisitor.prototype.endVisitRightOperandExpression = function (rightOperandExpression) {
            this.getParent().appendSource(this.getGeneratedSource());
            log.debug('End Visit Type Mapper Right Operand Expression');
        };

        TypeMapperRightOperandExpressionVisitor.prototype.visitFuncInvocationStatement = function (statement) {
            var StatementVisitorFactory = require('./type-mapper-statement-visitor-factory');
            var statementVisitorFactory = new StatementVisitorFactory();
            var statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        };

        TypeMapperRightOperandExpressionVisitor.prototype.visitExpression = function (expression) {
            var expressionVisitorFactory = new TypeMapperExpressionVisitorFactory();
            var expressionVisitor = expressionVisitorFactory.getExpressionVisitor({model: expression, parent: this});
            expression.accept(expressionVisitor);
            log.debug('Visit Type Mapper Expression');
        };

        TypeMapperRightOperandExpressionVisitor.prototype.visitFuncInvocationExpression = function (functionInvocation) {
            var args = {model: functionInvocation, parent: this};
            functionInvocation.accept(new FunctionInvocationExpressionVisitor(_.get(args, "parent")));
            log.debug('Visit Type Mapper Function Invocation expression');
        };

        return TypeMapperRightOperandExpressionVisitor;
    });