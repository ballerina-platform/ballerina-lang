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
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.tree.BLangConstantValue;
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
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 0.990.4
 */
public class ConstantValueResolver extends BLangNodeVisitor {

    private static final CompilerContext.Key<ConstantValueResolver> CONSTANT_VALUE_RESOLVER_KEY =
            new CompilerContext.Key<>();
    private BConstantSymbol currentConstSymbol;
    private BLangConstantValue result;
    private BLangDiagnosticLog dlog;
    private DiagnosticPos currentPos;
    private Map<BConstantSymbol, BLangConstant> unresolvedConstants = new HashMap<>();

    private ConstantValueResolver(CompilerContext context) {
        context.put(CONSTANT_VALUE_RESOLVER_KEY, this);
        this.dlog = BLangDiagnosticLog.getInstance(context);
    }

    public static ConstantValueResolver getInstance(CompilerContext context) {
        ConstantValueResolver constantValueResolver = context.get(CONSTANT_VALUE_RESOLVER_KEY);
        if (constantValueResolver == null) {
            constantValueResolver = new ConstantValueResolver(context);
        }
        return constantValueResolver;
    }

    public void resolve(List<BLangConstant> constants) {
        constants.forEach(constant -> this.unresolvedConstants.put(constant.symbol, constant));
        constants.forEach(constant -> constant.accept(this));
    }

    @Override
    public void visit(BLangConstant constant) {
        BConstantSymbol tempCurrentConstSymbol = this.currentConstSymbol;
        this.currentConstSymbol = constant.symbol;
        this.currentConstSymbol.value = visitExpr(constant.expr);
        unresolvedConstants.remove(this.currentConstSymbol);
        this.currentConstSymbol = tempCurrentConstSymbol;
    }

    @Override
    public void visit(BLangLiteral literal) {
        this.result = new BLangConstantValue(literal.value, literal.type);
    }

    @Override
    public void visit(BLangNumericLiteral literal) {
        this.result = new BLangConstantValue(literal.value, literal.type);
    }

