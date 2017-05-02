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
import ReturnStatement from '../../ast/statements/return-statement';
import TypeMapperExpressionVisitorFactory from './type-mapper-expression-visitor-factory';

class TypeMapperReturnStatementVisitor extends AbstractStatementSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitReturnStatement(returnStatement) {
        return returnStatement instanceof ReturnStatement;
    }

    canVisitExpression(expression) {
        return true;
    }

    beginVisitReturnStatement(returnStatement) {
        /**
         * set the configuration start for the reply statement definition language construct
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        this.appendSource(returnStatement.getReturnExpression());
        log.debug('Begin Visit Type Mapper Return Statement Definition');
    }

    endVisitReturnStatement(returnStatement) {
        this.appendSource(";\n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Type Mapper Return Statement Definition');
    }

    visitExpression(expression) {
        var expressionVisitorFactory = new TypeMapperExpressionVisitorFactory();
        var expressionVisitor = expressionVisitorFactory.getExpressionVisitor({model: expression, parent: this});
        expression.accept(expressionVisitor);
        log.debug('Visit Expression');
    }
}

export default TypeMapperReturnStatementVisitor;
