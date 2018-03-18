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
import ASTVisitor from './ast-visitor';
import ASTFactory from '../ast/ast-factory';

class StatementVisitor extends ASTVisitor {
    constructor(args) {
        super(args);
    }

    canVisitIfStatement(statement) {
        return false;
    }

    beginVisitIfStatement(statement) {
    }

    visitIfStatement(statement) {
    }

    endVisitIfStatement(statement) {
    }

    canVisitElseStatement(statement) {
        return false;
    }

    beginVisitElseStatement(statement) {
    }

    visitElseStatement(statement) {
    }

    endVisitElseStatement(statement) {
    }

    canVisitElseIfStatement(statement) {
        return false;
    }

    beginVisitElseIfStatement(statement) {
    }

    visitElseIfStatement(statement) {
    }

    endVisitElseIfStatement(statement) {
    }

    canVisitTryStatement(statement) {
        return false;
    }

    beginVisitTryStatement(statement) {
    }

    visitTryStatement(statement) {
    }

    endVisitTryStatement(statement) {
    }

    canVisitCatchStatement(statement) {
        return false;
    }

    canVisitFinallyStatement(statement) {
        return false;
    }

    beginVisitCatchStatement(statement) {
    }

    beginVisitFinallyStatement(statement) {
    }

    visitCatchStatement(statement) {
    }

    visitFinallyStatement(statement) {
    }

    endVisitCatchStatement(statement) {
    }

    endVisitFinallyStatement(statement) {
    }

    canVisitTransactionStatement(statement) {
        return false;
    }

    beginVisitTransactionStatement(statement) {
    }

    visitTransactionStatement(statement) {
    }

    endVisitTransactionStatement(statement) {
    }

    canVisitFailedStatement(statement) {
        return false;
    }

    beginVisitFailedStatement(statement) {
    }

    visitFailedStatement(statement) {
    }

    endVisitFailedStatement(statement) {
    }

    canVisitAbortedStatement(statement) {
        return false;
    }

    beginVisitAbortedStatement(statement) {
    }

    visitAbortedStatement(statement) {
    }

    endVisitAbortedStatement(statement) {
    }

    // / begin - abstract methods for committed statement source gen visitor function.
    canVisitCommittedStatement(statement) {
        return false;
    }

    beginVisitCommittedStatement(statement) {
    }

    visitCommittedStatement(statement) {
    }

    endVisitCommittedStatement(statement) {
    }

    // / end - abstract method for committed statement source gen visitor function.

    canVisitStatement(statement) {
        return false;
    }

    beginVisitStatement(statement) {
    }

    visitStatement(statement) {
    }

    endVisitStatement(statement) {
    }

    canVisitIfElseStatement(statement) {
        return false;
    }

    beginVisitIfElseStatement(statement) {
    }

    visitIfElseStatement(statement) {
    }

    endVisitIfElseStatement(statement) {
    }

    canVisitReplyStatement(statement) {
        return false;
    }

    beginVisitReplyStatement(statement) {
    }

    visitReplyStatement(statement) {
    }

    endVisitReplyStatement(statement) {
    }

    canVisitReturnStatement(statement) {
        return false;
    }

    beginVisitReturnStatement(statement) {
    }

    visitReturnStatement(statement) {
    }

    endVisitReturnStatement(statement) {
    }

    canVisitActionInvocationStatement(statement) {
        return false;
    }

    beginVisitActionInvocationStatement(statement) {
    }

    visitActionInvocationStatement(statement) {
    }

    endVisitActionInvocationStatement(statement) {
    }

    canVisitExpression(statement) {
        return false;
    }

    beginVisitExpression(statement) {
    }

    visitExpression(statement) {
    }

    endVisitExpression(statement) {
    }

    canVisitFuncInvocationStatement(statement) {
        return false;
    }

    beginVisitFuncInvocationStatement(statement) {
    }

    visitFuncInvocationStatement(statement) {
    }

    endVisitFuncInvocationStatement(statement) {
    }

