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
import ActionInvocationStatementVisitor from './action-invocation-statement-visitor';
import FunctionInvocationExpressionVisitor from './function-invocation-expression-visitor'

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
        const leftExpression = assignmentStatement.getLeftExpression();

        const varStr = assignmentStatement.getIsDeclaredWithVar() ? 'var ' : '';
        const leftStr = !_.isNil(leftExpression) ? leftExpression.getExpressionString() : '';
        const spaceStr = ((!_.isNil(leftExpression) && !_.isEmpty(leftExpression.getChildren()) &&
        _.last(leftExpression.getChildren()).whiteSpace.useDefault) ? ' ' : '');
        const prefix = (varStr + leftStr + spaceStr) + '=' + assignmentStatement.getWSRegion(3);
        this.appendSource(prefix);
        let constructedSourceSegment = prefix;
        if (ASTFactory.isLambdaExpression(rightExpression)) {
            const child = rightExpression.children[0];
            child.accept(new FunctionDefinitionVisitor(this));
        } else if (ASTFactory.isActionInvocationExpression(rightExpression)) {
            rightExpression.accept(new ActionInvocationStatementVisitor(this));
        } else if (ASTFactory.isFunctionInvocationExpression(rightExpression)) {
            // TODO: remove this by moving all visitors to functions.
        } else if (!_.isNil(rightExpression)) {
            const expressionStr = rightExpression.getExpressionString();
            constructedSourceSegment += expressionStr;
            this.appendSource(expressionStr);
        }
        const numberOfNewLinesAdded = this.getEndLinesInSegment(constructedSourceSegment);
        this.increaseTotalSourceLineCountBy(numberOfNewLinesAdded);
    }

    visitFuncInvocationExpression(node) {
        node.accept(new FunctionInvocationExpressionVisitor(this));
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
