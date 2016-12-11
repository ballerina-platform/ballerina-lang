/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.parser.visitor;

import org.wso2.ballerina.core.interpreter.SymbolTable;
import org.wso2.ballerina.core.model.Identifier;
import org.wso2.ballerina.core.model.Worker;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.ForkJoinStmt;
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.ReplyStmt;
import org.wso2.ballerina.core.model.statements.ReturnStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.ThrowStmt;
import org.wso2.ballerina.core.model.statements.TryCatchStmt;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.parser.BallerinaBaseVisitor;
import org.wso2.ballerina.core.parser.BallerinaParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor for statement
 */
public class StatementVisitor extends BallerinaBaseVisitor {

    private SymbolTable statementSymbolTable;

    public StatementVisitor(SymbolTable parentSymbolTable) {
        this.statementSymbolTable = new SymbolTable(parentSymbolTable);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitStatement(BallerinaParser.StatementContext ctx) {
        if (ctx.assignmentStatement() != null) {
            return this.visitAssignmentStatement(ctx.assignmentStatement());
        } else if (ctx.ifElseStatement() != null) {
            return this.visitIfElseStatement(ctx.ifElseStatement());
        } else if (ctx.iterateStatement() != null) {
            return this.visitIterateStatement(ctx.iterateStatement());
        } else if (ctx.whileStatement() != null) {
            return this.visitWhileStatement(ctx.whileStatement());
        } else if (ctx.breakStatement() != null) {
            return this.visitBreakStatement(ctx.breakStatement());
        } else if (ctx.forkJoinStatement() != null) {
            return this.visitForkJoinStatement(ctx.forkJoinStatement());
        } else if (ctx.tryCatchStatement() != null) {
            return this.visitTryCatchStatement(ctx.tryCatchStatement());
        } else if (ctx.throwStatement() != null) {
            return this.visitThrowStatement(ctx.throwStatement());
        } else if (ctx.returnStatement() != null) {
            return this.visitReturnStatement(ctx.returnStatement());
        } else if (ctx.replyStatement() != null) {
            return this.visitReplyStatement(ctx.replyStatement());
        } else if (ctx.workerInteractionStatement() != null) {
            return this.visitWorkerInteractionStatement(ctx.workerInteractionStatement());
        } else if (ctx.commentStatement() != null) {
            return this.visitCommentStatement(ctx.commentStatement());
        } else if (ctx.actionInvocationStatement() != null) {
            return this.visitActionInvocationStatement(ctx.actionInvocationStatement());
        } else if (ctx.functionInvocationStatement() != null) {
            return this.visitFunctionInvocationStatement(ctx.functionInvocationStatement());
        }
        // Return null if nothing is matched
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitAssignmentStatement(BallerinaParser.AssignmentStatementContext ctx) {
        // Check the existence of the variable in the symbol table before the assignment
        try {
            Identifier value = this.statementSymbolTable.lookup(ctx.variableReference().getText());
            VariableRefExpr variableRefExpr = new VariableRefExpr(value);
            Expression expression = this.visitExpressionX(ctx.expression());
            return new AssignStmt(variableRefExpr, expression);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitIfElseStatement(BallerinaParser.IfElseStatementContext ctx) {
        Statement[] thenClauseStmtArray = new Statement[ctx.statement().size()];
        for (int i = 0; i < ctx.statement().size(); i++) {
            thenClauseStmtArray[i] = (Statement) this.visitStatement(
                    (BallerinaParser.StatementContext) ctx.statement(i).getChild(0));
        }

//        List<BallerinaParser.ElseIfClauseContext> elseIfClauseContexts = ctx.elseIfClause();
//        for (BallerinaParser.ElseIfClauseContext elseIfCtx : ctx.elseIfClause()) {
//            BallerinaParser.ExpressionContext elseIfCondition = elseIfCtx.expression();
//            List<BallerinaParser.StatementContext> elseIfStatements = elseIfCtx.statement();
//        }

        BallerinaParser.ElseClauseContext elseClauseContext = ctx.elseClause();
        Statement[] elseClauseStmtArray = new Statement[elseClauseContext.statement().size()];
        for (int i = 0; i < elseClauseContext.statement().size(); i++) {
            elseClauseStmtArray[i] = (Statement) this.visitStatement(
                    (BallerinaParser.StatementContext) elseClauseContext.statement(i).getChild(0));
        }

        //todo handle else if part
        return new IfElseStmt(this.visitExpressionX(ctx.expression()), new BlockStmt(thenClauseStmtArray),
                new BlockStmt(elseClauseStmtArray));
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitElseIfClause(BallerinaParser.ElseIfClauseContext ctx) {
        return super.visitElseIfClause(ctx);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitElseClause(BallerinaParser.ElseClauseContext ctx) {
        return super.visitElseClause(ctx);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitIterateStatement(BallerinaParser.IterateStatementContext ctx) {
        //todo
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitWhileStatement(BallerinaParser.WhileStatementContext ctx) {
        //todo getChild(0) because function body has statement+
        Statement[] statementArray = new Statement[ctx.statement().size()];
        for (int i = 0; i < ctx.statement().size(); i++) {
            statementArray[i] = (Statement) this.visitStatement(
                    (BallerinaParser.StatementContext) ctx.statement(i).getChild(0));
        }
        return new WhileStmt(this.visitExpressionX(ctx.expression()), new BlockStmt(statementArray));
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitBreakStatement(BallerinaParser.BreakStatementContext ctx) {
        //todo
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitForkJoinStatement(BallerinaParser.ForkJoinStatementContext ctx) {
        List<Worker> workerList = new ArrayList<>();
        WorkerVisitor workerVisitor = new WorkerVisitor(statementSymbolTable);
        for (BallerinaParser.WorkerDeclarationContext wrkCtx : ctx.workerDeclaration()) {
            workerList.add((Worker) wrkCtx.accept(workerVisitor));
        }

        BallerinaParser.JoinClauseContext joinClauseContext = ctx.joinClause();
        Statement[] joinClauseStmtArray = new Statement[joinClauseContext.statement().size()];

        for (int i = 0; i < joinClauseContext.statement().size(); i++) {
            joinClauseStmtArray[i] = (Statement) this.visitStatement((BallerinaParser.StatementContext)
                    joinClauseContext.statement(i).getChild(0));
        }
        //todo join condition expression
        return new ForkJoinStmt(workerList, null, new BlockStmt(joinClauseStmtArray));
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitJoinClause(BallerinaParser.JoinClauseContext ctx) {
        return super.visitJoinClause(ctx);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitJoinConditions(BallerinaParser.JoinConditionsContext ctx) {
        return super.visitJoinConditions(ctx);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitTimeoutClause(BallerinaParser.TimeoutClauseContext ctx) {
        return super.visitTimeoutClause(ctx);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitTryCatchStatement(BallerinaParser.TryCatchStatementContext ctx) {
        Statement[] tryClauseStmtArray = new Statement[ctx.statement().size()];
        for (int i = 0; i < ctx.statement().size(); i++) {
            tryClauseStmtArray[i] = (Statement) this.visitStatement(
                    (BallerinaParser.StatementContext) ctx.statement(i).getChild(0));
        }
        BallerinaParser.CatchClauseContext catchClauseContext = ctx.catchClause();
        Statement[] catchClauseStmtArray = new Statement[catchClauseContext.statement().size()];
        for (int i = 0; i < catchClauseContext.statement().size(); i++) {
            catchClauseStmtArray[i] = (Statement) this.visitStatement(
                    (BallerinaParser.StatementContext) catchClauseContext.statement(i).getChild(0));
        }
        //todo exception type only exception not sub types
        return new TryCatchStmt(new BlockStmt(tryClauseStmtArray), null, new BlockStmt(catchClauseStmtArray));
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitCatchClause(BallerinaParser.CatchClauseContext ctx) {
        return super.visitCatchClause(ctx);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitThrowStatement(BallerinaParser.ThrowStatementContext ctx) {
        ExpressionVisitor expressionVisitor = new ExpressionVisitor(statementSymbolTable);
        return new ThrowStmt((Expression) (ctx.expression().accept(expressionVisitor)));
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitReturnStatement(BallerinaParser.ReturnStatementContext ctx) {
        return new ReturnStmt(this.visitExpressionX(ctx.expressionList().expression(0)));
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitReplyStatement(BallerinaParser.ReplyStatementContext ctx) {
        return new ReplyStmt(this.visitExpressionX(ctx.expression()));
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitWorkerInteractionStatement(BallerinaParser.WorkerInteractionStatementContext ctx) {
        //todo
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitTriggerWorker(BallerinaParser.TriggerWorkerContext ctx) {
        return super.visitTriggerWorker(ctx);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitWorkerReply(BallerinaParser.WorkerReplyContext ctx) {
        return super.visitWorkerReply(ctx);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitCommentStatement(BallerinaParser.CommentStatementContext ctx) {
        //todo ?
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitActionInvocationStatement(BallerinaParser.ActionInvocationStatementContext ctx) {
        //todo
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitFunctionInvocationStatement(BallerinaParser.FunctionInvocationStatementContext ctx) {
        //todo verify this statement
        return null;
    }

    private Expression visitExpressionX(BallerinaParser.ExpressionContext ctx) {
        ExpressionVisitor expressionVisitor = new ExpressionVisitor(statementSymbolTable);
        return (Expression) ctx.accept(expressionVisitor);
    }

    /**
     * Base method for retrieving the symbol table
     *
     * @return symbol table for this instance
     */
    @Override
    public SymbolTable getSymbolTable() {
        return this.statementSymbolTable;
    }
}
