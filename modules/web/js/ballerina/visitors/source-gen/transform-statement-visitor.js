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

/**
 * Source generation visitor for Transform Statement
 */
class TransformStatementVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Can visit check for the Transform Statement
     * @return {boolean} true|false
     */
    canVisitTransformStatement() {
        return true;
    }

    /**
     * Begin visit for the resource definition
     * @param {TransformStatement} transformStatement - transform statement node
     */
    beginVisitTransformStatement(transformStatement) {
        this.node = transformStatement;

        const useDefaultWS = transformStatement.whiteSpace.useDefault;
        if (useDefaultWS) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation('\n' + this.getIndentation());
        }

        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        transformStatement.setLineNumber(lineNumber, { doSilently: true });

        let constructedSourceSegment = '';

        constructedSourceSegment += transformStatement.getStatementString()
                  + transformStatement.getWSRegion(1)
                  + '{' + transformStatement.getWSRegion(2) + ((useDefaultWS) ? this.getIndentation() : '');

        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        // Increase the total number of lines
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.indent();
    }

    /**
     * Visit Transform Statement
     */
    visitTransformStatement() {
    }

    /**
     * Visit resource level statements
     * @param {ASTNode} statement - Statement Node
     */
    visitStatement(statement) {
        if (!_.isEqual(this.node, statement)) {
            const statementVisitorFactory = new StatementVisitorFactory();
            const statementVisitor = statementVisitorFactory.getStatementVisitor(statement, this);
            statement.accept(statementVisitor);
        }
    }

    /**
     * End visit for resource definition source generation
     * @param {ASTNode} transformStatement - transform statement ASTNode
     */
    endVisitTransformStatement(transformStatement) {
        this.outdent();

        const constructedSourceSegment = '}' + transformStatement.getWSRegion(3)
            + ((transformStatement.whiteSpace.useDefault) ? this.currentPrecedingIndentation : '');
        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default TransformStatementVisitor;
