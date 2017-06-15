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
import AbstractStatementSourceGenVisitor from './abstract-statement-source-gen-visitor';
import StatementVisitorFactory from './statement-visitor-factory';

class AbortedStatementVisitor extends AbstractStatementSourceGenVisitor {

    canVisitAbortedStatement() {
        return true;
    }

    beginVisitAbortedStatement(abortedStatement) {
        this.node = abortedStatement;
        this.appendSource('aborted' + abortedStatement.getWSRegion(1) + '{'
            + abortedStatement.getWSRegion(2));
        this.appendSource((abortedStatement.whiteSpace.useDefault) ? this.getIndentation() : '');
        this.indent();
        log.debug('Begin Visit Aborted Statement');
    }

    visitStatement(statement) {
        if (!_.isEqual(this.node, statement)) {
            const statementVisitorFactory = new StatementVisitorFactory();
            const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        }
    }

    endVisitAbortedStatement(abortedStatement) {
        this.outdent();
        /* if using default ws, add a new line to end unless there are any
         committed statement available*/
        const parent = abortedStatement.getParent();
        let tailingWS = abortedStatement.getWSRegion(3);
        if (abortedStatement.whiteSpace.useDefault
            && (_.isEmpty(parent.getCommittedStatement()))) {
            tailingWS = '\n';
        } else {
            const abortedIndex = parent.children.indexOf(abortedStatement);
            const committedIndex = parent.children.indexOf(parent.getCommittedStatement());
            if (committedIndex < abortedIndex) {
                tailingWS = '\n';
            } else {
                tailingWS = ' ';
            }
        }

        this.appendSource('}' + tailingWS);
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Aborted Statement');
    }
}

export default AbortedStatementVisitor;
