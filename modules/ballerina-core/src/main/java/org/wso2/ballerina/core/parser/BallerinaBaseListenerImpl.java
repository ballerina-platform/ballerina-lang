/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.parser;

import org.antlr.v4.runtime.tree.ParseTree;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.Identifier;
import org.wso2.ballerina.core.model.Import;
import org.wso2.ballerina.core.model.Operator;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.Worker;
import org.wso2.ballerina.core.model.expressions.AddExpression;
import org.wso2.ballerina.core.model.expressions.AndExpression;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.EqualExpression;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.GreaterEqualExpression;
import org.wso2.ballerina.core.model.expressions.GreaterThanExpression;
import org.wso2.ballerina.core.model.expressions.LessEqualExpression;
import org.wso2.ballerina.core.model.expressions.LessThanExpression;
import org.wso2.ballerina.core.model.expressions.MultExpression;
import org.wso2.ballerina.core.model.expressions.NotEqualExpression;
import org.wso2.ballerina.core.model.expressions.OrExpression;
import org.wso2.ballerina.core.model.expressions.SubstractExpression;
import org.wso2.ballerina.core.model.expressions.UnaryExpression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.ForkJoinStmt;
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.ReplyStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.ThrowStmt;
import org.wso2.ballerina.core.model.statements.TryCatchStmt;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.types.Type;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.BooleanValue;
import org.wso2.ballerina.core.model.values.FloatValue;
import org.wso2.ballerina.core.model.values.IntValue;
import org.wso2.ballerina.core.model.values.StringValue;
import org.wso2.ballerina.core.parser.BallerinaParser.ActionInvocationExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.ActionInvocationStatementContext;
import org.wso2.ballerina.core.parser.BallerinaParser.AnnotationContext;
import org.wso2.ballerina.core.parser.BallerinaParser.AssignmentStatementContext;
import org.wso2.ballerina.core.parser.BallerinaParser.BinaryAddExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.BinaryAndExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.BinaryDivitionExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.BinaryEqualExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.BinaryGEExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.BinaryGTExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.BinaryLEExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.BinaryLTExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.BinaryModExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.BinaryMultiplicationExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.BinaryNotEqualExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.BinaryOrExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.BinaryPowExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.BinarySubExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.BracedExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.BreakStatementContext;
import org.wso2.ballerina.core.parser.BallerinaParser.CatchClauseContext;
import org.wso2.ballerina.core.parser.BallerinaParser.CommentStatementContext;
import org.wso2.ballerina.core.parser.BallerinaParser.ElseClauseContext;
import org.wso2.ballerina.core.parser.BallerinaParser.ElseIfClauseContext;
import org.wso2.ballerina.core.parser.BallerinaParser.ExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.ForkJoinStatementContext;
import org.wso2.ballerina.core.parser.BallerinaParser.FunctionDefinitionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.FunctionInvocationExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.FunctionInvocationStatementContext;
import org.wso2.ballerina.core.parser.BallerinaParser.IfElseStatementContext;
import org.wso2.ballerina.core.parser.BallerinaParser.ImportDeclarationContext;
import org.wso2.ballerina.core.parser.BallerinaParser.IterateStatementContext;
import org.wso2.ballerina.core.parser.BallerinaParser.JoinClauseContext;
import org.wso2.ballerina.core.parser.BallerinaParser.LiteralExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.LiteralValueContext;
import org.wso2.ballerina.core.parser.BallerinaParser.PackageDeclarationContext;
import org.wso2.ballerina.core.parser.BallerinaParser.ParameterContext;
import org.wso2.ballerina.core.parser.BallerinaParser.ParameterListContext;
import org.wso2.ballerina.core.parser.BallerinaParser.ReplyStatementContext;
import org.wso2.ballerina.core.parser.BallerinaParser.ReturnStatementContext;
import org.wso2.ballerina.core.parser.BallerinaParser.ReturnTypeListContext;
import org.wso2.ballerina.core.parser.BallerinaParser.StatementContext;
import org.wso2.ballerina.core.parser.BallerinaParser.TemplateExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.ThrowStatementContext;
import org.wso2.ballerina.core.parser.BallerinaParser.TryCatchStatementContext;
import org.wso2.ballerina.core.parser.BallerinaParser.TypeCastingExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.TypeNameContext;
import org.wso2.ballerina.core.parser.BallerinaParser.UnaryExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.VariableDeclarationContext;
import org.wso2.ballerina.core.parser.BallerinaParser.VariableReferenceExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.WhileStatementContext;
import org.wso2.ballerina.core.parser.BallerinaParser.WorkerDeclarationContext;
import org.wso2.ballerina.core.parser.BallerinaParser.WorkerInteractionStatementContext;

