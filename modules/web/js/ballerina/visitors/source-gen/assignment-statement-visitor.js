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

import AbstractStatementSourceGenVisitor from './abstract-statement-source-gen-visitor';
import AssignmentStatement from '../../ast/statements/assignment-statement';
import ASTFactory from '../../ast/ballerina-ast-factory';
import FunctionDefinitionVisitor from './function-definition-visitor';

/**
 * Assignment statement source generation visitor
 */
class AssignmentStatementVisitor extends AbstractStatementSourceGenVisitor {

    /**
     * Check can visit for assignment statement
     * @param {AssignmentStatement} assignmentStatement - assignment statement ASTNode
     * @return {boolean} true|false whether can visit or not
     */
    canVisitAssignmentStatement(assignmentStatement) {
        return assignmentStatement instanceof AssignmentStatement;
    }

    /**
     * Begin visit for assignment statement
     * @param {AssignmentStatement} assignmentStatement - assignment statement ASTNode
     */
    beginVisitAssignmentStatement(assignmentStatement) {
        this.node = assignmentStatement;
        if (assignmentStatement.whiteSpace.useDefault) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(this.getIndentation());
        }

        // Calculate the line number
        const lineNumber = this.getTotalNumberOfLinesInSource() + 1;
        assignmentStatement.setLineNumber(lineNumber, { doSilently: true });
        const rightExpression = assignmentStatement.getRightExpression();
        // TODO: replace with
        if (ASTFactory.isLambdaExpression(rightExpression)) {
            const varStr = assignmentStatement.getIsDeclaredWithVar() ? 'var' + assignmentStatement.getWSRegion(1) : '';
            const leftStr = !_.isNil(assignmentStatement.getChildren()[0]) ?
                assignmentStatement.getLeftExpression().getExpressionString() : '';
            const spaceStr = ((!_.isNil(assignmentStatement.getLeftExpression()) &&
            !_.isEmpty(assignmentStatement.getLeftExpression().getChildren()) &&
            _.last(assignmentStatement.getLeftExpression().getChildren()).whiteSpace.useDefault) ? ' ' : '');
            const prefix = (varStr + leftStr + spaceStr) + '=' + assignmentStatement.getWSRegion(3);
            this.appendSource(prefix);
            const child = rightExpression.children[0];
            child.accept(new FunctionDefinitionVisitor(this));
        } else {
            const constructedSourceSegment = assignmentStatement.getStatementString();
            const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);

            // Increase the total number of lines
            this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
            this.appendSource(constructedSourceSegment);
        }
    }

    /**
     * End visit for assignment statement source generation
     * @param {AssignmentStatement} assignmentStatement - assignment statement ASTNode
     */
    endVisitAssignmentStatement(assignmentStatement) {
        const constructedSourceSegment = ';' + assignmentStatement.getWSRegion(4)
            + ((assignmentStatement.whiteSpace.useDefault) ? this.currentPrecedingIndentation : '');

        // Add the increased number of lines
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);

        this.appendSource(constructedSourceSegment);
        const generatedSource = this.getGeneratedSource();
        this.getParent().appendSource(generatedSource);

        assignmentStatement.getViewState().source = generatedSource;
    }
}

export default AssignmentStatementVisitor;