    canVisitFuncInvocationExpression(expression) {
        return false;
    }

    beginVisitFuncInvocationExpression(expression) {
    }

    visitFuncInvocationExpression(expression) {
    }

    endVisitFuncInvocationExpression(expression) {
    }

    canVisitTryCatchStatement(statement) {
        return false;
    }

    beginVisitTryCatchStatement(statement) {
    }

    visitTryCatchStatement(statement) {
    }

    endVisitTryCatchStatement(statement) {
    }

    canVisitTransactionAbortedStatement(statement) {
        return false;
    }

    beginVisitTransactionAbortedStatement(statement) {
    }

    visitTransactionAbortedStatement(statement) {
    }

    endVisitTransactionAbortedStatement(statement) {
    }

    canVisitAssignmentStatement(statement) {
        return false;
    }

    beginVisitAssignmentStatement(statement) {
    }

    visitAssignmentStatement(statement) {
    }

    endVisitAssignmentStatement(statement) {
    }

    canVisitTransformStatement(statement) {
        return false;
    }

    beginVisitTransformStatement(statement) {
    }

    visitTransformStatement(statement) {
    }

    endVisitTransformStatement(statement) {
    }

    canVisitWhileStatement(statement) {
        return false;
    }

    beginVisitWhileStatement(statement) {
    }

    visitWhileStatement(statement) {
    }

    endVisitWhileStatement(statement) {
    }

    canVisitBreakStatement(statement) {
        return false;
    }

    beginVisitBreakStatement(statement) {
    }

    visitBreakStatement(statement) {
    }

    endVisitBreakStatement(statement) {
    }

    canVisitContinueStatement(statement) {
        return false;
    }

    beginVisitContinueStatement(statement) {
    }

    visitContinueStatement(statement) {
    }

    endVisitContinueStatement(statement) {
    }

    canVisitAbortStatement(statement) {
        return false;
    }

    beginVisitAbortStatement(statement) {
    }

    visitAbortStatement(statement) {
    }

    endVisitAbortStatement(statement) {
    }

    canVisitRetryStatement(statement) {
        return false;
    }

    beginVisitRetryStatement(statement) {
    }

    visitRetryStatement(statement) {
    }

    endVisitRetryStatement(statement) {
    }

    canVisitVariableDefinitionStatement(statement) {
        return false;
    }

    beginVisitVariableDefinitionStatement(statement) {
    }

    visitVariableDefinitionStatement(statement) {
    }

    endVisitVariableDefinitionStatement(statement) {
    }

    canVisitWorkerInvocationStatement() {
        return false;
    }

    beginVisitWorkerInvocationStatement(statement) {
    }

    visitWorkerInvocationStatement(statement) {
    }

    endVisitWorkerInvocationStatement(statement) {
    }

    canVisitWorkerReplyStatement() {
        return false;
    }

    beginVisitWorkerReplyStatement(statement) {
    }

    visitWorkerReplyStatement(statement) {
    }

    endVisitWorkerReplyStatement(statement) {
    }

    canVisitThrowStatement(statement) {
        return false;
    }

    beginVisitThrowStatement(statement) {
    }

    visitThrowStatement(statement) {
    }

    endVisitThrowStatement(statement) {
    }

    canVisitCommentStatement(statement) {
        return false;
    }

    canVisitTimeoutStatement(statement) {
        return false;
    }

    canVisitJoinStatement(statement) {
        return false;
    }

    canVisitForkJoinStatement(statement) {
        return false;
    }

    beginVisitCommentStatement(statement) {
    }

    visitCommentStatement(statement) {
    }

    endVisitCommentStatement(statement) {
    }

    canVisitNamespaceDeclarationStatement(statement) {
        return false;
    }

    beginVisitNamespaceDeclarationStatement(statement) {

    }

    visitNamespaceDeclarationStatement(statement) {

    }

    endVisitNamespaceDeclarationStatement(statement) {

    }