import java.util.ArrayList;
import java.util.List;

public class BallerinaBaseListenerImpl extends BallerinaBaseListener {

    BallerinaFile balFile = new BallerinaFile();

    @Override
    public void exitPackageDeclaration(PackageDeclarationContext ctx) {
        balFile.setPackageName(ctx.packageName().getText());
    }

    @Override
    public void exitImportDeclaration(ImportDeclarationContext ctx) {
        if (ctx.Identifier() != null) {
            balFile.addImport(new Import(ctx.Identifier().getText(), ctx.packageName().getText()));
        } else {
            balFile.addImport(new Import(ctx.packageName().getText()));
        }
    }

    @Override
    public void exitFunctionDefinition(FunctionDefinitionContext ctx) {
        Identifier functionName = new Identifier(ctx.Identifier(0).getText());
        boolean isPublicFunction = !ctx.getTokens(31)
                .isEmpty(); //since function body cannot have public keyword inside.

        List<Annotation> annotationList = new ArrayList<>();
        for (AnnotationContext annotationContext : ctx.annotation()) {
            annotationList.add(parserAnnotation(annotationContext));
        }

        Annotation[] annotations = annotationList.toArray(new Annotation[annotationList.size()]);

        List<Parameter> parameterList = new ArrayList<>();
        ParameterListContext parameterListContext = ctx.parameterList();
        if (parameterListContext != null) { //parameter list is optional
            for (ParameterContext praCtx : parameterListContext.parameter()) {
                parameterList.add(parserParameter(praCtx));
            }
        }

        Parameter[] parameters = parameterList.toArray(new Parameter[parameterList.size()]);

        List<Type> typeList = new ArrayList<>();
        ReturnTypeListContext returnTypeListContext = ctx.returnTypeList();
        if (returnTypeListContext != null) { //return type list is optional
            for (TypeNameContext typeNameContext : returnTypeListContext.typeNameList().typeName()) {
                parserTypeName(typeNameContext);
            }
        }

        Type[] types = typeList.toArray(new Type[typeList.size()]);

        List<VariableDcl> variableDclList = new ArrayList<>();
        for (VariableDeclarationContext variableDeclarationContext : ctx.functionBody().variableDeclaration()) {
            variableDclList.add(parserVariableDclCtx(variableDeclarationContext));
        }

        VariableDcl[] variableDcls = variableDclList.toArray(new VariableDcl[variableDclList.size()]);

        Statement[] statementArray = new Statement[ctx.functionBody().statement().size()];
        for (int i = 0; i < ctx.functionBody().statement().size(); i++) {
            statementArray[i] = parserStatementCtx(ctx.functionBody().statement(i).getChild(0));
        }

        Function function = new BallerinaFunction(
                functionName,
                isPublicFunction,
                annotations,
                parameters,
                null,
                null,
                variableDcls,
                null,
                new BlockStmt(statementArray));

        balFile.addFunction(function);

    }

    @Override
    public void exitStatement(StatementContext ctx) {
        //Statement statement = parserStatementCtx(ctx.getChild(0));//todo can it have multiple children
    }

