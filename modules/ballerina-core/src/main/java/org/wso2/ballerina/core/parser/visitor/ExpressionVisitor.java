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
import org.wso2.ballerina.core.model.Operator;
import org.wso2.ballerina.core.model.SymbolName;
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
import org.wso2.ballerina.core.model.expressions.SubtractExpression;
import org.wso2.ballerina.core.model.expressions.UnaryExpression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.BooleanValue;
import org.wso2.ballerina.core.model.values.FloatValue;
import org.wso2.ballerina.core.model.values.IntValue;
import org.wso2.ballerina.core.model.values.StringValue;
import org.wso2.ballerina.core.parser.BallerinaBaseVisitor;
import org.wso2.ballerina.core.parser.BallerinaParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor for expressions
 */
public class ExpressionVisitor extends BallerinaBaseVisitor {

    private SymbolTable expressionSymbolTable;

    public ExpressionVisitor(SymbolTable parentSymbolTable) {
        this.expressionSymbolTable = new SymbolTable(parentSymbolTable);
    }

    /**
     * Visit a parse tree produced by {@link BallerinaParser#expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    @Override
    public Object visitExpression(BallerinaParser.ExpressionContext ctx) {
        if (ctx instanceof BallerinaParser.LiteralExpressionContext) {
            return this.visitLiteralExpression((BallerinaParser.LiteralExpressionContext) ctx);
        } else if (ctx instanceof BallerinaParser.VariableReferenceExpressionContext) {
            return this.visitVariableReferenceExpression((BallerinaParser.VariableReferenceExpressionContext) ctx);
        } else if (ctx instanceof BallerinaParser.TemplateExpressionContext) {
            return this.visitTemplateExpression((BallerinaParser.TemplateExpressionContext) ctx);
        } else if (ctx instanceof BallerinaParser.FunctionInvocationExpressionContext) {
            return this.visitFunctionInvocationExpression((BallerinaParser.FunctionInvocationExpressionContext) ctx);
        } else if (ctx instanceof BallerinaParser.ActionInvocationExpressionContext) {
            return this.visitActionInvocationExpression((BallerinaParser.ActionInvocationExpressionContext) ctx);
        } else if (ctx instanceof BallerinaParser.TypeCastingExpressionContext) {
            return this.visitTypeCastingExpression((BallerinaParser.TypeCastingExpressionContext) ctx);
        } else if (ctx instanceof BallerinaParser.UnaryExpressionContext) {
            return this.visitUnaryExpression((BallerinaParser.UnaryExpressionContext) ctx);
        } else if (ctx instanceof BallerinaParser.BracedExpressionContext) {
            return this.visitBracedExpression((BallerinaParser.BracedExpressionContext) ctx);
        } else if (ctx instanceof BallerinaParser.BinaryPowExpressionContext) {
            return this.visitBinaryPowExpression((BallerinaParser.BinaryPowExpressionContext) ctx);
        } else if (ctx instanceof BallerinaParser.BinaryDivitionExpressionContext) {
            return this.visitBinaryDivitionExpression((BallerinaParser.BinaryDivitionExpressionContext) ctx);
        } else if (ctx instanceof BallerinaParser.BinaryMultiplicationExpressionContext) {
            return this.visitBinaryMultiplicationExpression(
                    (BallerinaParser.BinaryMultiplicationExpressionContext) ctx);
        } else if (ctx instanceof BallerinaParser.BinaryModExpressionContext) {
            return this.visitBinaryModExpression((BallerinaParser.BinaryModExpressionContext) ctx);
        } else if (ctx instanceof BallerinaParser.BinaryAndExpressionContext) {
            return this.visitBinaryAndExpression((BallerinaParser.BinaryAndExpressionContext) ctx);
        } else if (ctx instanceof BallerinaParser.BinaryAddExpressionContext) {
            return this.visitBinaryAddExpression((BallerinaParser.BinaryAddExpressionContext) ctx);
        } else if (ctx instanceof BallerinaParser.BinarySubExpressionContext) {
            return this.visitBinarySubExpression((BallerinaParser.BinarySubExpressionContext) ctx);
        } else if (ctx instanceof BallerinaParser.BinaryOrExpressionContext) {
            return this.visitBinaryOrExpression((BallerinaParser.BinaryOrExpressionContext) ctx);
        } else if (ctx instanceof BallerinaParser.BinaryGTExpressionContext) {
            return this.visitBinaryGTExpression((BallerinaParser.BinaryGTExpressionContext) ctx);
        } else if (ctx instanceof BallerinaParser.BinaryGEExpressionContext) {
            return this.visitBinaryGEExpression((BallerinaParser.BinaryGEExpressionContext) ctx);
        } else if (ctx instanceof BallerinaParser.BinaryLTExpressionContext) {
            return this.visitBinaryLTExpression((BallerinaParser.BinaryLTExpressionContext) ctx);
        } else if (ctx instanceof BallerinaParser.BinaryLEExpressionContext) {
            return this.visitBinaryLEExpression((BallerinaParser.BinaryLEExpressionContext) ctx);
        } else if (ctx instanceof BallerinaParser.BinaryEqualExpressionContext) {
            return this.visitBinaryEqualExpression((BallerinaParser.BinaryEqualExpressionContext) ctx);
        } else if (ctx instanceof BallerinaParser.BinaryNotEqualExpressionContext) {
            return this.visitBinaryNotEqualExpression((BallerinaParser.BinaryNotEqualExpressionContext) ctx);
        }
        return super.visitExpression(ctx);
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
    public Object visitBinaryOrExpression(BallerinaParser.BinaryOrExpressionContext ctx) {
        return new OrExpression((Expression) visitExpression((BallerinaParser.ExpressionContext) ctx.children.get(0)),
                (Expression) visitExpression((BallerinaParser.ExpressionContext) ctx.children.get(2)));
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
    public Object visitBinaryGTExpression(BallerinaParser.BinaryGTExpressionContext ctx) {
        return new GreaterThanExpression((Expression) visitExpression(
                (BallerinaParser.ExpressionContext) ctx.children.get(0)),
                (Expression) visitExpression((BallerinaParser.ExpressionContext) ctx.children.get(2)));
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
    public Object visitTypeInitializeExpression(BallerinaParser.TypeInitializeExpressionContext ctx) {
        return super.visitTypeInitializeExpression(ctx);
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
    public Object visitTemplateExpression(BallerinaParser.TemplateExpressionContext ctx) {
        // xml or json as sting
        return new BasicLiteral(new BValueRef(new StringValue(ctx.getText())));
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
    public Object visitBinaryLEExpression(BallerinaParser.BinaryLEExpressionContext ctx) {
        return new LessEqualExpression((Expression) visitExpression(
                (BallerinaParser.ExpressionContext) ctx.children.get(0)),
                (Expression) visitExpression((BallerinaParser.ExpressionContext) ctx.children.get(2)));
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
    public Object visitFunctionInvocationExpression(BallerinaParser.FunctionInvocationExpressionContext ctx) {
        List<Expression> expressionList = new ArrayList<>();
        for (BallerinaParser.ExpressionContext expressionContext : ctx.argumentList().expressionList().expression()) {
            expressionList.add((Expression) this.visitExpression(expressionContext));
        }
        return new FunctionInvocationExpr(new SymbolName(ctx.functionName().getText()), expressionList);
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
    public Object visitBinaryGEExpression(BallerinaParser.BinaryGEExpressionContext ctx) {
        return new GreaterEqualExpression((Expression) visitExpression(
                (BallerinaParser.ExpressionContext) ctx.children.get(0)),
                (Expression) visitExpression((BallerinaParser.ExpressionContext) ctx.children.get(2)));
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
    public Object visitBinaryEqualExpression(BallerinaParser.BinaryEqualExpressionContext ctx) {
        return new EqualExpression((Expression) visitExpression(
                (BallerinaParser.ExpressionContext) ctx.children.get(0)),
                (Expression) visitExpression((BallerinaParser.ExpressionContext) ctx.children.get(2)));
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
    public Object visitBracedExpression(BallerinaParser.BracedExpressionContext ctx) {
        // todo braced expression
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
    public Object visitVariableReferenceExpression(BallerinaParser.VariableReferenceExpressionContext ctx) {
        //todo can extract whether simple, map , array or struct reference
        return new VariableRefExpr(new SymbolName(ctx.variableReference().getText()));
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
    public Object visitActionInvocationExpression(BallerinaParser.ActionInvocationExpressionContext ctx) {
        //todo action invocation expression
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
    public Object visitTypeCastingExpression(BallerinaParser.TypeCastingExpressionContext ctx) {
        // todo type cast expression
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
    public Object visitBinaryAndExpression(BallerinaParser.BinaryAndExpressionContext ctx) {
        return new AndExpression((Expression) visitExpression((BallerinaParser.ExpressionContext) ctx.children.get(0)),
                (Expression) visitExpression((BallerinaParser.ExpressionContext) ctx.children.get(2)));
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
    public Object visitBinaryNotEqualExpression(BallerinaParser.BinaryNotEqualExpressionContext ctx) {
        return new NotEqualExpression((Expression) visitExpression(
                (BallerinaParser.ExpressionContext) ctx.children.get(0)),
                (Expression) visitExpression((BallerinaParser.ExpressionContext) ctx.children.get(2)));
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
    public Object visitBinaryDivitionExpression(BallerinaParser.BinaryDivitionExpressionContext ctx) {
        //todo implement division operator
        return null;    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitBinaryModExpression(BallerinaParser.BinaryModExpressionContext ctx) {
        //todo implement mod operator
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
    public Object visitBinarySubExpression(BallerinaParser.BinarySubExpressionContext ctx) {
        return new SubtractExpression((Expression) this.visitExpression(
                (BallerinaParser.ExpressionContext) ctx.children.get(0)),
                (Expression) visitExpression((BallerinaParser.ExpressionContext) ctx.children.get(2)));
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
    public Object visitBinaryMultiplicationExpression(BallerinaParser.BinaryMultiplicationExpressionContext ctx) {
        return new MultExpression((Expression) this.visitExpression(
                (BallerinaParser.ExpressionContext) ctx.children.get(0)),
                (Expression) visitExpression((BallerinaParser.ExpressionContext) ctx.children.get(2)));
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
    public Object visitLiteralExpression(BallerinaParser.LiteralExpressionContext ctx) {
        BallerinaParser.LiteralValueContext literalValue = ctx.literalValue();
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
    public Object visitUnaryExpression(BallerinaParser.UnaryExpressionContext ctx) {
        return new UnaryExpression(getOpName(ctx.getChild(0).getText()),
                (Expression) this.visitExpression(ctx.expression()));
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
    public Object visitBinaryLTExpression(BallerinaParser.BinaryLTExpressionContext ctx) {
        return new LessThanExpression((Expression) this.visitExpression(
                (BallerinaParser.ExpressionContext) ctx.children.get(0)),
                (Expression) visitExpression((BallerinaParser.ExpressionContext) ctx.children.get(2)));
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
    public Object visitMapInitializerExpression(BallerinaParser.MapInitializerExpressionContext ctx) {
        return super.visitMapInitializerExpression(ctx);
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
    public Object visitBinaryPowExpression(BallerinaParser.BinaryPowExpressionContext ctx) {
        // todo implement pow operator
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
    public Object visitBinaryAddExpression(BallerinaParser.BinaryAddExpressionContext ctx) {
        return new AddExpression((Expression) this.visitExpression(
                (BallerinaParser.ExpressionContext) ctx.children.get(0)),
                (Expression) visitExpression((BallerinaParser.ExpressionContext) ctx.children.get(2)));
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

    /**
     * Base method for retrieving the symbol table
     *
     * @return symbol table for this instance
     */
    @Override
    public SymbolTable getSymbolTable() {
        return this.expressionSymbolTable;
    }
}
