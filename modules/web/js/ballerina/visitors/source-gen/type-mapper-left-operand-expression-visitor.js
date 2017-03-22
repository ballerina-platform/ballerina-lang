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
import TypeMapperExpressionVisitorFactory from './type-mapper-expression-visitor-factory';

class TypeMapperLeftOperandExpressionVisitor extends AbstractStatementSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitLeftOperandExpression(leftOperandExpression) {
        return true;
    }

    beginVisitLeftOperandExpression(leftOperandExpression) {
        log.debug('Begin Visit Type Mapper Left Operand Expression');
    }

    endVisitLeftOperandExpression(leftOperandExpression) {
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Type Mapper Left Operand Expression');
    }

    visitExpression(expression) {
        var expressionVisitorFactory = new TypeMapperExpressionVisitorFactory();
        var expressionVisitor = expressionVisitorFactory.getExpressionVisitor({model: expression, parent: this});
        expression.accept(expressionVisitor);
        log.debug('Visit Type Mapper Expression');
    }
}

export default TypeMapperLeftOperandExpressionVisitor;