    @Override
    public void visit(BLangSimpleVarRef varRef) {
        if (varRef.symbol == null || (varRef.symbol.tag & SymTag.CONSTANT) != SymTag.CONSTANT) {
            this.result = null;
            return;
        }

        BConstantSymbol constSymbol = (BConstantSymbol) varRef.symbol;
        BLangConstantValue constVal = constSymbol.value;
        if (constVal != null) {
            this.result = constVal;
            return;
        }

        // if the referring constant is not yet resolved, then go and resolve it first.
        this.unresolvedConstants.get(varRef.symbol).accept(this);
        this.result = constSymbol.value;
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        Map<String, BLangConstantValue> mapConstVal = new HashMap<>();
        for (RecordLiteralNode.RecordField field : recordLiteral.fields) {
            String key;
            BLangConstantValue value;

            if (field.isKeyValueField()) {
                BLangRecordLiteral.BLangRecordKeyValueField keyValuePair =
                        (BLangRecordLiteral.BLangRecordKeyValueField) field;
                NodeKind nodeKind = keyValuePair.key.expr.getKind();

                if (nodeKind == NodeKind.LITERAL || nodeKind == NodeKind.NUMERIC_LITERAL) {
                    key = (String) ((BLangLiteral) keyValuePair.key.expr).value;
                } else if (nodeKind == NodeKind.SIMPLE_VARIABLE_REF) {
                    key = ((BLangSimpleVarRef) keyValuePair.key.expr).variableName.value;
                } else {
                    continue;
                }

                value = visitExpr(keyValuePair.valueExpr);
            } else if (field.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                BLangRecordLiteral.BLangRecordVarNameField varNameField =
                        (BLangRecordLiteral.BLangRecordVarNameField) field;
                key = varNameField.variableName.value;
                value = visitExpr(varNameField);
            } else {
                BLangConstantValue spreadOpConstValue =
                        visitExpr(((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr);

                if (spreadOpConstValue != null) {
                    mapConstVal.putAll((Map<String, BLangConstantValue>) spreadOpConstValue.value);
                }
                continue;
            }

            mapConstVal.put(key, value);
        }

        this.result = new BLangConstantValue(mapConstVal, recordLiteral.type);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {

        BLangConstantValue lhs = visitExpr(binaryExpr.lhsExpr);
        BLangConstantValue rhs = visitExpr(binaryExpr.rhsExpr);
        this.result = calculateConstValue(lhs, rhs, binaryExpr.opKind);
    }

    public void visit(BLangGroupExpr groupExpr) {

        this.result = visitExpr(groupExpr.expression);
    }

    private BLangConstantValue calculateConstValue(BLangConstantValue lhs, BLangConstantValue rhs, OperatorKind kind) {

        if (lhs == null || rhs == null || lhs.value == null || rhs.value == null) {
            // This is a compilation error.
            // This is to avoid NPE exceptions in sub-sequent validations.
            return new BLangConstantValue(null, this.currentConstSymbol.type);
        }
        // See Types.isAllowedConstantType() for supported types.
        try {
            switch (kind) {
                case ADD:
                    return calculateAddition(lhs, rhs);
                case SUB:
                    return calculateSubtract(lhs, rhs);
                case MUL:
                    return calculateMultiplication(lhs, rhs);
                case DIV:
                    return calculateDivision(lhs, rhs);
            }
        } catch (NumberFormatException nfe) {
            // Ignore. This will be handled as a compiler error.
        } catch (ArithmeticException ae) {
            dlog.error(currentPos, DiagnosticCode.INVALID_CONST_EXPRESSION, ae.getMessage());
        }
        // This is a compilation error already logged.
        // This is to avoid NPE exceptions in sub-sequent validations.
        return new BLangConstantValue(null, this.currentConstSymbol.type);
    }

    private BLangConstantValue calculateAddition(BLangConstantValue lhs, BLangConstantValue rhs) {
        // TODO : Handle overflow in numeric values.
        Object result = null;
        switch (this.currentConstSymbol.type.tag) {
            case TypeTags.INT:
            case TypeTags.BYTE: // Byte will be a compiler error.
                result = (Long) lhs.value + (Long) rhs.value;
                break;
            case TypeTags.FLOAT:
                result = String.valueOf(Double.parseDouble(String.valueOf(lhs.value))
                        + Double.parseDouble(String.valueOf(rhs.value)));
                break;
            case TypeTags.DECIMAL:
                BigDecimal lhsDecimal = new BigDecimal(String.valueOf(lhs.value), MathContext.DECIMAL128);
                BigDecimal rhsDecimal = new BigDecimal(String.valueOf(rhs.value), MathContext.DECIMAL128);
                BigDecimal resultDecimal = lhsDecimal.add(rhsDecimal, MathContext.DECIMAL128);
                result = resultDecimal.toPlainString();
                break;
            case TypeTags.STRING:
                result = String.valueOf(lhs.value) + String.valueOf(rhs.value);
                break;
        }
        return new BLangConstantValue(result, currentConstSymbol.type);
    }

    private BLangConstantValue calculateSubtract(BLangConstantValue lhs, BLangConstantValue rhs) {

        Object result = null;
        switch (this.currentConstSymbol.type.tag) {
            case TypeTags.INT:
            case TypeTags.BYTE: // Byte will be a compiler error.
                result = (Long) lhs.value - (Long) rhs.value;
                break;
            case TypeTags.FLOAT:
                result = String.valueOf(Double.parseDouble(String.valueOf(lhs.value))
                        - Double.parseDouble(String.valueOf(rhs.value)));
                break;
            case TypeTags.DECIMAL:
                BigDecimal lhsDecimal = new BigDecimal(String.valueOf(lhs.value), MathContext.DECIMAL128);
                BigDecimal rhsDecimal = new BigDecimal(String.valueOf(rhs.value), MathContext.DECIMAL128);
                BigDecimal resultDecimal = lhsDecimal.subtract(rhsDecimal, MathContext.DECIMAL128);
                result = resultDecimal.toPlainString();
                break;
        }
        return new BLangConstantValue(result, currentConstSymbol.type);
    }

    private BLangConstantValue calculateMultiplication(BLangConstantValue lhs, BLangConstantValue rhs) {
        // TODO : Handle overflow in numeric values.
        Object result = null;
        switch (this.currentConstSymbol.type.tag) {
            case TypeTags.INT:
            case TypeTags.BYTE: // Byte will be a compiler error.
                result = (Long) lhs.value * (Long) rhs.value;
                break;
            case TypeTags.FLOAT:
                result = String.valueOf(Double.parseDouble(String.valueOf(lhs.value))
                        * Double.parseDouble(String.valueOf(rhs.value)));
                break;
            case TypeTags.DECIMAL:
                BigDecimal lhsDecimal = new BigDecimal(String.valueOf(lhs.value), MathContext.DECIMAL128);
                BigDecimal rhsDecimal = new BigDecimal(String.valueOf(rhs.value), MathContext.DECIMAL128);
                BigDecimal resultDecimal = lhsDecimal.multiply(rhsDecimal, MathContext.DECIMAL128);
                result = resultDecimal.toPlainString();
                break;
        }
        return new BLangConstantValue(result, currentConstSymbol.type);
    }

    private BLangConstantValue calculateDivision(BLangConstantValue lhs, BLangConstantValue rhs) {

        Object result = null;
        switch (this.currentConstSymbol.type.tag) {
            case TypeTags.INT:
            case TypeTags.BYTE: // Byte will be a compiler error.
                result = (Long) ((Long) lhs.value / (Long) rhs.value);
                break;
            case TypeTags.FLOAT:
                result = String.valueOf(Double.parseDouble(String.valueOf(lhs.value))
                        / Double.parseDouble(String.valueOf(rhs.value)));
                break;
            case TypeTags.DECIMAL:
                BigDecimal lhsDecimal = new BigDecimal(String.valueOf(lhs.value), MathContext.DECIMAL128);
                BigDecimal rhsDecimal = new BigDecimal(String.valueOf(rhs.value), MathContext.DECIMAL128);
                BigDecimal resultDecimal = lhsDecimal.divide(rhsDecimal, MathContext.DECIMAL128);
                result = resultDecimal.toPlainString();
                break;
        }
        return new BLangConstantValue(result, currentConstSymbol.type);
    }

    // TODO : Not working at the moment.
    private BLangConstantValue calculateMod(BLangConstantValue lhs, BLangConstantValue rhs) {

        Object result = null;
        switch (this.currentConstSymbol.type.tag) {
            case TypeTags.INT:
            case TypeTags.BYTE: // Byte will be a compiler error.
                result = (Long) ((Long) lhs.value % (Long) rhs.value);
                break;
            case TypeTags.FLOAT:
                result = String.valueOf(Double.parseDouble(String.valueOf(lhs.value))
                        % Double.parseDouble(String.valueOf(rhs.value)));
                break;
            case TypeTags.DECIMAL:
                BigDecimal lhsDecimal = new BigDecimal(String.valueOf(lhs.value), MathContext.DECIMAL128);
                BigDecimal rhsDecimal = new BigDecimal(String.valueOf(rhs.value), MathContext.DECIMAL128);
                BigDecimal resultDecimal = lhsDecimal.remainder(rhsDecimal, MathContext.DECIMAL128);
                result = resultDecimal.toPlainString();
                break;
        }
        return new BLangConstantValue(result, currentConstSymbol.type);
    }

    private BLangConstantValue visitExpr(BLangExpression node) {

        if (!node.typeChecked) {
            return null;
        }
        switch (node.getKind()) {
            case LITERAL:
            case NUMERIC_LITERAL:
            case RECORD_LITERAL_EXPR:
            case SIMPLE_VARIABLE_REF:
            case BINARY_EXPR:
            case GROUP_EXPR:
                BLangConstantValue prevResult = this.result;
                DiagnosticPos prevPos = this.currentPos;
                this.currentPos = node.pos;
                this.result = null;
                node.accept(this);
                BLangConstantValue newResult = this.result;
                this.result = prevResult;
                this.currentPos = prevPos;
                return newResult;
            default:
                return null;
        }
    }
}