    private Expression parserExpressionCtx(ParseTree ctx) {

        if (ctx instanceof LiteralExpressionContext) {

            LiteralExpressionContext binaryExpCtx = (LiteralExpressionContext) ctx;

            LiteralValueContext literalValue = binaryExpCtx.literalValue();
            if (literalValue.IntegerLiteral() != null) {
                return new BasicLiteral(
                        new BValueRef(new IntValue(Integer.parseInt(literalValue.IntegerLiteral().getText()))));
            } else if (literalValue.FloatingPointLiteral() != null) {
                return new BasicLiteral(
                        new BValueRef(new FloatValue(Float.parseFloat(literalValue.FloatingPointLiteral().getText()))));
            } else if (literalValue.QuotedStringLiteral() != null) {
                return new BasicLiteral(new BValueRef(new StringValue(literalValue.QuotedStringLiteral().getText())));
            } else if (literalValue.BooleanLiteral() != null) {
                return new BasicLiteral(
                        new BValueRef(new BooleanValue(Boolean.parseBoolean(literalValue.BooleanLiteral().getText()))));

            } else {
                return null;
            }
        } else if (ctx instanceof VariableReferenceExpressionContext) {
            VariableReferenceExpressionContext binaryExpCtx = (VariableReferenceExpressionContext) ctx;
            //todo can extract whether simple, map , array or struct reference
            return new VariableRefExpr(new Identifier(binaryExpCtx.variableReference().getText()));

        } else if (ctx instanceof TemplateExpressionContext) {
            TemplateExpressionContext binaryExpCtx = (TemplateExpressionContext) ctx;
            // xml or json as sting
            return new BasicLiteral(new BValueRef(new StringValue(binaryExpCtx.getText())));

        } else if (ctx instanceof FunctionInvocationExpressionContext) {
            FunctionInvocationExpressionContext funInoCtx = (FunctionInvocationExpressionContext) ctx;

            List<Expression> expressionList = new ArrayList<>();

            for (ExpressionContext expressionContext : funInoCtx.argumentList().expressionList().expression()) {
                expressionList.add(parserExpressionCtx(expressionContext));
            }
            return new FunctionInvocationExpr(new Identifier(funInoCtx.functionName().getText()), expressionList);

        } else if (ctx instanceof ActionInvocationExpressionContext) {
            ActionInvocationExpressionContext actionInoCtx = (ActionInvocationExpressionContext) ctx;
            //todo action invocation expression
            return null;

        } else if (ctx instanceof TypeCastingExpressionContext) {
            // todo type cast expression
            return null;
        } else if (ctx instanceof UnaryExpressionContext) {
            UnaryExpressionContext unaryExpCtx = (UnaryExpressionContext) ctx;
            return new UnaryExpression(getOpName(unaryExpCtx.getChild(0).getText()),
                    parserExpressionCtx(unaryExpCtx.expression()));

        } else if (ctx instanceof BracedExpressionContext) {
            // todo braced expression
            return null;

        } else if (ctx instanceof BinaryPowExpressionContext) {
            BinaryPowExpressionContext binaryExpCtx = (BinaryPowExpressionContext) ctx;

            // todo implement pow operator

        } else if (ctx instanceof BinaryDivitionExpressionContext) {
            BinaryDivitionExpressionContext binaryExpCtx = (BinaryDivitionExpressionContext) ctx;
            // todo implement divition operator
        } else if (ctx instanceof BinaryMultiplicationExpressionContext) {

            BinaryMultiplicationExpressionContext binaryExpCtx = (BinaryMultiplicationExpressionContext) ctx;

            return new MultExpression(parserExpressionCtx(binaryExpCtx.children.get(0)),
                    parserExpressionCtx(binaryExpCtx.children.get(2)));

        } else if (ctx instanceof BinaryModExpressionContext) {

            BinaryModExpressionContext binaryExpCtx = (BinaryModExpressionContext) ctx;

            //todo implement mod operator
            return null;

        } else if (ctx instanceof BinaryAndExpressionContext) {

            BinaryAndExpressionContext binaryExpCtx = (BinaryAndExpressionContext) ctx;

            return new AndExpression(parserExpressionCtx(binaryExpCtx.children.get(0)),
                    parserExpressionCtx(binaryExpCtx.children.get(2)));

        } else if (ctx instanceof BinaryAddExpressionContext) {

            BinaryAddExpressionContext binaryExpCtx = (BinaryAddExpressionContext) ctx;

            return new AddExpression(parserExpressionCtx(binaryExpCtx.children.get(0)),
                    parserExpressionCtx(binaryExpCtx.children.get(2)));

        } else if (ctx instanceof BinarySubExpressionContext) {

            BinarySubExpressionContext binaryExpCtx = (BinarySubExpressionContext) ctx;

            return new SubstractExpression(parserExpressionCtx(binaryExpCtx.children.get(0)),
                    parserExpressionCtx(binaryExpCtx.children.get(2)));

        } else if (ctx instanceof BinaryOrExpressionContext) {

            BinaryOrExpressionContext binaryExpCtx = (BinaryOrExpressionContext) ctx;

            return new OrExpression(parserExpressionCtx(binaryExpCtx.children.get(0)),
                    parserExpressionCtx(binaryExpCtx.children.get(2)));

        } else if (ctx instanceof BinaryGTExpressionContext) {

            BinaryGTExpressionContext binaryExpCtx = (BinaryGTExpressionContext) ctx;

            return new GreaterThanExpression(parserExpressionCtx(binaryExpCtx.children.get(0)),
                    parserExpressionCtx(binaryExpCtx.children.get(2)));

        } else if (ctx instanceof BinaryGEExpressionContext) {

            BinaryGEExpressionContext binaryExpCtx = (BinaryGEExpressionContext) ctx;

            return new GreaterEqualExpression(parserExpressionCtx(binaryExpCtx.children.get(0)),
                    parserExpressionCtx(binaryExpCtx.children.get(2)));

        } else if (ctx instanceof BinaryLTExpressionContext) {

            BinaryLTExpressionContext binaryExpCtx = (BinaryLTExpressionContext) ctx;

            return new LessThanExpression(parserExpressionCtx(binaryExpCtx.children.get(0)),
                    parserExpressionCtx(binaryExpCtx.children.get(2)));

        } else if (ctx instanceof BinaryLEExpressionContext) {

            BinaryLEExpressionContext binaryExpCtx = (BinaryLEExpressionContext) ctx;

            return new LessEqualExpression(parserExpressionCtx(binaryExpCtx.children.get(0)),
                    parserExpressionCtx(binaryExpCtx.children.get(2)));

        } else if (ctx instanceof BinaryEqualExpressionContext) {

            BinaryEqualExpressionContext binaryExpCtx = (BinaryEqualExpressionContext) ctx;

            return new EqualExpression(parserExpressionCtx(binaryExpCtx.children.get(0)),
                    parserExpressionCtx(binaryExpCtx.children.get(2)));

        } else if (ctx instanceof BinaryNotEqualExpressionContext) {

            BinaryNotEqualExpressionContext binaryExpCtx = (BinaryNotEqualExpressionContext) ctx;

            return new NotEqualExpression(parserExpressionCtx(binaryExpCtx.children.get(0)),
                    parserExpressionCtx(binaryExpCtx.children.get(2)));

        }
        return null;
    }

