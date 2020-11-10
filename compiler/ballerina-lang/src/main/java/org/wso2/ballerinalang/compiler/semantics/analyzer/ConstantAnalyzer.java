/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Stack;

/**
 * Validate Given constant expressions.
 *
 * @since JBallerina 1.0.0
 */
public class ConstantAnalyzer extends BLangNodeVisitor {

    private static final CompilerContext.Key<ConstantAnalyzer> CONSTANT_ANALYZER_KEY =
            new CompilerContext.Key<>();
    private BLangDiagnosticLog dlog;
    private Stack<BLangExpression> expressions = new Stack<>();

    private ConstantAnalyzer(CompilerContext context) {

        context.put(CONSTANT_ANALYZER_KEY, this);
        this.dlog = BLangDiagnosticLog.getInstance(context);
    }

    public static ConstantAnalyzer getInstance(CompilerContext context) {

        ConstantAnalyzer constantAnalyzer = context.get(CONSTANT_ANALYZER_KEY);
        if (constantAnalyzer == null) {
            constantAnalyzer = new ConstantAnalyzer(context);
        }
        return constantAnalyzer;
    }

    @Override
    public void visit(BLangConstant constant) {

        analyzeExpr(constant.expr);
    }

    @Override
    public void visit(BLangLiteral literal) {
        // Ignore
    }

    @Override
    public void visit(BLangNumericLiteral literal) {
        // Ignore
    }

    @Override
    public void visit(BLangSimpleVarRef varRef) {

        BSymbol symbol = varRef.symbol;
        // Symbol can be null in some invalid scenarios. Eg - const string m = { name: "Ballerina" };
        if (symbol != null && (symbol.tag & SymTag.CONSTANT) != SymTag.CONSTANT) {
            dlog.error(varRef.pos, DiagnosticCode.EXPRESSION_IS_NOT_A_CONSTANT_EXPRESSION);
        }
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {

        for (RecordLiteralNode.RecordField field : recordLiteral.fields) {
            if (field.isKeyValueField()) {
                BLangRecordLiteral.BLangRecordKeyValueField keyValuePair =
                        (BLangRecordLiteral.BLangRecordKeyValueField) field;
                analyzeExpr(keyValuePair.key.expr);
                analyzeExpr(keyValuePair.valueExpr);
            } else if (field.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                analyzeExpr((BLangRecordLiteral.BLangRecordVarNameField) field);
            } else {
                analyzeExpr(((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr);
            }
        }
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {

        for (int i = expressions.size() - 1; i >= 0; i--) {
            NodeKind kind = expressions.get(i).getKind();
            if (kind == NodeKind.GROUP_EXPR) {
                continue;
            }
            if (kind != NodeKind.BINARY_EXPR) {
                dlog.error(binaryExpr.pos, DiagnosticCode.CONSTANT_EXPRESSION_NOT_SUPPORTED);
            }
        }
        analyzeExpr(binaryExpr.lhsExpr);
        analyzeExpr(binaryExpr.rhsExpr);
    }

    public void visit(BLangGroupExpr expr) {

        analyzeExpr(expr.expression);
    }

    void analyzeExpr(BLangExpression expr) {

        switch (expr.getKind()) {
            case LITERAL:
            case NUMERIC_LITERAL:
            case RECORD_LITERAL_EXPR:
            case SIMPLE_VARIABLE_REF:
            case BINARY_EXPR:
            case GROUP_EXPR:
                this.expressions.push(expr);
                expr.accept(this);
                this.expressions.pop();
                return;
        }
        dlog.error(expr.pos, DiagnosticCode.EXPRESSION_IS_NOT_A_CONSTANT_EXPRESSION);
    }

    /**
     * Checks whether the given expression type is allowed as an expression in a constant.
     *
     * @param expression the expression which needs to be checked
     * @return {@code true} if the given expression is allowed, {@code false} otherwise.
     */
    static boolean isValidConstantExpressionNode(BLangExpression expression) {

        switch (expression.getKind()) {
            case LITERAL:
            case NUMERIC_LITERAL:
            case RECORD_LITERAL_EXPR:
            case BINARY_EXPR:
            case GROUP_EXPR:
                return true;
        }
        return false;
    }
}