    /**
     * @param node {ASTNode}
     */
    visitStatement(node) {
        if (ASTFactory.isWhileStatement(node)) {
            return this.visitWhileStatement(node);
        } else if (ASTFactory.isBreakStatement(node)) {
            return this.visitBreakStatement(node);
        } else if (ASTFactory.isContinueStatement(node)) {
            return this.visitContinueStatement(node);
        } else if (ASTFactory.isIfElseStatement(node)) {
            return this.visitIfElseStatement(node);
        } else if (ASTFactory.isIfStatement(node)) {
            return this.visitIfStatement(node);
        } else if (ASTFactory.isElseStatement(node)) {
            return this.visitElseStatement(node);
        } else if (ASTFactory.isElseIfStatement(node)) {
            return this.visitElseIfStatement(node);
        } else if (ASTFactory.isTryCatchStatement(node)) {
            return this.visitTryCatchStatement(node);
        } else if (ASTFactory.isTryStatement(node)) {
            return this.visitTryStatement(node);
        } else if (ASTFactory.isCatchStatement(node)) {
            return this.visitCatchStatement(node);
        } else if (ASTFactory.isFinallyStatement(node)) {
            return this.visitFinallyStatement(node);
        } else if (ASTFactory.isAssignmentStatement(node)) {
            return this.visitAssignmentStatement(node);
        } else if (ASTFactory.isActionInvocationStatement(node)) {
            return this.visitActionInvocationStatement(node);
        } else if (ASTFactory.isExpression(node)) {
            return this.visitExpression(node);
        } else if (ASTFactory.isReplyStatement(node)) {
            return this.visitReplyStatement(node);
        } else if (ASTFactory.isReturnStatement(node)) {
            return this.visitReturnStatement(node);
        } else if (ASTFactory.isFunctionInvocationStatement(node)) {
            return this.visitFuncInvocationStatement(node);
        } else if (ASTFactory.isFunctionInvocationExpression(node)) {
            return this.visitFuncInvocationExpression(node);
        } else if (ASTFactory.isVariableDefinitionStatement(node)) {
            return this.visitVariableDefinitionStatement(node);
        } else if (ASTFactory.isWorkerInvocationStatement(node)) {
            return this.visitWorkerInvocationStatement(node);
        } else if (ASTFactory.isWorkerReplyStatement(node)) {
            return this.visitWorkerReplyStatement(node);
        } else if (ASTFactory.isThrowStatement(node)) {
            return this.visitThrowStatement(node);
        } else if (ASTFactory.isCommentStatement(node)) {
            return this.visitCommentStatement(node);
        } else if (ASTFactory.isTransactionAbortedStatement(node)) {
            return this.visitTransactionAbortedStatement(node);
        } else if (ASTFactory.isTransactionStatement(node)) {
            return this.visitTransactionStatement(node);
        } else if (ASTFactory.isAbortedStatement(node)) {
            return this.visitAbortedStatement(node);
        } else if (ASTFactory.isAbortStatement(node)) {
            return this.visitAbortStatement(node);
        } else if (ASTFactory.isJoinStatement(node)) {
            return this.visitJoinStatement(node);
        } else if (ASTFactory.isCommittedStatement(node)) {
            return this.visitCommittedStatement(node);
        } else if (ASTFactory.isNamespaceDeclarationStatement(node)) {
            return this.visitNamespaceDeclarationStatement(node);
        } else if (ASTFactory.isFailedStatement(node)) {
            return this.visitFailedStatement(node);
        } else if (ASTFactory.isRetry(node)) {
            return this.visitRetryStatement(node);
        }
    }

