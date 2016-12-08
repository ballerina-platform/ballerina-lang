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
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.Identifier;
import org.wso2.ballerina.core.model.Import;
import org.wso2.ballerina.core.model.VariableDcl;
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
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.types.IntType;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.BooleanValue;
import org.wso2.ballerina.core.model.values.FloatValue;
import org.wso2.ballerina.core.model.values.IntValue;
import org.wso2.ballerina.core.model.values.StringValue;
import org.wso2.ballerina.core.parser.BallerinaParser.ActionInvocationExpressionContext;
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
import org.wso2.ballerina.core.parser.BallerinaParser.ExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.FunctionDefinitionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.FunctionInvocationExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.ImportDeclarationContext;
import org.wso2.ballerina.core.parser.BallerinaParser.LiteralExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.LiteralValueContext;
import org.wso2.ballerina.core.parser.BallerinaParser.PackageDeclarationContext;
import org.wso2.ballerina.core.parser.BallerinaParser.StatementContext;
import org.wso2.ballerina.core.parser.BallerinaParser.TemplateExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.TypeCastingExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.UnaryExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.VariableDeclarationContext;
import org.wso2.ballerina.core.parser.BallerinaParser.VariableReferenceExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.WhileStatementContext;

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
        boolean isPublicFunction = !ctx.getTokens(30)
                .isEmpty(); //since function body cannot have public keyword inside.

        List<VariableDcl> variableDclList = new ArrayList<>();

        for (VariableDeclarationContext variableDeclarationContext : ctx.functionBody().variableDeclaration()) {
            variableDclList.add(parserVariableDclCtx(variableDeclarationContext));
        }

        Statement[] statementArray = new Statement[ctx.functionBody().statement().size()];
        for (int i = 0; i < ctx.functionBody().statement().size(); i++) {
            statementArray[i] = parserStatementCtx(ctx.functionBody().statement(i).getChild(0));
        }

        Function function = new Function(functionName, isPublicFunction, null, null, null, null, variableDclList, null,
                new BlockStmt(statementArray)); //getting only while statement

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
            // todo debug and fix
            return null;

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

        } else if (ctx instanceof WhileStatementContext) {
            WhileStatementContext whileStmtCtx = (WhileStatementContext) ctx;
            //todo getChild(0) because function body has statement+
            Statement[] statementArray = new Statement[whileStmtCtx.statement().size()];
            for (int i = 0; i < whileStmtCtx.statement().size(); i++) {
                statementArray[i] = parserStatementCtx(whileStmtCtx.statement(i).getChild(0));
            }
            return new WhileStmt(parserExpressionCtx(whileStmtCtx.expression()), new BlockStmt(statementArray));

        }
        return null;
    }

    private VariableDcl parserVariableDclCtx(VariableDeclarationContext ctx) {
        //todo type is hard coded
        //todo value should be removed from constructor
        return new VariableDcl(new IntType(), new Identifier(ctx.Identifier().getText()), null);
    }

    @Override
    public void exitLiteralExpression(LiteralExpressionContext ctx) {
        super.exitLiteralExpression(ctx);
    }

}
