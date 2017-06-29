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

class TimeoutStatementVisitor extends AbstractStatementSourceGenVisitor {

    canVisitTimeoutStatement() {
        return true;
    }

    beginVisitTimeoutStatement(timeoutStatement) {
        this.node = timeoutStatement;

        this.appendSource('timeout' + timeoutStatement.getWSRegion(1) + '(' + timeoutStatement.getWSRegion(2) +
            timeoutStatement.getExpression() + ')' + timeoutStatement.getWSRegion(3) +
            '(' + timeoutStatement.getParameter().getWSRegion(0) +
            timeoutStatement.getParameter().getParameterDefinitionAsString() +
            ')' + timeoutStatement.getWSRegion(6) +
            '{' + timeoutStatement.getWSRegion(7));
        log.debug('Begin Visit Timeout Statement');
    }

    endVisitTimeoutStatement(timeoutStatement) {
        this.appendSource('}' + timeoutStatement.getWSRegion(8));
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Timeout Statement');
    }


    visitStatement(statement) {
        if (!_.isEqual(this.node, statement)) {
            const statementVisitorFactory = new StatementVisitorFactory();
            const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        }
    }
}

export default TimeoutStatementVisitor;
