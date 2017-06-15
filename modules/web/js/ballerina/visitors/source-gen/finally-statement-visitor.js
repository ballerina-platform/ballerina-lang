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

class FinallyStatementVisitor extends AbstractStatementSourceGenVisitor {

    canVisitFinallyStatement() {
        return true;
    }

    beginVisitFinallyStatement(finallyStatement) {
        this.node = finallyStatement;
        /**
         * set the configuration start for the finally statement
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        this.appendSource('finally' + finallyStatement.getWSRegion(1) + '{' + finallyStatement.getWSRegion(2));
        this.appendSource((finallyStatement.whiteSpace.useDefault) ? this.getIndentation() : '');
        this.indent();
    }

    visitStatement(statement) {
        if (!_.isEqual(this.node, statement)) {
            const statementVisitorFactory = new StatementVisitorFactory();
            const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        }
    }

    endVisitFinallyStatement(finallyStatement) {
        this.outdent();
        this.appendSource('}' + finallyStatement.getWSRegion(3));
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default FinallyStatementVisitor;
