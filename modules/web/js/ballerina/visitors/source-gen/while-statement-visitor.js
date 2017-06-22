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
import AbstractStatementSourceGenVisitor from './abstract-statement-source-gen-visitor';
import StatementVisitorFactory from './statement-visitor-factory';

class WhileStatementVisitor extends AbstractStatementSourceGenVisitor {

    canVisitWhileStatement() {
        if (_.isNil(this.node)) {
            return true;
        }
        return false;
    }

    beginVisitWhileStatement(whileStatement) {
        this.node = whileStatement;
        if (whileStatement.whiteSpace.useDefault) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(this.getIndentation());
        }
        this.appendSource('while' + whileStatement.getWSRegion(1) + '(' 
            + whileStatement.getWSRegion(2) + whileStatement.getConditionString() + ')' 
            + whileStatement.getWSRegion(3) + '{' + whileStatement.getWSRegion(4));
        this.appendSource((whileStatement.whiteSpace.useDefault) ? this.getIndentation() : '');
        this.indent();
    }

    visitWhileStatement() {
    }

    visitStatement(statement) {
        if (!_.isEqual(this.node, statement)) {
            const statementVisitorFactory = new StatementVisitorFactory();
            const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        }
    }

    endVisitWhileStatement(whileStatement) {
        this.outdent();
        this.appendSource('}' + whileStatement.getWSRegion(5));
        this.appendSource((whileStatement.whiteSpace.useDefault)
          ? this.currentPrecedingIndentation : '');
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default WhileStatementVisitor;
