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
import log from "log";
import AbstractStatementSourceGenVisitor from "./abstract-statement-source-gen-visitor";
import StatementVisitorFactory from "./statement-visitor-factory";

class ForkJoinStatementVisitor extends AbstractStatementSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitForkJoinStatement(forkJoinStatement) {
        return true;
    }

    beginVisitForkJoinStatement(forkJoinStatement) {
        this.appendSource('fork (message m){');
        log.debug('Begin Visit Fork Statement');
    }

    endVisitForkJoinStatement(forkJoinStatement) {
        this.appendSource("}\n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Fork Statement');
    }


    visitStatement(statement) {
        let statementVisitorFactory = new StatementVisitorFactory();
        let statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
        // statement.accept(statementVisitor);
    }
}

export default ForkJoinStatementVisitor;
