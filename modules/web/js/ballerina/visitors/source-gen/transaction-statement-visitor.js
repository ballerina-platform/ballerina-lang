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
import AbstractStatementSourceGenVisitor from './abstract-statement-source-gen-visitor';
import StatementVisitorFactory from './statement-visitor-factory';

class TransactionStatementVisitor extends AbstractStatementSourceGenVisitor {

    canVisitTransactionStatement() {
        return true;
    }

    beginVisitTransactionStatement(transactionStatement) {
        this.node = transactionStatement;
        this.appendSource('transaction' + transactionStatement.getWSRegion(1) + '{'
            + transactionStatement.getWSRegion(2));
        this.appendSource((transactionStatement.whiteSpace.useDefault) ? this.getIndentation() : '');
        this.indent();
    }

    visitStatement(statement) {
        if (!_.isEqual(this.node, statement)) {
            const statementVisitorFactory = new StatementVisitorFactory();
            const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        }
    }

    endVisitTransactionStatement(transactionStatement) {
        this.outdent();
        /* if using default ws, add a new line to end unless there are any aborted
         or committed statement available*/
        const parent = transactionStatement.getParent();
        const tailingWS = (transactionStatement.whiteSpace.useDefault
        && (_.isEmpty(parent.getAbortedStatement())
        && _.isEmpty(parent.getCommittedStatement())))
            ? '\n' : transactionStatement.getWSRegion(3);
        this.appendSource('}' + tailingWS);
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default TransactionStatementVisitor;
