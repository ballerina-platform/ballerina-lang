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
import _ from 'lodash';
import log from 'log';
import EventChannel from 'event_channel';
import AbstractStatementSourceGenVisitor from './abstract-statement-source-gen-visitor';
import FunctionInvocationExpression from '../../ast/expressions/function-invocation-expression';
import StructFieldAccessExpression from '../../ast/expressions/struct-field-access-expression';
import $____type_mapper_expression_visitor_factory from './type-mapper-expression-visitor-factory';

/**
 * Constructor for Function invocation expression visitor
 * @param {ASTNode} parent - parent node
 * @constructor
 */
class TypeMapperFunctionInvocationExpressionVisitor extends AbstractStatementSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitFuncInvocationExpression(functionInvocation) {
        //This visitor has already visited the indended function invocation expression
        // , and hence no need to visit again.
        return true && this._generatedSource === "";
    }

    canVisitExpression(expression) {
        return !this.getGeneratedSource();
    }

    beginVisitFuncInvocationExpression(functionInvocation) {
        var source = "";
        if (!_.isNil(functionInvocation.getPackageName()) && !_.isEmpty(functionInvocation.getPackageName())
            && !_.isEqual(functionInvocation.getPackageName(), 'Current Package')) {
            source += functionInvocation.getPackageName() + ":";
        }
        source += functionInvocation.getFunctionName() + '(';
        this.appendSource(source);
        log.debug('Begin Visit Type Mapper Function Invocation expression - ' + functionInvocation.getFunctionalExpression());
    }

    visitFuncInvocationExpression(functionInvocation) {
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
    }

    endVisitFuncInvocationExpression(functionInvocation) {
        this.appendSource(')');
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Type Mapper Function Invocation expression - ' + functionInvocation.getFunctionalExpression());
    }

    beginVisitExpression(expression) {
        log.debug('Begin visit expression ');
    }

    endVisitExpression(expression) {
        log.debug('End visit Type Mapper expression');
    }

    visitExpression(expression) {
        var parent = expression.getParent();
        var index = _.findIndex(parent.getChildren(), function (aExp) {
            return aExp === expression;
        });
        if (index !== 0) {
            this.appendSource(',');
        }
        var ExpressionVisitorFactory = $____type_mapper_expression_visitor_factory;
        var expressionVisitorFactory = new ExpressionVisitorFactory();
        var expressionVisitor = expressionVisitorFactory.getExpressionVisitor({model: expression, parent: this});
        expression.accept(expressionVisitor);
        log.debug('Visit Type Mapper Struct Field Access Expression');
    }
}

export default TypeMapperFunctionInvocationExpressionVisitor;
