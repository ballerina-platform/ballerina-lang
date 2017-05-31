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

class TryStatementVisitor extends AbstractStatementSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitTryStatement(tryStatement) {
        return true;
    }

    beginVisitTryStatement(tryStatement) {
        this.node = tryStatement;
        /**
         * set the configuration start for the try statement
         * If we need to add additional parameters which are dynamically added to the configuration start
         * that particular source generation has to be constructed here
         */
        this.appendSource('try' + tryStatement.getWSRegion(1) + '{' + tryStatement.getWSRegion(2));
        this.appendSource((tryStatement.whiteSpace.useDefault) ? this.getIndentation() : '');
        this.indent();
        log.debug('Begin Visit Try Statement');
    }

    visitStatement(statement) {
        if(!_.isEqual(this.node, statement)) {
            let statementVisitorFactory = new StatementVisitorFactory();
            let statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        }
    }

    endVisitTryStatement(tryStatement) {
        this.outdent();
        this.appendSource('}' + tryStatement.getWSRegion(3));
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Try Statement');
    }
}

export default TryStatementVisitor;