    /**
     * @param node {ASTNode}
     */
    canVisitStatement(node) {
        if (ASTFactory.isWhileStatement(node)) {
            return this.canVisitWhileStatement(node);
        } else if (ASTFactory.isBreakStatement(node)) {
            return this.canVisitBreakStatement(node);
        } else if (ASTFactory.isContinueStatement(node)) {
            return this.canVisitContinueStatement(node);
        } else if (ASTFactory.isIfElseStatement(node)) {
            return this.canVisitIfElseStatement(node);
        } else if (ASTFactory.isIfStatement(node)) {
            return this.canVisitIfStatement(node);
        } else if (ASTFactory.isElseStatement(node)) {
            return this.canVisitElseStatement(node);
        } else if (ASTFactory.isElseIfStatement(node)) {
            return this.canVisitElseIfStatement(node);
        } else if (ASTFactory.isTryCatchStatement(node)) {
            return this.canVisitTryCatchStatement(node);
        } else if (ASTFactory.isTryStatement(node)) {
            return this.canVisitTryStatement(node);
        } else if (ASTFactory.isCatchStatement(node)) {
            return this.canVisitCatchStatement(node);
        } else if (ASTFactory.isFinallyStatement(node)) {
            return this.canVisitFinallyStatement(node);
        } else if (ASTFactory.isAssignmentStatement(node)) {
            return this.canVisitAssignmentStatement(node);
        } else if (ASTFactory.isTransformStatement(node)) {
            return this.canVisitTransformStatement(node);
        } else if (ASTFactory.isActionInvocationStatement(node)) {
            return this.canVisitActionInvocationStatement(node);
        } else if (ASTFactory.isExpression(node)) {
            return this.canVisitExpression(node);
        } else if (ASTFactory.isReplyStatement(node)) {
            return this.canVisitReplyStatement(node);
        } else if (ASTFactory.isReturnStatement(node)) {
            return this.canVisitReturnStatement(node);
        } else if (ASTFactory.isFunctionInvocationStatement(node)) {
            return this.canVisitFuncInvocationStatement(node);
        } else if (ASTFactory.isFunctionInvocationExpression(node)) {
            return this.canVisitFuncInvocationExpression(node);
        } else if (ASTFactory.isVariableDefinitionStatement(node)) {
            return this.canVisitVariableDefinitionStatement(node);
        } else if (ASTFactory.isWorkerInvocationStatement(node)) {
            return this.canVisitWorkerInvocationStatement(node);
        } else if (ASTFactory.isWorkerReplyStatement(node)) {
            return this.canVisitWorkerReplyStatement(node);
        } else if (ASTFactory.isThrowStatement(node)) {
            return this.canVisitThrowStatement(node);
        } else if (ASTFactory.isCommentStatement(node)) {
            return this.canVisitCommentStatement(node);
        } else if (ASTFactory.isTimeoutStatement(node)) {
            return this.canVisitTimeoutStatement(node);
        } else if (ASTFactory.isForkJoinStatement(node)) {
            return this.canVisitForkJoinStatement(node);
        } else if (ASTFactory.isJoinStatement(node)) {
            return this.canVisitJoinStatement(node);
        } else if (ASTFactory.isTransactionAbortedStatement(node)) {
            return this.canVisitTransactionAbortedStatement(node);
        } else if (ASTFactory.isTransactionStatement(node)) {
            return this.canVisitTransactionStatement(node);
        } else if (ASTFactory.isAbortedStatement(node)) {
            return this.canVisitAbortedStatement(node);
        } else if (ASTFactory.isAbortStatement(node)) {
            return this.canVisitAbortStatement(node);
        } else if (ASTFactory.isCommittedStatement(node)) {
            return this.canVisitCommittedStatement(node);
        } else if (ASTFactory.isNamespaceDeclarationStatement(node)) {
            return this.canVisitNamespaceDeclarationStatement(node);
        } else if (ASTFactory.isFailedStatement(node)) {
            return this.canVisitFailedStatement(node);
        } else if (ASTFactory.isRetry(node)) {
            return this.canVisitRetryStatement(node);
        }
    }

