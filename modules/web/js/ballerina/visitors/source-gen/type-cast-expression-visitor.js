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
import ExpressionVisitorFactory from './expression-visitor-factory';

class TypeCastExpressionVisitor extends AbstractExpressionSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitTypeCastExpression(expression) {
        return true;
    }

    beginVisitTypeCastExpression(expression) {
        this.appendSource('(' + expression.getName() + ')');
        log.debug('Begin Visit Type Cast Expression');
    }

    visitTypeCastExpression(expression) {
        log.debug('Visit Ref Type Type Cast Expression');
    }

    endVisitTypeCastExpression(expression) {
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Type Cast Expression');
    }

    visitExpression(expression) {
        const expressionVisitorFactory = new ExpressionVisitorFactory();
        const expressionVisitor = expressionVisitorFactory.getExpressionView({ model: expression, parent: this });
        expression.accept(expressionVisitor);
        log.debug('Visit Expression');
    }
}

export default TypeCastExpressionVisitor;