    private Statement parserStatementCtx(ParseTree ctx) {
        if (ctx instanceof AssignmentStatementContext) {
            AssignmentStatementContext assignmentStmtCtx = (AssignmentStatementContext) ctx;

            VariableRefExpr variableRefExpr = new VariableRefExpr(
                    new Identifier(assignmentStmtCtx.variableReference().getText()));

            Expression expression = parserExpressionCtx(assignmentStmtCtx.expression());
            return new AssignStmt(variableRefExpr, expression);

        } else if (ctx instanceof IfElseStatementContext) {
            IfElseStatementContext ifElseStmtCtx = (IfElseStatementContext) ctx;

            Statement[] thenClauseStmtArray = new Statement[ifElseStmtCtx.statement().size()];
            for (int i = 0; i < ifElseStmtCtx.statement().size(); i++) {
                thenClauseStmtArray[i] = parserStatementCtx(ifElseStmtCtx.statement(i).getChild(0));
            }

            List<ElseIfClauseContext> elseIfClauseContexts = ifElseStmtCtx.elseIfClause();
            for (ElseIfClauseContext elseIfCtx : ifElseStmtCtx.elseIfClause()) {
                ExpressionContext elseIfCondition = elseIfCtx.expression();
                List<StatementContext> elseIfStatements = elseIfCtx.statement();
            }

            ElseClauseContext elseClauseContext = ifElseStmtCtx.elseClause();
            Statement[] elseClauseStmtArray = new Statement[elseClauseContext.statement().size()];
            for (int i = 0; i < elseClauseContext.statement().size(); i++) {
                elseClauseStmtArray[i] = parserStatementCtx(elseClauseContext.statement(i).getChild(0));
            }

            //todo handle else if part
            return new IfElseStmt(parserExpressionCtx(ifElseStmtCtx.expression()), new BlockStmt(thenClauseStmtArray),
                    new BlockStmt(elseClauseStmtArray));
        } else if (ctx instanceof IterateStatementContext) {
            IterateStatementContext iterateStmtCtx = (IterateStatementContext) ctx;
            //todo
            return null;
        } else if (ctx instanceof WhileStatementContext) {
            WhileStatementContext whileStmtCtx = (WhileStatementContext) ctx;
            //todo getChild(0) because function body has statement+
            Statement[] statementArray = new Statement[whileStmtCtx.statement().size()];
            for (int i = 0; i < whileStmtCtx.statement().size(); i++) {
                statementArray[i] = parserStatementCtx(whileStmtCtx.statement(i).getChild(0));
            }
            return new WhileStmt(parserExpressionCtx(whileStmtCtx.expression()), new BlockStmt(statementArray));

        } else if (ctx instanceof BreakStatementContext) {
            BreakStatementContext breakStmtCtx = (BreakStatementContext) ctx;
            //todo
            return null;
        } else if (ctx instanceof ForkJoinStatementContext) {
            ForkJoinStatementContext forkJoinStmtCtx = (ForkJoinStatementContext) ctx;

            List<Worker> workerList = new ArrayList<>();
            for (WorkerDeclarationContext wrkCtx : forkJoinStmtCtx.workerDeclaration()) {
                workerList.add(parserWorker(wrkCtx));
            }

            JoinClauseContext joinClauseContext = forkJoinStmtCtx.joinClause();
            Statement[] joinClauseStmtArray = new Statement[joinClauseContext.statement().size()];
            for (int i = 0; i < joinClauseContext.statement().size(); i++) {
                joinClauseStmtArray[i] = parserStatementCtx(joinClauseContext.statement(i).getChild(0));
            }
            //todo join condition expression
            return new ForkJoinStmt(workerList, null, new BlockStmt(joinClauseStmtArray));
        } else if (ctx instanceof TryCatchStatementContext) {
            TryCatchStatementContext tryCatchStmtCtx = (TryCatchStatementContext) ctx;

            Statement[] tryClauseStmtArray = new Statement[tryCatchStmtCtx.statement().size()];
            for (int i = 0; i < tryCatchStmtCtx.statement().size(); i++) {
                tryClauseStmtArray[i] = parserStatementCtx(tryCatchStmtCtx.statement(i).getChild(0));
            }

            CatchClauseContext catchClauseContext = tryCatchStmtCtx.catchClause();

            Statement[] catchClauseStmtArray = new Statement[catchClauseContext.statement().size()];
            for (int i = 0; i < catchClauseContext.statement().size(); i++) {
                catchClauseStmtArray[i] = parserStatementCtx(catchClauseContext.statement(i).getChild(0));
            }

            //todo exception type only exception not sub types
            return new TryCatchStmt(new BlockStmt(tryClauseStmtArray), null, new BlockStmt(catchClauseStmtArray));
        } else if (ctx instanceof ThrowStatementContext) {
            ThrowStatementContext throwStmtCtx = (ThrowStatementContext) ctx;
            new ThrowStmt(parserExpressionCtx(throwStmtCtx.expression()));
            return null;
        } else if (ctx instanceof ReturnStatementContext) {
            ReturnStatementContext returnStmtCtx = (ReturnStatementContext) ctx;
            return new ReplyStmt(parserExpressionCtx(returnStmtCtx));
        } else if (ctx instanceof ReplyStatementContext) {
            ReplyStatementContext replyStmtCtx = (ReplyStatementContext) ctx;
            return new ReplyStmt(parserExpressionCtx(replyStmtCtx));
        } else if (ctx instanceof WorkerInteractionStatementContext) {
            WorkerInteractionStatementContext wkrInteractionStmtCtx = (WorkerInteractionStatementContext) ctx;
            //todo
            return null;
        } else if (ctx instanceof CommentStatementContext) {
            CommentStatementContext commentStatementContext = (CommentStatementContext) ctx;
            //todo ?
            return null;
        } else if (ctx instanceof ActionInvocationStatementContext) {
            ActionInvocationStatementContext actionInvocationStmtCtx = (ActionInvocationStatementContext) ctx;
            //todo
            return null;
        } else if (ctx instanceof FunctionInvocationStatementContext) {
            FunctionInvocationStatementContext functionInvocationStmtCtx = (FunctionInvocationStatementContext) ctx;
            //todo verify this statement
            return null;
        }
        return null;
    }

