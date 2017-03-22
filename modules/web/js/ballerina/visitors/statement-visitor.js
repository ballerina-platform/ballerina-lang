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
import log from 'log';
import ASTVisitor from './ast-visitor';
import AST from '../ast/module';

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

    beginVisitCatchStatement(statement) {
    }

    visitCatchStatement(statement) {
    }

    endVisitCatchStatement(statement) {
    }

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

    canVisitAssignmentStatement(statement) {
        return false;
    }

    beginVisitAssignmentStatement(statement) {
    }

    visitAssignmentStatement(statement) {
    }

    endVisitAssignmentStatement(statement) {
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

    canVisitLeftOperandExpression(statement) {
        return false;
    }

    beginVisitLeftOperandExpression(statement) {
    }

    visitLeftOperandExpression(statement) {
    }

    endVisitLeftOperandExpression(statement) {
    }

    canVisitRightOperandExpression(statement) {
        return false;
    }

    beginVisitRightOperandExpression(statement) {
    }

    visitRightOperandExpression(statement) {
    }

    endVisitRightOperandExpression(statement) {
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

    beginVisitCommentStatement(statement) {
    }

    visitCommentStatement(statement) {
    }

    endVisitCommentStatement(statement) {
    }

    /**
     * @param node {ASTNode}
     */
    visitStatement(node) {
        if (node instanceof AST.WhileStatement) {
            return this.visitWhileStatement(node);
        } else if (node instanceof AST.BreakStatement) {
            return this.visitBreakStatement(node);
        } else if (node instanceof AST.IfElseStatement) {
            return this.visitIfElseStatement(node);
        } else if (node instanceof AST.IfStatement) {
            return this.visitIfStatement(node);
        } else if (node instanceof AST.ElseStatement) {
            return this.visitElseStatement(node);
        }  else if (node instanceof AST.ElseIfStatement) {
            return this.visitElseIfStatement(node);
        } else if (node instanceof AST.TryCatchStatement) {
            return this.visitTryCatchStatement(node);
        } else if (node instanceof AST.TryStatement) {
            return this.visitTryStatement(node);
        } else if (node instanceof AST.CatchStatement) {
            return this.visitCatchStatement(node);
        } else if (node instanceof AST.AssignmentStatement) {
            return this.visitAssignmentStatement(node);
        } else if (node instanceof AST.Assignment) {
            return this.visitAssignment(node);
        } else if (node instanceof AST.ActionInvocationStatement) {
            return this.visitActionInvocationStatement(node);
        } else if (node instanceof AST.Expression) {
            return this.visitExpression(node);
        } else if (node instanceof AST.ReplyStatement) {
            return this.visitReplyStatement(node);
        } else if (node instanceof AST.ReturnStatement) {
            return this.visitReturnStatement(node);
        }  else if (node instanceof AST.FunctionInvocation) {
            return this.visitFuncInvocationStatement(node);
        }  else if (node instanceof AST.FunctionInvocationExpression) {
            return this.visitFuncInvocationExpression(node);
        }  else if (node instanceof AST.LeftOperandExpression) {
            return this.visitLeftOperandExpression(node);
        }  else if (node instanceof AST.RightOperandExpression) {
            return this.visitRightOperandExpression(node);
        }  else if (node instanceof AST.VariableDefinitionStatement) {
            return this.visitVariableDefinitionStatement(node);
        }  else if (node instanceof AST.WorkerInvocationStatement) {
            return this.visitWorkerInvocationStatement(node);
        }  else if (node instanceof AST.WorkerReplyStatement) {
            return this.visitWorkerReplyStatement(node);
        }  else if (node instanceof AST.ThrowStatement) {
            return this.visitThrowStatement(node);
        }  else if (node instanceof AST.CommentStatement) {
            return this.visitCommentStatement(node);
        }
    }

    /**
     * @param node {ASTNode}
     */
    canVisitStatement(node) {
        if (node instanceof AST.WhileStatement) {
            return this.canVisitWhileStatement(node);
        } else if (node instanceof AST.BreakStatement) {
            return this.canVisitBreakStatement(node);
        } else if (node instanceof AST.IfElseStatement) {
            return this.canVisitIfElseStatement(node);
        } else if (node instanceof AST.IfStatement) {
            return this.canVisitIfStatement(node);
        } else if (node instanceof AST.ElseStatement) {
            return this.canVisitElseStatement(node);
        }  else if (node instanceof AST.ElseIfStatement) {
            return this.canVisitElseIfStatement(node);
        } else if (node instanceof AST.TryCatchStatement) {
            return this.canVisitTryCatchStatement(node);
        } else if (node instanceof AST.TryStatement) {
            return this.canVisitTryStatement(node);
        } else if (node instanceof AST.CatchStatement) {
            return this.canVisitCatchStatement(node);
        } else if (node instanceof AST.AssignmentStatement) {
            return this.canVisitAssignmentStatement(node);
        }  else if (node instanceof AST.Assignment) {
            return this.canVisitAssignment(node);
        } else if (node instanceof AST.ActionInvocationStatement) {
            return this.canVisitActionInvocationStatement(node);
        } else if (node instanceof AST.Expression) {
            return this.canVisitExpression(node);
        } else if (node instanceof AST.ReplyStatement) {
            return this.canVisitReplyStatement(node);
        } else if (node instanceof AST.ReturnStatement) {
            return this.canVisitReturnStatement(node);
        }  else if (node instanceof AST.FunctionInvocation) {
            return this.canVisitFuncInvocationStatement(node);
        }  else if (node instanceof AST.FunctionInvocationExpression) {
            return this.canVisitFuncInvocationExpression(node);
        }  else if (node instanceof AST.LeftOperandExpression) {
            return this.canVisitLeftOperandExpression(node);
        }  else if (node instanceof AST.RightOperandExpression) {
            return this.canVisitRightOperandExpression(node);
        }  else if (node instanceof AST.VariableDefinitionStatement) {
            return this.canVisitVariableDefinitionStatement(node);
        }  else if (node instanceof AST.WorkerInvocationStatement) {
            return this.canVisitWorkerInvocationStatement(node);
        }  else if (node instanceof AST.WorkerReplyStatement) {
            return this.canVisitWorkerReplyStatement(node);
        }  else if (node instanceof AST.ThrowStatement) {
            return this.canVisitThrowStatement(node);
        }  else if (node instanceof AST.CommentStatement) {
            return this.canVisitCommentStatement(node);
        }
    }

    /**
     * @param node {ASTNode}
     */
    beginVisitStatement(node) {
        if (node instanceof AST.WhileStatement) {
            this.beginVisitWhileStatement(node);
        } else if (node instanceof AST.BreakStatement) {
            return this.beginVisitBreakStatement(node);
        } else if (node instanceof AST.IfElseStatement) {
            return this.beginVisitIfElseStatement(node);
        } else if (node instanceof AST.IfStatement) {
            return this.beginVisitIfStatement(node);
        } else if (node instanceof AST.ElseStatement) {
            return this.beginVisitElseStatement(node);
        }  else if (node instanceof AST.ElseIfStatement) {
            return this.beginVisitElseIfStatement(node);
        } else if (node instanceof AST.TryCatchStatement) {
            return this.beginVisitTryCatchStatement(node);
        } else if (node instanceof AST.TryStatement) {
            return this.beginVisitTryStatement(node);
        } else if (node instanceof AST.CatchStatement) {
            return this.beginVisitCatchStatement(node);
        } else if (node instanceof AST.AssignmentStatement) {
            return this.beginVisitAssignmentStatement(node);
        }  else if (node instanceof AST.Assignment) {
            return this.beginVisitAssignment(node);
        } else if (node instanceof AST.ActionInvocationStatement) {
            return this.beginVisitActionInvocationStatement(node);
        } else if (node instanceof AST.Expression) {
            return this.beginVisitExpression(node);
        } else if (node instanceof AST.ReplyStatement) {
            return this.beginVisitReplyStatement(node);
        } else if (node instanceof AST.ReturnStatement) {
            return this.beginVisitReturnStatement(node);
        }  else if (node instanceof AST.FunctionInvocation) {
            return this.beginVisitFuncInvocationStatement(node);
        }  else if (node instanceof AST.FunctionInvocationExpression) {
            return this.beginVisitFuncInvocationExpression(node);
        }  else if (node instanceof AST.LeftOperandExpression) {
            return this.beginVisitLeftOperandExpression(node);
        }  else if (node instanceof AST.RightOperandExpression) {
            return this.beginVisitRightOperandExpression(node);
        }  else if (node instanceof AST.VariableDefinitionStatement) {
            return this.beginVisitVariableDefinitionStatement(node);
        }  else if (node instanceof AST.WorkerInvocationStatement) {
            return this.beginVisitWorkerInvocationStatement(node);
        }  else if (node instanceof AST.WorkerReplyStatement) {
            return this.beginVisitWorkerReplyStatement(node);
        }  else if (node instanceof AST.ThrowStatement) {
            return this.beginVisitThrowStatement(node);
        }  else if (node instanceof AST.CommentStatement) {
            return this.beginVisitCommentStatement(node);
        }
    }

    /**
     * @param node {ASTNode}
     */
    endVisitStatement(node) {
        if (node instanceof AST.WhileStatement) {
            return this.endVisitWhileStatement(node);
        } else if (node instanceof AST.BreakStatement) {
            return this.endVisitBreakStatement(node);
        } else if (node instanceof AST.IfElseStatement) {
            return this.endVisitIfElseStatement(node);
        } else if (node instanceof AST.IfStatement) {
            return this.endVisitIfStatement(node);
        } else if (node instanceof AST.ElseStatement) {
            return this.endVisitElseStatement(node);
        }  else if (node instanceof AST.ElseIfStatement) {
            return this.endVisitElseIfStatement(node);
        } else if (node instanceof AST.TryCatchStatement) {
            return this.endVisitTryCatchStatement(node);
        } else if (node instanceof AST.TryStatement) {
            return this.endVisitTryStatement(node);
        } else if (node instanceof AST.CatchStatement) {
            return this.endVisitCatchStatement(node);
        } else if (node instanceof AST.AssignmentStatement) {
            return this.endVisitAssignmentStatement(node);
        }  else if (node instanceof AST.Assignment) {
            return this.endVisitAssignment(node);
        } else if (node instanceof AST.ActionInvocationStatement) {
            return this.endVisitActionInvocationStatement(node);
        } else if (node instanceof AST.Expression) {
            return this.endVisitExpression(node);
        } else if (node instanceof AST.ReplyStatement) {
            return this.endVisitReplyStatement(node);
        }  else if (node instanceof AST.ReturnStatement) {
            return this.endVisitReturnStatement(node);
        }  else if (node instanceof AST.FunctionInvocation) {
            return this.endVisitFuncInvocationStatement(node);
        }  else if (node instanceof AST.FunctionInvocationExpression) {
            return this.endVisitFuncInvocationExpression(node);
        }  else if (node instanceof AST.LeftOperandExpression) {
            return this.endVisitLeftOperandExpression(node);
        }  else if (node instanceof AST.RightOperandExpression) {
            return this.endVisitRightOperandExpression(node);
        }  else if (node instanceof AST.VariableDefinitionStatement) {
            return this.endVisitVariableDefinitionStatement(node);
        }  else if (node instanceof AST.WorkerInvocationStatement) {
            return this.endVisitWorkerInvocationStatement(node);
        }  else if (node instanceof AST.WorkerReplyStatement) {
            return this.endVisitWorkerReplyStatement(node);
        }  else if (node instanceof AST.ThrowStatement) {
            return this.endVisitThrowStatement(node);
        }  else if (node instanceof AST.CommentStatement) {
            return this.endVisitCommentStatement(node);
        }
    }
}

export default StatementVisitor;

