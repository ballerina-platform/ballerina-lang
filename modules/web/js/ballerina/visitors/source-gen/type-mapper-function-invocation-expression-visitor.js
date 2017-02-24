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
define(['require', 'lodash', 'log', 'event_channel', './abstract-statement-source-gen-visitor', '../../ast/function-invocation-expression',
        '../../ast/struct-field-access-expression'],
    function (require, _, log, EventChannel, AbstractStatementSourceGenVisitor, FunctionInvocationExpression, StructFieldAccessExpression) {

        /**
         * Constructor for Function invocation expression visitor
         * @param {ASTNode} parent - parent node
         * @constructor
         */
        var TypeMapperFunctionInvocationExpressionVisitor = function (parent) {
            AbstractStatementSourceGenVisitor.call(this, parent);
        };

        TypeMapperFunctionInvocationExpressionVisitor.prototype = Object.create(AbstractStatementSourceGenVisitor.prototype);
        TypeMapperFunctionInvocationExpressionVisitor.prototype.constructor = TypeMapperFunctionInvocationExpressionVisitor;

        TypeMapperFunctionInvocationExpressionVisitor.prototype.canVisitFuncInvocationExpression = function (functionInvocation) {
            //This visitor has already visited the indended function invocation expression
            // , and hence no need to visit again.
            return true && this._generatedSource === "";
        };

        TypeMapperFunctionInvocationExpressionVisitor.prototype.canVisitExpression = function (expression) {
            return !this.getGeneratedSource();
        };

        TypeMapperFunctionInvocationExpressionVisitor.prototype.beginVisitFuncInvocationExpression = function (functionInvocation) {
            this.appendSource(functionInvocation.getFunctionName() + '(');
            log.debug('Begin Visit Type Mapper Function Invocation expression - ' + functionInvocation.getFunctionalExpression());
        };

        TypeMapperFunctionInvocationExpressionVisitor.prototype.visitFuncInvocationExpression = function (functionInvocation) {
            var parent = functionInvocation.getParent();
            var index = _.findIndex(parent.getChildren(), function (aExp) {
                return aExp === functionInvocation;
            });
            if (index !== 0) {
                this.appendSource(',');
            }
            var args = {model: functionInvocation, parent: this};
            functionInvocation.accept(new TypeMapperFunctionInvocationExpressionVisitor(_.get(args, "parent")));
            log.debug('Visit Type Mapper Function Invocation expression');
        };

        TypeMapperFunctionInvocationExpressionVisitor.prototype.endVisitFuncInvocationExpression = function (functionInvocation) {
            this.appendSource(')');
            this.getParent().appendSource(this.getGeneratedSource());
            log.debug('End Visit Type Mapper Function Invocation expression - ' + functionInvocation.getFunctionalExpression());
        };

        TypeMapperFunctionInvocationExpressionVisitor.prototype.beginVisitExpression = function (expression) {
            log.debug('Begin visit expression ');
        };

        TypeMapperFunctionInvocationExpressionVisitor.prototype.endVisitExpression = function (expression) {
            log.debug('End visit Type Mapper expression');
        };

        TypeMapperFunctionInvocationExpressionVisitor.prototype.visitExpression = function (expression) {
            var parent = expression.getParent();
            var index = _.findIndex(parent.getChildren(), function (aExp) {
                return aExp === expression;
            });
            if (index !== 0) {
                this.appendSource(',');
            }
            var ExpressionVisitorFactory = require('./type-mapper-expression-visitor-factory');
            var expressionVisitorFactory = new ExpressionVisitorFactory();
            var expressionVisitor = expressionVisitorFactory.getExpressionVisitor({model: expression, parent: this});
            expression.accept(expressionVisitor);
            log.debug('Visit Type Mapper Struct Field Access Expression');
        };

        return TypeMapperFunctionInvocationExpressionVisitor;
    });