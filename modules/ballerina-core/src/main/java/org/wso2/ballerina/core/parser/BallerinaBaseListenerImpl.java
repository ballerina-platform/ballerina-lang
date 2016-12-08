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
import org.wso2.ballerina.core.model.Operator;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.BinaryArithmeticExpression;
import org.wso2.ballerina.core.model.expressions.BinaryCompareExpression;
import org.wso2.ballerina.core.model.expressions.BinaryEqualityExpression;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.types.IntType;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.StringValue;
import org.wso2.ballerina.core.parser.BallerinaParser.AssignmentStatementContext;
import org.wso2.ballerina.core.parser.BallerinaParser.BinaryComparisonExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.BinaryEqualExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.BinaryMulDivPercentExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.BinaryPlusMinusExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.ExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.FunctionDefinitionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.FunctionInvocationExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.ImportDeclarationContext;
import org.wso2.ballerina.core.parser.BallerinaParser.LiteralExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.PackageDeclarationContext;
import org.wso2.ballerina.core.parser.BallerinaParser.StatementContext;
import org.wso2.ballerina.core.parser.BallerinaParser.TemplateExpressionContext;
import org.wso2.ballerina.core.parser.BallerinaParser.VariableDeclarationContext;
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
        //todo function body should be list of statement
        Function function = new Function(functionName, isPublicFunction, null, null, null, null, variableDclList, null,
                parserStatementCtx(ctx.functionBody().statement(3).getChild(0))); //getting only while statement

        balFile.addFunction(function);

    }

    @Override
    public void exitStatement(StatementContext ctx) {
        //Statement statement = parserStatementCtx(ctx.getChild(0));//todo can it have multiple children
    }

    private Expression parserExpressionCtx(ParseTree ctx) {
        if (ctx instanceof BinaryPlusMinusExpressionContext) {
            BinaryPlusMinusExpressionContext binaryPlusMinusExpCtx = (BinaryPlusMinusExpressionContext) ctx;

            return new BinaryArithmeticExpression(parserExpressionCtx(binaryPlusMinusExpCtx.children.get(0)),
                    getOperatorName(binaryPlusMinusExpCtx.children.get(1).getText()),
                    parserExpressionCtx(binaryPlusMinusExpCtx.children.get(2)));

        } else if (ctx instanceof BinaryMulDivPercentExpressionContext) {

            BinaryMulDivPercentExpressionContext binaryMulDivExpCtx = (BinaryMulDivPercentExpressionContext) ctx;

            return new BinaryArithmeticExpression(parserExpressionCtx(binaryMulDivExpCtx.children.get(0)),
                    getOperatorName(binaryMulDivExpCtx.children.get(1).getText()),
                    parserExpressionCtx(binaryMulDivExpCtx.children.get(2)));

        } else if (ctx instanceof BinaryEqualExpressionContext) {
            BinaryEqualExpressionContext binaryEqualExpCtx = (BinaryEqualExpressionContext) ctx;

            return new BinaryEqualityExpression(parserExpressionCtx(binaryEqualExpCtx.children.get(0)),
                    getOperatorName(binaryEqualExpCtx.children.get(1).getText()),
                    parserExpressionCtx(binaryEqualExpCtx.children.get(2)));

        } else if (ctx instanceof BinaryComparisonExpressionContext) {
            BinaryComparisonExpressionContext binaryComparisonExpCtx = (BinaryComparisonExpressionContext) ctx;

            return new BinaryCompareExpression(parserExpressionCtx(binaryComparisonExpCtx.children.get(0)),
                    getOperatorName(binaryComparisonExpCtx.children.get(1).getText()),
                    parserExpressionCtx(binaryComparisonExpCtx.children.get(2)));

        } else if (ctx instanceof LiteralExpressionContext) {
            LiteralExpressionContext literalExpCtx = (LiteralExpressionContext) ctx;

            return new BasicLiteral(new BValueRef(new StringValue(literalExpCtx.primary().getText())));

        } else if (ctx instanceof TemplateExpressionContext) {
            TemplateExpressionContext templateExpCtx = (TemplateExpressionContext) ctx;

            return new BasicLiteral(new BValueRef(new StringValue(templateExpCtx.backtickString().getText())));
        } else if (ctx instanceof FunctionInvocationExpressionContext) {
            FunctionInvocationExpressionContext funExpCtx = (FunctionInvocationExpressionContext) ctx;

            List<Expression> expressionList = new ArrayList<>();

            for (ExpressionContext expressionContext : funExpCtx.argumentList().expressionList().expression()) {
                expressionList.add(parserExpressionCtx(expressionContext));
            }

            return new FunctionInvocationExpr(new Identifier(funExpCtx.functionName().getText()), expressionList);
        }

        return null;
    }

    private Statement parserStatementCtx(ParseTree ctx) {
        if (ctx instanceof AssignmentStatementContext) {
            AssignmentStatementContext assignmentStmtCtx = (AssignmentStatementContext) ctx;

            VariableRefExpr variableRefExpr = new VariableRefExpr(
                    new Identifier(assignmentStmtCtx.variableAccessor().getText()));

            Expression expression = parserExpressionCtx(assignmentStmtCtx.expression());
            return new AssignStmt(variableRefExpr, expression);

        } else if (ctx instanceof WhileStatementContext) {
            WhileStatementContext whileStmtCtx = (WhileStatementContext) ctx;
            //todo whileBody should be list of statement

            //todo getChild(0) because function body has statement+
            return new WhileStmt(parserExpressionCtx(whileStmtCtx.expression()),
                    parserStatementCtx(whileStmtCtx.statement(0).getChild(0)));

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

    private Operator getOperatorName(String op) {

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
