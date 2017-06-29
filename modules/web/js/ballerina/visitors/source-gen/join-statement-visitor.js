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
import log from 'log';
import _ from 'lodash';
import AbstractStatementSourceGenVisitor from './abstract-statement-source-gen-visitor';
import StatementVisitorFactory from './statement-visitor-factory';

class JoinStatementVisitor extends AbstractStatementSourceGenVisitor {

    canVisitJoinStatement() {
        return true;
    }

    beginVisitJoinStatement(joinStatement) {
        this.node = joinStatement;
        const JOIN_CONDITION_KEY = 'joinCondition';
        this.appendSource('join' + joinStatement.getWSRegion(1) + '(' +
            joinStatement.getChildWSRegion(JOIN_CONDITION_KEY, 0) + joinStatement.getJoinConditionString() +
            joinStatement.getChildWSRegion(JOIN_CONDITION_KEY, 2) + ')' + joinStatement.getWSRegion(2) +
            '(' + joinStatement.getParameter().getWSRegion(0) +
            joinStatement.getParameter().getParameterDefinitionAsString() + ')' + joinStatement.getWSRegion(5) + '{' +
            joinStatement.getWSRegion(6));
        this.appendSource((joinStatement.whiteSpace.useDefault) ? this.getIndentation() : '');
        this.indent();
        log.debug('Begin Visit Join Statement');
    }

    endVisitJoinStatement(joinStatement) {
        this.appendSource('}' + joinStatement.getWSRegion(7));
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Join Statement');
    }


    visitStatement(statement) {
        if (!_.isEqual(this.node, statement)) {
            const statementVisitorFactory = new StatementVisitorFactory();
            const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        }
    }
}

export default JoinStatementVisitor;
