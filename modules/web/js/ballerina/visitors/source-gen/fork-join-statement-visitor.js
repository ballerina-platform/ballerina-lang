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
import WorkerDeclarationVisitor from './worker-declaration-visitor';

class ForkJoinStatementVisitor extends AbstractStatementSourceGenVisitor {

    canVisitForkJoinStatement() {
        return true;
    }

    beginVisitForkJoinStatement(forkJoinStatement) {
        this.node = forkJoinStatement;
        if (forkJoinStatement.whiteSpace.useDefault) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(this.getIndentation());
        }
        this.appendSource('fork' + forkJoinStatement.getWSRegion(1) + '{' + forkJoinStatement.getWSRegion(2));
        this.indent();
    }

    visitWorkerDeclaration(workerDeclaration) {
        const workerDeclarationVisitor = new WorkerDeclarationVisitor(this);
        workerDeclaration.accept(workerDeclarationVisitor);
    }

    endVisitForkJoinStatement(forkJoinStatement) {
        this.appendSource((forkJoinStatement.whiteSpace.useDefault) ? this.currentPrecedingIndentation : '');
        this.getParent().appendSource(this.getGeneratedSource());
    }


    visitStatement(statement) {
        if (!_.isEqual(this.node, statement)) {
            const forkJoinStatement = statement.getParent();
            this.outdent();
            if (forkJoinStatement.whiteSpace.useDefault) {
                this.appendSource(this.getIndentation());
            }
            this.appendSource('}' + statement.getWSRegion(0));
            const statementVisitorFactory = new StatementVisitorFactory();
            const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        }
    }
}

export default ForkJoinStatementVisitor;
