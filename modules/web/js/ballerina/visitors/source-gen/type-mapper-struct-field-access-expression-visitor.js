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
import AbstractExpressionSourceGenVisitor from './abstract-expression-source-gen-visitor';
import StructFieldAccessExpression from '../../ast/expressions/struct-field-access-expression';
import LeftOperandExpression from '../../ast/statements/left-operand-expression';
import ExpressionVisitorFactory from './type-mapper-expression-visitor-factory';

class TypeMapperSructFieldAccessExpressionVisitor extends AbstractExpressionSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitStructFieldAccessExpression(expression) {
        return expression instanceof StructFieldAccessExpression && this._generatedSource === "";
    }

    beginVisitStructFieldAccessExpression(expression) {
        if(expression.getParent() instanceof StructFieldAccessExpression){
            this.appendSource('.');
        }
        log.debug('Begin Visit Type Mapper Struct Field Access Expression');
    }

    visitStructFieldAccessExpression(expression) {
        log.debug('Visit Type Mapper Struct Field Access Expression');
    }

    endVisitStructFieldAccessExpression(expression) {
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Type Mapper Struct Field Access Expression');
    }

    visitExpression(expression) {
        var expressionVisitorFactory = new ExpressionVisitorFactory();
        var expressionVisitor = expressionVisitorFactory.getExpressionVisitor({model:expression, parent:this});
        expression.accept(expressionVisitor);
        log.debug('Visit Type Mapper Expression');
    }
}

export default TypeMapperSructFieldAccessExpressionVisitor;
