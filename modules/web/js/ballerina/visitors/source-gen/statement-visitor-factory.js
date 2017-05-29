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
import ASTFactory from '../../ast/ballerina-ast-factory';
import TryCatchStatementVisitor from './try-catch-statement-visitor';
import TryStatementVisitor from './try-statement-visitor';
import CatchStatementVisitor from './catch-statement-visitor';
import IfElseStatementVisitor from './if-else-statement-visitor';
import IfStatementVisitor from './if-statement-visitor';
import ElseStatementVisitor from './else-statement-visitor';
import ElseIfStatementVisitor from './else-if-statement-visitor';
import WhileStatementVisitor from './while-statement-visitor';
import AssignmentStatementVisitor from './assignment-statement-visitor';
import TransformStatementVisitor from './transform-statement-visitor';
import ActionInvocationStatementVisitor from './action-invocation-statement-visitor';
import ReplyStatementVisitor from './reply-statement-visitor';
import ReturnStatementVisitor from './return-statement-visitor';
import FunctionInvocationVisitor from './function-invocation-visitor';
import AssignmentVisitor from './assignment-visitor';
import LeftOperandExpressionVisitor from './left-operand-expression-visitor';
import RightOperandExpressionVisitor from './right-operand-expression-visitor';
import VariableDefinitionStatement from './variable-definition-statement-visitor';
import WorkerInvocationStatementVisitor from './worker-invocation-statement-visitor';
import WorkerReplyStatementVisitor from './worker-reply-statement-visitor';
import BreakStatementVisitor from './break-statement-visitor';
import ThrowStatementVisitor from './throw-statement-visitor';
import CommentStatementVisitor from './comment-statement-visitor';
import TransactionAbortedStatementVisitor from './transaction-aborted-statement-visitor';
import TransactionStatementVisitor from './transaction-statement-visitor';
import AbortedStatementVisitor from './aborted-statement-visitor';
import AbortStatementVisitor from './abort-statement-visitor';

class StatementVisitorFactor {
    getStatementVisitor(statement, parent) {
        if (ASTFactory.isTryCatchStatement(statement)) {
            return new TryCatchStatementVisitor(parent);
        } else if (ASTFactory.isTryStatement(statement)) {
            return new TryStatementVisitor(parent);
        } else if (ASTFactory.isCatchStatement(statement)) {
            return new CatchStatementVisitor(parent);
        } else if (ASTFactory.isIfElseStatement(statement)) {
            return new IfElseStatementVisitor(parent);
        } else if (ASTFactory.isIfStatement(statement)) {
            return new IfStatementVisitor(parent);
        } else if (ASTFactory.isElseStatement(statement)) {
            return new ElseStatementVisitor(parent);
        } else if (ASTFactory.isElseIfStatement(statement)) {
            return new ElseIfStatementVisitor(parent);
        } else if (ASTFactory.isWhileStatement(statement)) {
            return new WhileStatementVisitor(parent);
        } else if (ASTFactory.isAssignmentStatement(statement)) {
            return new AssignmentStatementVisitor(parent);
        } else if (ASTFactory.isTransformStatement(statement)) {
            return new TransformStatementVisitor(parent);
        } else if (ASTFactory.isReplyStatement(statement)) {
            return new ReplyStatementVisitor(parent);
        } else if (ASTFactory.isReturnStatement(statement)) {
            return new ReturnStatementVisitor(parent);
        } else if (ASTFactory.isFunctionInvocationStatement(statement)) {
            return new FunctionInvocationVisitor(parent);
        } else if (ASTFactory.isAssignment(statement)) {
            return new AssignmentVisitor(parent);
        } else if (ASTFactory.isLeftOperandExpression(statement)) {
            return new LeftOperandExpressionVisitor(parent);
        } else if (ASTFactory.isRightOperandExpression(statement)) {
            return new RightOperandExpressionVisitor(parent);
        } else if (ASTFactory.isVariableDefinitionStatement(statement)) {
            return new VariableDefinitionStatement(parent);
        } else if (ASTFactory.isWorkerInvocationStatement(statement)) {
            return new WorkerInvocationStatementVisitor(parent);
        } else if (ASTFactory.isWorkerReplyStatement(statement)) {
            return new WorkerReplyStatementVisitor(parent);
        } else if (ASTFactory.isActionInvocationStatement(statement)) {
            return new ActionInvocationStatementVisitor(parent);
        } else if (ASTFactory.isBreakStatement(statement)) {
            return new BreakStatementVisitor(parent);
        } else if (ASTFactory.isThrowStatement(statement)) {
            return new ThrowStatementVisitor(parent);
        } else if (ASTFactory.isCommentStatement(statement)) {
            return new CommentStatementVisitor(parent);
        } else if (ASTFactory.isTransactionAbortedStatement(statement)) {
            return new TransactionAbortedStatementVisitor(parent);
        } else if (ASTFactory.isTransactionStatement(statement)) {
            return new TransactionStatementVisitor(parent);
        } else if (ASTFactory.isAbortedStatement(statement)) {
            return new AbortedStatementVisitor(parent);
        } else if (ASTFactory.isAbortStatement(statement)) {
            return new AbortStatementVisitor(parent);
        }
    }
}

export default StatementVisitorFactor;