    /**
     * @param node {ASTNode}
     */
    beginVisitStatement(node) {
        if (ASTFactory.isWhileStatement(node)) {
            this.beginVisitWhileStatement(node);
        } else if (ASTFactory.isBreakStatement(node)) {
            return this.beginVisitBreakStatement(node);
        } else if (ASTFactory.isContinueStatement(node)) {
            return this.beginVisitContinueStatement(node);
        } else if (ASTFactory.isIfElseStatement(node)) {
            return this.beginVisitIfElseStatement(node);
        } else if (ASTFactory.isIfStatement(node)) {
            return this.beginVisitIfStatement(node);
        } else if (ASTFactory.isElseStatement(node)) {
            return this.beginVisitElseStatement(node);
        } else if (ASTFactory.isElseIfStatement(node)) {
            return this.beginVisitElseIfStatement(node);
        } else if (ASTFactory.isTryCatchStatement(node)) {
            return this.beginVisitTryCatchStatement(node);
        } else if (ASTFactory.isTryStatement(node)) {
            return this.beginVisitTryStatement(node);
        } else if (ASTFactory.isCatchStatement(node)) {
            return this.beginVisitCatchStatement(node);
        } else if (ASTFactory.isFinallyStatement(node)) {
            return this.beginVisitFinallyStatement(node);
        } else if (ASTFactory.isAssignmentStatement(node)) {
            return this.beginVisitAssignmentStatement(node);
        } else if (ASTFactory.isActionInvocationStatement(node)) {
            return this.beginVisitActionInvocationStatement(node);
        } else if (ASTFactory.isExpression(node)) {
            return this.beginVisitExpression(node);
        } else if (ASTFactory.isReplyStatement(node)) {
            return this.beginVisitReplyStatement(node);
        } else if (ASTFactory.isReturnStatement(node)) {
            return this.beginVisitReturnStatement(node);
        } else if (ASTFactory.isFunctionInvocationStatement(node)) {
            return this.beginVisitFuncInvocationStatement(node);
        } else if (ASTFactory.isFunctionInvocationExpression(node)) {
            return this.beginVisitFuncInvocationExpression(node);
        } else if (ASTFactory.isVariableDefinitionStatement(node)) {
            return this.beginVisitVariableDefinitionStatement(node);
        } else if (ASTFactory.isWorkerInvocationStatement(node)) {
            return this.beginVisitWorkerInvocationStatement(node);
        } else if (ASTFactory.isWorkerReplyStatement(node)) {
            return this.beginVisitWorkerReplyStatement(node);
        } else if (ASTFactory.isThrowStatement(node)) {
            return this.beginVisitThrowStatement(node);
        } else if (ASTFactory.isCommentStatement(node)) {
            return this.beginVisitCommentStatement(node);
        } else if (ASTFactory.isTransformStatement(node)) {
            return this.beginVisitTransformStatement(node);
        } else if (ASTFactory.isTransactionAbortedStatement(node)) {
            return this.beginVisitTransactionAbortedStatement(node);
        } else if (ASTFactory.isTransactionStatement(node)) {
            return this.beginVisitTransactionStatement(node);
        } else if (ASTFactory.isAbortedStatement(node)) {
            return this.beginVisitAbortedStatement(node);
        } else if (ASTFactory.isAbortStatement(node)) {
            return this.beginVisitAbortStatement(node);
        } else if (ASTFactory.isTimeoutStatement(node)) {
            return this.beginVisitTimeoutStatement(node);
        } else if (ASTFactory.isForkJoinStatement(node)) {
            return this.beginVisitForkJoinStatement(node);
        } else if (ASTFactory.isJoinStatement(node)) {
            return this.beginVisitJoinStatement(node);
        } else if (ASTFactory.isCommittedStatement(node)) {
            return this.beginVisitCommittedStatement(node);
        } else if (ASTFactory.isNamespaceDeclarationStatement(node)) {
            return this.beginVisitNamespaceDeclarationStatement(node);
        } else if (ASTFactory.isFailedStatement(node)) {
            return this.beginVisitFailedStatement(node);
        } else if (ASTFactory.isRetry(node)) {
            return this.beginVisitRetryStatement(node);
        }
    }

