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

class IfStatementVisitor extends AbstractStatementSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitIfStatement(ifStatement) {
        return true;
    }

    beginVisitIfStatement(ifStatement) {
        this.node = ifStatement;
        this.appendSource('if' + ifStatement.getWSRegion(1) + '(' + ifStatement.getWSRegion(2));
        this.appendSource((!_.isNil(ifStatement.getCondition())) ? ifStatement.getConditionString() : '');
        this.appendSource(')' + ifStatement.getWSRegion(3) + '{' + ifStatement.getWSRegion(4));
        this.appendSource((ifStatement.whiteSpace.useDefault) ? this.getIndentation() : '');
        this.indent();
        log.debug('Begin Visit If Statement Definition');
    }

    visitStatement(statement) {
        if(!_.isEqual(this.node, statement)) {
            var statementVisitorFactory = new StatementVisitorFactory();
            var statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        }
    }

    endVisitIfStatement(ifStatement) {
        this.outdent();
        // if using default ws, add a new line to end unless there are any elseif stmts available
        // or an else statement is available
        let tailingWS = (ifStatement.whiteSpace.useDefault
                            && (_.isEmpty(ifStatement.getParent().getElseIfStatements())
                                      && _.isNil(ifStatement.getParent().getElseStatement())))
                        ? '\n' : ifStatement.getWSRegion(5);
        this.appendSource('}' + tailingWS);
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit If Statement Definition');
    }
}

export default IfStatementVisitor;
