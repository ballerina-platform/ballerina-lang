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
import AbstractStatementSourceGenVisitor from './abstract-statement-source-gen-visitor';
import AbortStatement from '../../ast/statements/abort-statement';

class AbortStatementVisitor extends AbstractStatementSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    /**
     * Can visit Abort Statement.
     * @param {AbortStatement} abortStatement
     * @return {boolean} true if instance of AbortStatement, else false.
     * */
    canVisitAbortStatement(abortStatement) {
        return abortStatement instanceof AbortStatement;
    }

    /**
     * Begin Visit Abort Statement.
     * @param {AbortStatement} abortStatement
     * */
    beginVisitAbortStatement(abortStatement) {
        this.appendSource(abortStatement.getStatement());
        log.debug('Begin Visit Abort Statement');
    }

    /**
     * Visit Abort Statement.
     * @param {AbortStatement} abortStatement
     * */
    visitAbortStatement(abortStatement) {
        log.debug('visit Abort Statement');
    }

    /**
     * End visit Abort Statement.
     * @param {AbortStatement} abortStatement
     * */
    endVisitAbortStatement(abortStatement) {
        this.appendSource(";\n");
        this.getParent().appendSource("\n" + this.getIndentation() + this.getGeneratedSource());
        log.debug('End Visit Abort Statement');
    }
}

export default AbortStatementVisitor;