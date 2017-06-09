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

class CommittedStatementVisitor extends AbstractStatementSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitCommittedStatement(committedStatement) {
        log.debug('can visit CommittedStatementVisitor');
        return true;
    }

    beginVisitCommittedStatement(committedStatement) {
        log.debug('begin visit CommittedStatementVisitor');
        this.node = committedStatement;
        this.appendSource('committed' + committedStatement.getWSRegion(1) + '{' + committedStatement.getWSRegion(2));
        this.appendSource((committedStatement.whiteSpace.useDefault) ? this.getIndentation() : '');
        this.indent();
    }

    visitStatement(statement) {
        log.debug('visit CommittedStatementVisitor');
        if (!_.isEqual(this.node, statement)) {
            let statementVisitorFactory = new StatementVisitorFactory();
            let statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        }
    }

    endVisitCommittedStatement(committedStatement) {
        log.debug('end visit CommittedStatementVisitor');
        this.outdent();
        /*if using default ws, add a new line to end unless there are any
         aborted statement available*/
        let parent = committedStatement.getParent();
        let tailingWS = committedStatement.getWSRegion(3);
        if (committedStatement.whiteSpace.useDefault
            && (_.isEmpty(parent.getAbortedStatement()))) {
            tailingWS = '\n';
        } else {
            let abortedIndex = parent.children.indexOf(parent.getAbortedStatement());
            let committedIndex = parent.children.indexOf(committedStatement);
            if (abortedIndex < committedIndex) {
                tailingWS = '\n';
            } else {
                tailingWS = ' ';
            }
        }

        this.appendSource("}" + tailingWS);
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default CommittedStatementVisitor;