    private VariableDcl parserVariableDclCtx(VariableDeclarationContext ctx) {
        //todo value should be removed from constructor
        return new VariableDcl(parserTypeName(ctx.typeName()), new Identifier(ctx.Identifier().getText()), null);
    }

    private Worker parserWorker(WorkerDeclarationContext ctx) {
        List<VariableDcl> variableDclList = new ArrayList<>();
        for (VariableDeclarationContext varDclCtx : ctx.variableDeclaration()) {
            variableDclList.add(parserVariableDclCtx(varDclCtx));
        }

        List<Statement> statementList = new ArrayList<>();
        for (StatementContext statementContext : ctx.statement()) {
            //todo check getChild(0)
            statementList.add(parserStatementCtx(statementContext.getChild(0)));
        }

        return new Worker(variableDclList, statementList);
    }

    private Annotation parserAnnotation(AnnotationContext ctx) {
        //todo implement complex annotations later
        return new Annotation(ctx.annotationName().getText(), ctx.elementValue().getText());
    }

    private Parameter parserParameter(ParameterContext ctx) {
        return new Parameter(parserTypeName(ctx.typeName()), new Identifier(ctx.Identifier().getText()));
    }

    private Type parserTypeName(TypeNameContext ctx) {
        //todo
        return null;
    }

    @Override
    public void exitLiteralExpression(LiteralExpressionContext ctx) {
        super.exitLiteralExpression(ctx);
    }

    private Operator getOpName(String op) {
        switch (op) {
            case "+":
                return Operator.ADD;
            case "-":
                return Operator.SUB;
            case "*":
                return Operator.MUL;
            case "/":
                return Operator.DIV;
            case "&&":
                return Operator.AND;
            case "||":
                return Operator.OR;
            case "==":
                return Operator.EQUAL;
            case "!=":
                return Operator.NOT_EQUAL;
            case ">":
                return Operator.GREATER_THAN;
            case ">=":
                return Operator.GREATER_EQUAL;
            case "<":
                return Operator.LESS_THAN;
            case "<=":
                return Operator.LESS_EQUAL;
        }
        return null;
    }

}
