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
import AbstractStatementSourceGenVisitor from './abstract-statement-source-gen-visitor';
import StatementVisitorFactory from './statement-visitor-factory';

class ElseIfStatementVisitor extends AbstractStatementSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitElseIfStatement(elseIfStatement) {
        return true;
    }

    beginVisitElseIfStatement(elseIfStatement) {
        this.node = elseIfStatement;
        this.appendSource('else' + elseIfStatement.getWSRegion(1) + 'if' + elseIfStatement.getWSRegion(2)
                            + '(' + elseIfStatement.getWSRegion(3) +  elseIfStatement.getConditionString()
                            + ')' + elseIfStatement.getWSRegion(4) + '{' + elseIfStatement.getWSRegion(5));
        this.appendSource((elseIfStatement.whiteSpace.useDefault) ? this.getIndentation() : '');
        this.indent();
        log.debug('Begin Visit Else If Statement Definition');
    }

    endVisitElseIfStatement(elseIfStatement) {
        this.outdent();
        // if using default ws, add a new line to end unless there are anymore elseif stmts available
        // or an else statement is available
        let tailingWS = (elseIfStatement.whiteSpace.useDefault
                            && (_.isNil(elseIfStatement.getParent().getElseStatement())
                                      && _.isEqual(_.last(elseIfStatement.getParent().getElseIfStatements()), elseIfStatement)))
                        ? '\n' : elseIfStatement.getWSRegion(6);
        this.appendSource('}' + tailingWS);
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Else If Statement Definition');
    }

    visitStatement(statement) {
        if(!_.isEqual(this.node, statement)) {
            var statementVisitorFactory = new StatementVisitorFactory();
            var statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        }
    }
}

export default ElseIfStatementVisitor;
