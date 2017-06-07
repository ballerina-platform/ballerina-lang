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
import AbstractStatementSourceGenVisitor from './abstract-statement-source-gen-visitor';
import AssignmentStatement from '../../ast/statements/assignment-statement';
import LeftOperandExpressionVisitor from './left-operand-expression-visitor';
import RightOperandExpressionVisitor from './right-operand-expression-visitor';

class AssignmentStatementVisitor extends AbstractStatementSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitAssignmentStatement(assignmentStatement) {
        return assignmentStatement instanceof AssignmentStatement;
    }

    beginVisitAssignmentStatement(assignmentStatement) {
        this.node = assignmentStatement;
        if (assignmentStatement.whiteSpace.useDefault) {
            this.currentPrecedingIndentation = this.getCurrentPrecedingIndentation();
            this.replaceCurrentPrecedingIndentation(this.getIndentation());
        }
        if (!_.isNil(assignmentStatement.children[0])) {
            this.visitLeftOperandExpression(assignmentStatement.children[0]);
        }
        if (!_.isNil(assignmentStatement.children[1])) {
            this.visitRightOperandExpression(assignmentStatement.children[1]);
        }
    }

    visitLeftOperandExpression(expression) {
        var leftExpVisitor = new LeftOperandExpressionVisitor(this);
        expression.accept(leftExpVisitor);
    }

    visitRightOperandExpression(expression) {
        this.appendSource('=' + this.node.getWSRegion(2));
        var rightExpVisitor = new RightOperandExpressionVisitor(this);
        expression.accept(rightExpVisitor);
    }

    endVisitAssignmentStatement(assignmentStatement) {
        this.appendSource(';' + assignmentStatement.getWSRegion(3));
        this.appendSource((assignmentStatement.whiteSpace.useDefault)
            ? this.currentPrecedingIndentation : '');
        this.getParent().appendSource(this.getGeneratedSource());
    }
}

export default AssignmentStatementVisitor;
