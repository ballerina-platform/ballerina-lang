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

/**
 * Constructor for Function invocation expression visitor
 * @param {ASTNode} parent - parent node
 * @constructor
 */
class FunctionInvocationExpressionVisitor extends AbstractStatementSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitFuncInvocationExpression(functionInvocation) {
        return true;
    }

    beginVisitFuncInvocationExpression(functionInvocation) {
        var source = functionInvocation.getFunctionalExpression();
        this.appendSource(source);
        log.debug('Begin Visit Function Invocation expression');
    }

    visitFuncInvocationExpression(functionInvocation) {
        log.debug('Visit Function Invocation expression');
    }

    endVisitFuncInvocationExpression(functionInvocation) {
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Function Invocation expression');
    }
}

FunctionInvocationExpressionVisitor.prototype.constructor = FunctionInvocationExpression;

export default FunctionInvocationExpressionVisitor;