    /**
     * @param node {ASTNode}
     */
    endVisitStatement(node) {
        if (ASTFactory.isWhileStatement(node)) {
            return this.endVisitWhileStatement(node);
        } else if (ASTFactory.isBreakStatement(node)) {
            return this.endVisitBreakStatement(node);
        } else if (ASTFactory.isContinueStatement(node)) {
            return this.endVisitContinueStatement(node);
        } else if (ASTFactory.isIfElseStatement(node)) {
            return this.endVisitIfElseStatement(node);
        } else if (ASTFactory.isIfStatement(node)) {
            return this.endVisitIfStatement(node);
        } else if (ASTFactory.isElseStatement(node)) {
            return this.endVisitElseStatement(node);
        } else if (ASTFactory.isElseIfStatement(node)) {
            return this.endVisitElseIfStatement(node);
        } else if (ASTFactory.isTryCatchStatement(node)) {
            return this.endVisitTryCatchStatement(node);
        } else if (ASTFactory.isTryStatement(node)) {
            return this.endVisitTryStatement(node);
        } else if (ASTFactory.isCatchStatement(node)) {
            return this.endVisitCatchStatement(node);
        } else if (ASTFactory.isFinallyStatement(node)) {
            return this.endVisitFinallyStatement(node);
        } else if (ASTFactory.isAssignmentStatement(node)) {
            return this.endVisitAssignmentStatement(node);
        } else if (ASTFactory.isTransformStatement(node)) {
            return this.endVisitTransformStatement(node);
        } else if (ASTFactory.isActionInvocationStatement(node)) {
            return this.endVisitActionInvocationStatement(node);
        } else if (ASTFactory.isExpression(node)) {
            return this.endVisitExpression(node);
        } else if (ASTFactory.isReplyStatement(node)) {
            return this.endVisitReplyStatement(node);
        } else if (ASTFactory.isReturnStatement(node)) {
            return this.endVisitReturnStatement(node);
        } else if (ASTFactory.isFunctionInvocationStatement(node)) {
            return this.endVisitFuncInvocationStatement(node);
        } else if (ASTFactory.isFunctionInvocationExpression(node)) {
            return this.endVisitFuncInvocationExpression(node);
        } else if (ASTFactory.isVariableDefinitionStatement(node)) {
            return this.endVisitVariableDefinitionStatement(node);
        } else if (ASTFactory.isWorkerInvocationStatement(node)) {
            return this.endVisitWorkerInvocationStatement(node);
        } else if (ASTFactory.isWorkerReplyStatement(node)) {
            return this.endVisitWorkerReplyStatement(node);
        } else if (ASTFactory.isThrowStatement(node)) {
            return this.endVisitThrowStatement(node);
        } else if (ASTFactory.isCommentStatement(node)) {
            return this.endVisitCommentStatement(node);
        } else if (ASTFactory.isTransactionAbortedStatement(node)) {
            return this.endVisitTransactionAbortedStatement(node);
        } else if (ASTFactory.isTransactionStatement(node)) {
            return this.endVisitTransactionStatement(node);
        } else if (ASTFactory.isAbortedStatement(node)) {
            return this.endVisitAbortedStatement(node);
        } else if (ASTFactory.isAbortStatement(node)) {
            return this.endVisitAbortStatement(node);
        } else if (ASTFactory.isTimeoutStatement(node)) {
            return this.endVisitTimeoutStatement(node);
        } else if (ASTFactory.isForkJoinStatement(node)) {
            return this.endVisitForkJoinStatement(node);
        } else if (ASTFactory.isJoinStatement(node)) {
            return this.endVisitJoinStatement(node);
        } else if (ASTFactory.isCommittedStatement(node)) {
            return this.endVisitCommittedStatement(node);
        } else if (ASTFactory.isNamespaceDeclarationStatement(node)) {
            return this.endVisitNamespaceDeclarationStatement(node);
        } else if (ASTFactory.isFailedStatement(node)) {
            return this.endVisitFailedStatement(node);
        } else if (ASTFactory.isRetry(node)) {
            return this.endVisitRetryStatement(node);
        }
    }
}

export default StatementVisitor;
