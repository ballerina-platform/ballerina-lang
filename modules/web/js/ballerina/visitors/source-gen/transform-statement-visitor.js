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

class TransformStatementVisitor extends AbstractStatementSourceGenVisitor {

    canVisitTransformStatement() {
        return true;
    }

    beginVisitTransformStatement(transformStatement) {
        this.node = transformStatement;

        const useDefaultWS = transformStatement.whiteSpace.useDefault;
        if (useDefaultWS) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation('\n' + this.getIndentation());
        }

        let constructedSourceSegment = '';

        constructedSourceSegment += transformStatement.getStatementString()
                  + transformStatement.getWSRegion(1)
                  + '{' + transformStatement.getWSRegion(2);
        this.appendSource(constructedSourceSegment);
        this.appendSource((useDefaultWS) ? this.getIndentation() : '');
        this.indent();
        log.debug('Begin Visit TransformStatement');
    }

    visitTransformStatement() {
        log.debug('Visit TransformStatement');
    }

    visitStatement(statement) {
        if (!_.isEqual(this.node, statement)) {
            const statementVisitorFactory = new StatementVisitorFactory();
            const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        }
    }

    endVisitTransformStatement(transformStatement) {
        this.outdent();
        this.appendSource('}' + transformStatement.getWSRegion(3));
        this.appendSource((transformStatement.whiteSpace.useDefault) ?
                      this.currentPrecedingIndentation : '');
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit TransformStatement');
    }
}

export default TransformStatementVisitor;
