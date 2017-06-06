/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import WhileStatement from '../../ast/statements/while-statement';
import StatementVisitorFactory from './statement-visitor-factory';

class WhileStatementVisitor extends AbstractStatementSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitWhileStatement(whileStatement) {
        return true;
    }

    beginVisitWhileStatement(whileStatement) {
        this.node = whileStatement;
        this.appendSource('\n' + this.getIndentation() + 'while (' + whileStatement.getCondition().getExpression() + ') {\n');
        this.indent();
        log.debug('Begin Visit If Statement Definition');
    }

    visitWhileStatement(whileStatement) {
        log.debug('Visit If Statement Definition');
    }

    visitStatement(statement) {
        if(!_.isEqual(this.node, statement)) {
            var statementVisitorFactory = new StatementVisitorFactory();
            var statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        }
    }

    endVisitWhileStatement(whileStatement) {
        this.outdent();
        this.appendSource(this.getIndentation() + "}\n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit If Statement Definition');
    }
}

export default WhileStatementVisitor;
