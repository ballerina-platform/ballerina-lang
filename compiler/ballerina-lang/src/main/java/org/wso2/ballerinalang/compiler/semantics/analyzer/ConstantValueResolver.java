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

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeDefinitionSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNoType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleMember;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangConstantValue;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ImmutableTypeCloner;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeDefBuilderHelper;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.BiFunction;

import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;

/**
 * @since 0.990.4
 */
public class ConstantValueResolver extends BLangNodeVisitor {

    private static final CompilerContext.Key<ConstantValueResolver> CONSTANT_VALUE_RESOLVER_KEY =
            new CompilerContext.Key<>();
    private BConstantSymbol currentConstSymbol;
    private BLangConstantValue result;
    private BLangDiagnosticLog dlog;
    private Location currentPos;
    private BLangAnonymousModelHelper anonymousModelHelper;
    private SymbolEnv symEnv;
    private Names names;
    private SymbolTable symTable;
    private Types types;
    private PackageID pkgID;
    private Map<BConstantSymbol, BLangConstant> unresolvedConstants = new HashMap<>();
    private Map<String, BLangConstantValue> constantMap = new HashMap<>();
    private ArrayList<BConstantSymbol> resolvingConstants = new ArrayList<>();
    private HashSet<BConstantSymbol> unresolvableConstants = new HashSet<>();
    private HashMap<BSymbol, BLangTypeDefinition> createdTypeDefinitions = new HashMap<>();
    private Stack<String> anonTypeNameSuffixes = new Stack<>();

    private ConstantValueResolver(CompilerContext context) {
        context.put(CONSTANT_VALUE_RESOLVER_KEY, this);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.names = Names.getInstance(context);
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
        this.types = Types.getInstance(context);
    }

    public static ConstantValueResolver getInstance(CompilerContext context) {
        ConstantValueResolver constantValueResolver = context.get(CONSTANT_VALUE_RESOLVER_KEY);
        if (constantValueResolver == null) {
            constantValueResolver = new ConstantValueResolver(context);
        }
        return constantValueResolver;
    }

    public void resolve(List<BLangConstant> constants, PackageID packageID, SymbolEnv symEnv) {
        this.dlog.setCurrentPackageId(packageID);
        this.pkgID = packageID;
        this.symEnv = symEnv;
        constants.forEach(constant -> this.unresolvedConstants.put(constant.symbol, constant));
        constants.forEach(constant -> constant.accept(this));
        constantMap.clear();
        this.createdTypeDefinitions.clear();
    }

    @Override
    public void visit(BLangConstant constant) {
        if (!unresolvedConstants.containsKey(constant.symbol)) {
            return; // Already visited.
        }
        BConstantSymbol tempCurrentConstSymbol = this.currentConstSymbol;
        this.currentConstSymbol = constant.symbol;
        this.resolvingConstants.add(this.currentConstSymbol);
        this.currentConstSymbol.value = constructBLangConstantValue(constant.expr);
        this.resolvingConstants.remove(this.currentConstSymbol);
        this.anonTypeNameSuffixes.push(constant.name.value);
        updateConstantType(constant);
        this.anonTypeNameSuffixes.pop();
        checkUniqueness(constant);
        unresolvedConstants.remove(this.currentConstSymbol);
        this.currentConstSymbol = tempCurrentConstSymbol;
    }

    @Override
    public void visit(BLangLiteral literal) {
        this.result = new BLangConstantValue(literal.value, literal.getBType());
    }

    @Override
    public void visit(BLangNumericLiteral literal) {
        this.result = new BLangConstantValue(literal.value, literal.getBType());
    }

    @Override
    public void visit(BLangConstRef constRef) {
        this.result = ((BConstantSymbol) constRef.symbol).value;
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

        if (this.currentConstSymbol == constSymbol) {
            dlog.error(varRef.pos, DiagnosticErrorCode.SELF_REFERENCE_CONSTANT, constSymbol.name);
            return;
        }

        if (!this.unresolvedConstants.containsKey(constSymbol)) {
            if (this.unresolvableConstants.contains(constSymbol)) {
                this.result = null;
                return;
            }
            this.unresolvableConstants.add(constSymbol);
            dlog.error(varRef.pos, DiagnosticErrorCode.CANNOT_RESOLVE_CONST, constSymbol.name.value);
            this.result = null;
            return;
        }

        if (this.resolvingConstants.contains(constSymbol)) {
            for (BConstantSymbol symbol : this.resolvingConstants) {
                this.unresolvableConstants.add(symbol);
            }
            dlog.error(varRef.pos, DiagnosticErrorCode.CONSTANT_CYCLIC_REFERENCE, this.resolvingConstants);
            this.result = null;
            return;
        }

        // if the referring constant is not yet resolved, then go and resolve it first.
        this.unresolvedConstants.get(constSymbol).accept(this);
        this.result = constSymbol.value;
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        Map<String, BLangConstantValue> mapConstVal = new LinkedHashMap<>();
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

                value = constructBLangConstantValue(keyValuePair.valueExpr);
            } else if (field.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                BLangRecordLiteral.BLangRecordVarNameField varNameField =
                        (BLangRecordLiteral.BLangRecordVarNameField) field;
                key = varNameField.variableName.value;
                value = constructBLangConstantValue(varNameField);
            } else {
                BLangConstantValue spreadOpConstValue =
                        constructBLangConstantValue(((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr);

                if (spreadOpConstValue != null) {
                    mapConstVal.putAll((Map<String, BLangConstantValue>) spreadOpConstValue.value);
                }
                continue;
            }

            mapConstVal.put(key, value);
        }

        this.result = new BLangConstantValue(mapConstVal, recordLiteral.getBType());
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        List<BLangExpression> memberExprs = listConstructorExpr.exprs;

        List<BLangConstantValue> listConstValue = new ArrayList<>(memberExprs.size());

        for (BLangExpression memberExpr : memberExprs) {
            listConstValue.add(constructBLangConstantValue(memberExpr));
        }

        this.result = new BLangConstantValue(listConstValue, listConstructorExpr.getBType());
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangArrayLiteral listConstructorExpr) {
        visit((BLangListConstructorExpr) listConstructorExpr);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangTupleLiteral listConstructorExpr) {
        visit((BLangListConstructorExpr) listConstructorExpr);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        BLangConstantValue lhs = constructBLangConstantValue(binaryExpr.lhsExpr);
        BLangConstantValue rhs = constructBLangConstantValue(binaryExpr.rhsExpr);
        this.result = calculateConstValue(lhs, rhs, binaryExpr.opKind);
    }

    public void visit(BLangGroupExpr groupExpr) {
        this.result = constructBLangConstantValue(groupExpr.expression);
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        BLangConstantValue value = constructBLangConstantValue(unaryExpr.expr);
        this.result = evaluateUnaryOperator(value, unaryExpr.operator);
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
                case MOD:
                    return calculateMod(lhs, rhs);
                case BITWISE_AND:
                    return calculateBitWiseOp(lhs, rhs, (a, b) -> a & b);
                case BITWISE_OR:
                    return calculateBitWiseOp(lhs, rhs, (a, b) -> a | b);
                case BITWISE_LEFT_SHIFT:
                    return calculateBitWiseOp(lhs, rhs, (a, b) -> a << b);
                case BITWISE_RIGHT_SHIFT:
                    return calculateBitWiseOp(lhs, rhs, (a, b) -> a >> b);
                case BITWISE_UNSIGNED_RIGHT_SHIFT:
                    return calculateBitWiseOp(lhs, rhs, (a, b) -> a >>> b);
                case BITWISE_XOR:
                    return calculateBitWiseOp(lhs, rhs, (a, b) -> a ^ b);
                default:
                    dlog.error(currentPos, DiagnosticErrorCode.CONSTANT_EXPRESSION_NOT_SUPPORTED);
            }
        } catch (NumberFormatException nfe) {
            // Ignore. This will be handled as a compiler error.
        } catch (ArithmeticException ae) {
            dlog.error(currentPos, DiagnosticErrorCode.INVALID_CONST_EXPRESSION, ae.getMessage());
        }
        // This is a compilation error already logged.
        // This is to avoid NPE exceptions in sub-sequent validations.
        return new BLangConstantValue(null, this.currentConstSymbol.type);
    }

    private BLangConstantValue evaluateUnaryOperator(BLangConstantValue value, OperatorKind kind) {
        if (value == null || value.value == null) {
            // This is a compilation error.
            // This is to avoid NPE exceptions in sub-sequent validations.
            return new BLangConstantValue(null, this.currentConstSymbol.type);
        }

        try {
            switch (kind) {
                case ADD:
                    return new BLangConstantValue(value.value, currentConstSymbol.type);
                case SUB:
                    return calculateNegation(value);
                case BITWISE_COMPLEMENT:
                    return calculateBitWiseComplement(value);
                case NOT:
                    return calculateBooleanComplement(value);
            }
        } catch (ClassCastException ce) {
            // Ignore. This will be handled as a compiler error.
        }
        // This is a compilation error already logged.
        // This is to avoid NPE exceptions in sub-sequent validations.
        return new BLangConstantValue(null, this.currentConstSymbol.type);
    }

    private BLangConstantValue calculateBitWiseOp(BLangConstantValue lhs, BLangConstantValue rhs,
                                                  BiFunction<Long, Long, Long> func) {
        switch (Types.getReferredType(this.currentConstSymbol.type).tag) {
            case TypeTags.INT:
                Long val = func.apply((Long) lhs.value, (Long) rhs.value);
                return new BLangConstantValue(val, this.currentConstSymbol.type);
            default:
                dlog.error(currentPos, DiagnosticErrorCode.CONSTANT_EXPRESSION_NOT_SUPPORTED);

        }
        return new BLangConstantValue(null, this.currentConstSymbol.type);
    }

    private BLangConstantValue calculateAddition(BLangConstantValue lhs, BLangConstantValue rhs) {
        Object result = null;
        switch (Types.getReferredType(this.currentConstSymbol.type).tag) {
            case TypeTags.INT:
            case TypeTags.BYTE: // Byte will be a compiler error.
                try {
                    result = Math.addExact((Long) lhs.value, (Long) rhs.value);
                } catch (ArithmeticException ae) {
                    dlog.error(currentPos, DiagnosticErrorCode.INT_RANGE_OVERFLOW_ERROR);
                }
                break;
            case TypeTags.FLOAT:
                result = String.valueOf(Double.parseDouble(String.valueOf(lhs.value))
                        + Double.parseDouble(String.valueOf(rhs.value)));
                break;
            case TypeTags.DECIMAL:
                BigDecimal lhsDecimal = new BigDecimal(String.valueOf(lhs.value), MathContext.DECIMAL128);
                BigDecimal rhsDecimal = new BigDecimal(String.valueOf(rhs.value), MathContext.DECIMAL128);
                BigDecimal resultDecimal = lhsDecimal.add(rhsDecimal, MathContext.DECIMAL128);
                resultDecimal = types.getValidDecimalNumber(currentPos, resultDecimal);
                result = resultDecimal != null ? resultDecimal.toPlainString() : null;
                break;
            case TypeTags.STRING:
                result = String.valueOf(lhs.value) + String.valueOf(rhs.value);
                break;
        }
        return new BLangConstantValue(result, currentConstSymbol.type);
    }

    private BLangConstantValue calculateSubtract(BLangConstantValue lhs, BLangConstantValue rhs) {
        Object result = null;
        switch (Types.getReferredType(this.currentConstSymbol.type).tag) {
            case TypeTags.INT:
            case TypeTags.BYTE: // Byte will be a compiler error.
                try {
                    result = Math.subtractExact((Long) lhs.value, (Long) rhs.value);
                } catch (ArithmeticException ae) {
                    dlog.error(currentPos, DiagnosticErrorCode.INT_RANGE_OVERFLOW_ERROR);
                }
                break;
            case TypeTags.FLOAT:
                result = String.valueOf(Double.parseDouble(String.valueOf(lhs.value))
                        - Double.parseDouble(String.valueOf(rhs.value)));
                break;
            case TypeTags.DECIMAL:
                BigDecimal lhsDecimal = new BigDecimal(String.valueOf(lhs.value), MathContext.DECIMAL128);
                BigDecimal rhsDecimal = new BigDecimal(String.valueOf(rhs.value), MathContext.DECIMAL128);
                BigDecimal resultDecimal = lhsDecimal.subtract(rhsDecimal, MathContext.DECIMAL128);
                resultDecimal = types.getValidDecimalNumber(currentPos, resultDecimal);
                result = resultDecimal != null ? resultDecimal.toPlainString() : null;
                break;
        }
        return new BLangConstantValue(result, currentConstSymbol.type);
    }

    private BLangConstantValue calculateMultiplication(BLangConstantValue lhs, BLangConstantValue rhs) {
        Object result = null;
        switch (Types.getReferredType(this.currentConstSymbol.type).tag) {
            case TypeTags.INT:
            case TypeTags.BYTE: // Byte will be a compiler error.
                try {
                    result = Math.multiplyExact((Long) lhs.value, (Long) rhs.value);
                } catch (ArithmeticException ae) {
                    dlog.error(currentPos, DiagnosticErrorCode.INT_RANGE_OVERFLOW_ERROR);
                }
                break;
            case TypeTags.FLOAT:
                result = String.valueOf(Double.parseDouble(String.valueOf(lhs.value))
                        * Double.parseDouble(String.valueOf(rhs.value)));
                break;
            case TypeTags.DECIMAL:
                BigDecimal lhsDecimal = new BigDecimal(String.valueOf(lhs.value), MathContext.DECIMAL128);
                BigDecimal rhsDecimal = new BigDecimal(String.valueOf(rhs.value), MathContext.DECIMAL128);
                BigDecimal resultDecimal = lhsDecimal.multiply(rhsDecimal, MathContext.DECIMAL128);
                resultDecimal = types.getValidDecimalNumber(currentPos, resultDecimal);
                result = resultDecimal != null ? resultDecimal.toPlainString() : null;
                break;
        }
        return new BLangConstantValue(result, currentConstSymbol.type);
    }

    private BLangConstantValue calculateDivision(BLangConstantValue lhs, BLangConstantValue rhs) {
        Object result = null;
        switch (Types.getReferredType(this.currentConstSymbol.type).tag) {
            case TypeTags.INT:
            case TypeTags.BYTE: // Byte will be a compiler error.
                if ((Long) lhs.value == Long.MIN_VALUE && (Long) rhs.value == -1) {
                    dlog.error(currentPos, DiagnosticErrorCode.INT_RANGE_OVERFLOW_ERROR);
                    return new BLangConstantValue(null, this.currentConstSymbol.type);
                }
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
                resultDecimal = types.getValidDecimalNumber(currentPos, resultDecimal);
                result = resultDecimal != null ? resultDecimal.toPlainString() : null;
                break;
        }
        return new BLangConstantValue(result, currentConstSymbol.type);
    }

    private BLangConstantValue calculateMod(BLangConstantValue lhs, BLangConstantValue rhs) {
        Object result = null;
        switch (Types.getReferredType(this.currentConstSymbol.type).tag) {
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

    private Object calculateNegationForInt(BLangConstantValue value) {
        if ((Long) (value.value) == Long.MIN_VALUE) {
            dlog.error(currentPos, DiagnosticErrorCode.INT_RANGE_OVERFLOW_ERROR);
            return new BLangConstantValue(null, this.currentConstSymbol.type);
        }
        return -1 * ((Long) (value.value));
    }

    private Object calculateNegationForFloat(BLangConstantValue value) {
        return String.valueOf(-1 * Double.parseDouble(String.valueOf(value.value)));
    }

    private Object calculateNegationForDecimal(BLangConstantValue value) {
        BigDecimal valDecimal = new BigDecimal(String.valueOf(value.value), MathContext.DECIMAL128);
        BigDecimal negDecimal = new BigDecimal(String.valueOf(-1), MathContext.DECIMAL128);
        BigDecimal resultDecimal = valDecimal.multiply(negDecimal, MathContext.DECIMAL128);
        return resultDecimal.toPlainString();
    }

    private BLangConstantValue calculateNegation(BLangConstantValue value) {
        Object result = null;
        BType constSymbolValType = value.type;
        int constSymbolValTypeTag = constSymbolValType.tag;

        switch (constSymbolValTypeTag) {
            case TypeTags.INT:
                result = calculateNegationForInt(value);
                break;
            case TypeTags.FLOAT:
                result = calculateNegationForFloat(value);
                break;
            case TypeTags.DECIMAL:
                result = calculateNegationForDecimal(value);
                break;
        }

        return new BLangConstantValue(result, constSymbolValType);
    }

    private BLangConstantValue calculateBitWiseComplement(BLangConstantValue value) {
        Object result = null;
        if (Types.getReferredType(this.currentConstSymbol.type).tag == TypeTags.INT) {
            result = ~((Long) (value.value));
        }
        return new BLangConstantValue(result, currentConstSymbol.type);
    }

    private BLangConstantValue calculateBooleanComplement(BLangConstantValue value) {
        Object result = null;
        if (Types.getReferredType(this.currentConstSymbol.type).tag == TypeTags.BOOLEAN) {
            result = !((Boolean) (value.value));
        }
        return new BLangConstantValue(result, currentConstSymbol.type);
    }

    BLangConstantValue constructBLangConstantValueWithExactType(BLangExpression expression,
                                                                BConstantSymbol constantSymbol, SymbolEnv env) {
        return constructBLangConstantValueWithExactType(expression, constantSymbol, env, new Stack<>());
    }

    BLangConstantValue constructBLangConstantValueWithExactType(BLangExpression expression,
                                                                BConstantSymbol constantSymbol, SymbolEnv env,
                                                                Stack<String> anonTypeNameSuffixes) {
        BLangConstantValue value = constructBLangConstantValue(expression);
        constantSymbol.value = value;

        if (value == null) {
            return value;
        }

        this.anonTypeNameSuffixes = anonTypeNameSuffixes;
        updateConstantType(constantSymbol, expression, env);
        return value;
    }

    private BLangConstantValue constructBLangConstantValue(BLangExpression node) {
        if (!node.typeChecked) {
            return null;
        }
        switch (node.getKind()) {
            case LITERAL:
            case NUMERIC_LITERAL:
            case RECORD_LITERAL_EXPR:
            case LIST_CONSTRUCTOR_EXPR:
            case ARRAY_LITERAL_EXPR:
            case TUPLE_LITERAL_EXPR:
            case SIMPLE_VARIABLE_REF:
            case BINARY_EXPR:
            case GROUP_EXPR:
            case UNARY_EXPR:
                BLangConstantValue prevResult = this.result;
                Location prevPos = this.currentPos;
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

    private void checkUniqueness(BLangConstant constant) {
        if (constant.symbol.kind == SymbolKind.CONSTANT) {
            String nameString = constant.name.value;
            BLangConstantValue value = constant.symbol.value;

            if (constantMap.containsKey(nameString)) {
                if (value == null) {
                    dlog.error(constant.name.pos, DiagnosticErrorCode.ALREADY_INITIALIZED_SYMBOL, nameString);
                } else {
                    BLangConstantValue lastValue = constantMap.get(nameString);
                    if (!value.equals(lastValue)) {
                        if (lastValue == null) {
                            dlog.error(constant.name.pos, DiagnosticErrorCode.ALREADY_INITIALIZED_SYMBOL, nameString);
                        } else {
                            dlog.error(constant.name.pos, DiagnosticErrorCode.ALREADY_INITIALIZED_SYMBOL_WITH_ANOTHER,
                                    nameString, lastValue);
                        }
                    }
                }
            } else {
                constantMap.put(nameString, value);
            }
        }
    }

    private void updateConstantType(BLangConstant constant) {
        BConstantSymbol symbol = constant.symbol;
        updateConstantType(symbol, constant.expr, symEnv);

        BType resolvedType = symbol.type;

        if (resolvedType.tag == TypeTags.INTERSECTION) {
            BIntersectionType intersectionType = (BIntersectionType) resolvedType;

            if (intersectionType.effectiveType.tag == TypeTags.RECORD) {
                addAssociatedTypeDefinition(constant, intersectionType);
            }
        }
    }

    private void updateConstantType(BConstantSymbol symbol, BLangExpression expr, SymbolEnv env) {
        BType type = Types.getReferredType(symbol.type);
        if (type.getKind() == TypeKind.FINITE) {
            return;
        }

        if (symbol.value == null) {
            return;
        }

        BType resolvedType = checkType(expr, symbol, symbol.value.value, type, symbol.pos, env);
        if (resolvedType == null) {
            return;
        }

        if (resolvedType.getKind() == TypeKind.INTERSECTION && isListOrMapping(type.tag)) {
            expr.setBType(((BIntersectionType) resolvedType).effectiveType);
            symbol.type = resolvedType;
            symbol.literalType = resolvedType;
            symbol.value.type = resolvedType;
            return;
        }

        expr.setBType(resolvedType);
        symbol.type = resolvedType;
    }

    private boolean isListOrMapping(int tag) {
        switch (tag) {
            case TypeTags.RECORD:
            case TypeTags.MAP:
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
                return true;
        }
        return false;
    }

    private BFiniteType createFiniteType(BConstantSymbol constantSymbol, BLangExpression expr) {
        BTypeSymbol finiteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE, constantSymbol.flags, Names.EMPTY,
                                                                constantSymbol.pkgID, null, constantSymbol.owner,
                                                                constantSymbol.pos, VIRTUAL);
        BFiniteType finiteType = new BFiniteType(finiteTypeSymbol);
        finiteType.addValue(expr);
        return finiteType;
    }

    private BType checkType(BLangExpression expr, BConstantSymbol constantSymbol, Object value, BType type,
                            Location pos, SymbolEnv env) {
        if (expr != null && expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF &&
                (((BLangSimpleVarRef) expr).symbol.type.getKind() == TypeKind.FINITE ||
                ((BLangSimpleVarRef) expr).symbol.type.getKind() == TypeKind.INTERSECTION)) {
            return ((BLangSimpleVarRef) expr).symbol.type; // Already type resolved constant
        }

        type = Types.getReferredType(type);

        switch (type.tag) {
            case TypeTags.INT:
            case TypeTags.FLOAT:
            case TypeTags.DECIMAL:
                BLangNumericLiteral numericLiteral = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
                return createFiniteType(constantSymbol, updateLiteral(numericLiteral, value, type, pos));
            case TypeTags.BYTE:
                BLangNumericLiteral byteLiteral = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
                return createFiniteType(constantSymbol, updateLiteral(byteLiteral, value, symTable.intType, pos));
            case TypeTags.STRING:
            case TypeTags.NIL:
            case TypeTags.BOOLEAN:
                BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
                return createFiniteType(constantSymbol, updateLiteral(literal, value, type, pos));
            case TypeTags.MAP:
            case TypeTags.RECORD:
                if (value != null) {
                    return createRecordType(expr, constantSymbol, value, pos, env);
                }
                return null;
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
                if (value != null) {
                    return createTupleType(expr, constantSymbol, pos, value, env);
                }
                return null;
            default:
                return null;
        }
    }

    private BLangLiteral updateLiteral(BLangLiteral literal, Object value, BType type, Location pos) {
        literal.value = value;
        literal.isConstant = true;
        literal.setBType(type);
        literal.pos = pos;
        return literal;
    }

    private BField createField(BVarSymbol symbol, BType symbolType, String fieldName, Location pos) {
        symbol.type = symbolType;
        if (symbolType.getKind() == TypeKind.INTERSECTION) {
            for (BType memberType : ((BIntersectionType) symbolType).getConstituentTypes()) {
                if (memberType.getKind() == TypeKind.RECORD) {
                    symbol.type = memberType;
                }
            }
        }
        BField field = new BField(Names.fromString(fieldName), pos, symbol);
        field.symbol.flags |= Flags.REQUIRED;
        return field;
    }

    private void createTypeDefinition(BRecordType type, Location pos, SymbolEnv env) {
        BRecordTypeSymbol recordSymbol = (BRecordTypeSymbol) type.tsymbol;

        BTypeDefinitionSymbol typeDefinitionSymbol = Symbols.createTypeDefinitionSymbol(type.tsymbol.flags,
                type.tsymbol.name, pkgID, null, env.scope.owner, pos, VIRTUAL);
        typeDefinitionSymbol.scope = new Scope(typeDefinitionSymbol);
        typeDefinitionSymbol.scope.define(names.fromString(typeDefinitionSymbol.name.value), typeDefinitionSymbol);

        type.tsymbol.scope = new Scope(type.tsymbol);
        for (BField field : ((HashMap<String, BField>) type.fields).values()) {
            type.tsymbol.scope.define(field.name, field.symbol);
            field.symbol.owner = recordSymbol;
        }
        typeDefinitionSymbol.type = type;
        recordSymbol.type = type;
        recordSymbol.typeDefinitionSymbol = typeDefinitionSymbol;
        recordSymbol.markdownDocumentation = new MarkdownDocAttachment(0);

        BLangRecordTypeNode recordTypeNode = TypeDefBuilderHelper.createRecordTypeNode(new ArrayList<>(), type,
                pos);
        TypeDefBuilderHelper.populateStructureFields(types, symTable, null, names, recordTypeNode, type, type, pos,
                env, pkgID, null, 0, false);
        recordTypeNode.sealed = true;
        type.restFieldType = new BNoType(TypeTags.NONE);
        BLangTypeDefinition typeDefinition = TypeDefBuilderHelper.createTypeDefinitionForTSymbol(null,
                typeDefinitionSymbol, recordTypeNode, env);
        typeDefinition.symbol.scope = new Scope(typeDefinition.symbol);
        typeDefinition.symbol.type = type;
        typeDefinition.flagSet = new HashSet<>();
        typeDefinition.flagSet.add(Flag.PUBLIC);
        typeDefinition.flagSet.add(Flag.ANONYMOUS);
        createdTypeDefinitions.put(type.tsymbol, typeDefinition);
    }

    private BLangTypeDefinition findTypeDefinition(List<BLangTypeDefinition> typeDefinitionArrayList, String name) {
        for (int i = typeDefinitionArrayList.size() - 1; i >= 0; i--) {
            BLangTypeDefinition typeDefinition = typeDefinitionArrayList.get(i);
            if (typeDefinition.name.value.equals(name)) {
                return typeDefinition;
            }
        }
        return null;
    }

    private void addAssociatedTypeDefinition(BLangConstant constant, BIntersectionType immutableType) {
        BLangTypeDefinition typeDefinition = findTypeDefinition(symEnv.enclPkg.typeDefinitions,
                immutableType.effectiveType.tsymbol.name.value);

        constant.associatedTypeDefinition = typeDefinition;
    }

    private BType createRecordType(BLangExpression expr, BConstantSymbol constantSymbol, Object value, Location pos,
                                   SymbolEnv env) {
        if (expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            return expr.getBType();
        }

        HashMap<String, BLangConstantValue> constValueMap = (HashMap<String, BLangConstantValue>) value;
        for (BLangConstantValue memberValue : constValueMap.values()) {
            if (memberValue == null) {
                return null;
            }
        }

        Name genName = Names.fromString(anonymousModelHelper.getNextAnonymousTypeKey(env.enclPkg.packageID,
                this.anonTypeNameSuffixes));
        BRecordTypeSymbol recordTypeSymbol = new BRecordTypeSymbol(SymTag.RECORD,
                                                                   constantSymbol.flags | Flags.ANONYMOUS, genName,
                                                                   constantSymbol.pkgID,
                                                                   null, constantSymbol.owner, pos, VIRTUAL);
        recordTypeSymbol.scope = constantSymbol.scope;
        BRecordType recordType = new BRecordType(recordTypeSymbol);
        recordType.tsymbol.name = genName;
        recordType.sealed = true;
        recordType.restFieldType = new BNoType(TypeTags.NONE);
        recordTypeSymbol.type = recordType;

        if (constValueMap.size() != 0) {
            if (!populateRecordFields(expr, constantSymbol, pos, constValueMap, recordType, env)) {
                return null;
            }
        }

        createTypeDefinition(recordType, pos, env);
        BIntersectionType intersectionType = ImmutableTypeCloner.getImmutableIntersectionType(pos, types,
                recordType, env, symTable, anonymousModelHelper, names, new HashSet<>());
        return intersectionType;
    }

    private boolean populateRecordFields(BLangExpression expr, BConstantSymbol constantSymbol, Location pos,
                                         HashMap<String, BLangConstantValue> constValueMap, BRecordType recordType,
                                         SymbolEnv env) {
        for (RecordLiteralNode.RecordField field : ((BLangRecordLiteral) expr).fields) {
            String key;
            BVarSymbol newSymbol;

            if (field.isKeyValueField()) {
                BLangRecordLiteral.BLangRecordKeyValueField keyValuePair =
                        (BLangRecordLiteral.BLangRecordKeyValueField) field;
                BLangExpression exprValueField = keyValuePair.valueExpr;

                if (exprValueField.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                    key = keyValuePair.key.toString();
                    newSymbol = new BVarSymbol(constantSymbol.flags, Names.fromString(key), constantSymbol.pkgID,
                                               null, constantSymbol.owner, pos, VIRTUAL);

                    BLangSimpleVarRef simpleVarRefExpr = (BLangSimpleVarRef) exprValueField;
                    if (simpleVarRefExpr.symbol.type.getKind() == TypeKind.FINITE ||
                            simpleVarRefExpr.symbol.type.getKind() == TypeKind.INTERSECTION) {

                        // Already type resolved constant
                        BType resolvedType = simpleVarRefExpr.symbol.type;
                        recordType.fields.put(key, createField(newSymbol, resolvedType, key, pos));
                        keyValuePair.setBType(resolvedType);
                        if (resolvedType.getKind() != TypeKind.FINITE) {
                            constValueMap.get(key).type = resolvedType;
                            if (resolvedType.getKind() == TypeKind.INTERSECTION) {
                                simpleVarRefExpr.setBType(((BIntersectionType) resolvedType).effectiveType);
                            }
                        }
                        continue;
                    }
                }

                key = keyValuePair.key.toString();
                newSymbol = new BVarSymbol(constantSymbol.flags, Names.fromString(key), constantSymbol.pkgID,
                                           null, constantSymbol.owner, pos, VIRTUAL);
                this.anonTypeNameSuffixes.push(key);
                BType newType = checkType(exprValueField, constantSymbol, constValueMap.get(key).value,
                                          constValueMap.get(key).type, pos, env);
                this.anonTypeNameSuffixes.pop();
                if (newType == null) {
                    return false;
                }
                keyValuePair.setBType(newType);
                if (newType.getKind() != TypeKind.FINITE) {
                    constValueMap.get(key).type = newType;
                    if (newType.getKind() == TypeKind.INTERSECTION) {
                        exprValueField.setBType(((BIntersectionType) newType).effectiveType);
                    }
                }

                recordType.fields.put(key, createField(newSymbol, newType, key, pos));
            } else if (field.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                BLangRecordLiteral.BLangRecordVarNameField varNameField =
                        (BLangRecordLiteral.BLangRecordVarNameField) field;
                key = varNameField.variableName.value;
                newSymbol = new BVarSymbol(constantSymbol.flags, Names.fromString(key), constantSymbol.pkgID,
                                           null, constantSymbol.owner, pos, VIRTUAL);
                BType resolvedType = varNameField.symbol.type;
                varNameField.setBType(resolvedType);
                recordType.fields.put(key, createField(newSymbol, resolvedType, key, pos));
                continue;
            } else {
                BLangExpression exprSpreadField = ((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr;
                if (exprSpreadField.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                    BLangSimpleVarRef simpleVarRefExpr = (BLangSimpleVarRef) exprSpreadField;
                    if (simpleVarRefExpr.symbol.type.getKind() == TypeKind.FINITE ||
                            simpleVarRefExpr.symbol.type.getKind() == TypeKind.INTERSECTION) {
                        // Already type resolved constant.
                        BRecordType resolvedType = (BRecordType) ((BIntersectionType)
                                simpleVarRefExpr.symbol.type).effectiveType;
                        exprSpreadField.setBType(resolvedType);

                        for (String spreadFieldKeys : ((HashMap<String, BField>) resolvedType.fields).keySet()) {
                            newSymbol = new BVarSymbol(constantSymbol.flags, Names.fromString(spreadFieldKeys),
                                                       constantSymbol.pkgID, null, constantSymbol.owner, pos, VIRTUAL);
                            BType spreadFieldType = resolvedType.fields.get(spreadFieldKeys).type;
                            recordType.fields.put(spreadFieldKeys, createField(newSymbol, spreadFieldType,
                                    spreadFieldKeys, pos));
                        }
                    }
                    continue;
                }
            }
        }
        return true;
    }

    private BType createTupleType(BLangExpression expr, BConstantSymbol constantSymbol, Location pos,
                                  Object constValue, SymbolEnv env) {
        if (expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            return expr.getBType();
        }

        List<BLangConstantValue> constValueList = (List<BLangConstantValue>) constValue;

        for (BLangConstantValue memberValue : constValueList) {
            if (memberValue == null) {
                return null;
            }
        }

        List<BLangExpression> memberExprs = ((BLangListConstructorExpr) expr).exprs;
        List<BTupleMember> tupleTypes = new ArrayList<>(constValueList.size());

        for (int i = 0; i < memberExprs.size(); i++) {
            BLangExpression memberExpr = memberExprs.get(i);
            BLangConstantValue memberConstValue = constValueList.get(i);

            if (memberExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                BType type = ((BLangSimpleVarRef) memberExpr).symbol.type;
                int tag = type.tag;

                if (tag == TypeTags.FINITE) {
                    // https://github.com/ballerina-platform/ballerina-lang/issues/35127
                    BVarSymbol varSymbol = Symbols.createVarSymbolForTupleMember(type);
                    tupleTypes.add(new BTupleMember(type, varSymbol));
                    continue;
                }

                if (tag == TypeTags.INTERSECTION) {
                    memberConstValue.type = type;
                    BVarSymbol varSymbol = Symbols.createVarSymbolForTupleMember(type);
                    tupleTypes.add(new BTupleMember(type, varSymbol));
                    continue;
                }
            }

            BType newType = checkType(memberExpr, constantSymbol, memberConstValue.value, memberConstValue.type, pos,
                                      env);
            if (newType == null) {
                return null;
            }
            BVarSymbol varSymbol = Symbols.createVarSymbolForTupleMember(newType);
            tupleTypes.add(new BTupleMember(newType, varSymbol));

            if (newType.tag != TypeTags.FINITE) {
                // https://github.com/ballerina-platform/ballerina-lang/issues/35127
                memberConstValue.type = newType;
                memberExpr.setBType(newType.tag == TypeTags.INTERSECTION ?
                                            ((BIntersectionType) newType).effectiveType : newType);
            }
        }

        BTypeSymbol tupleTypeSymbol = Symbols.createTypeSymbol(SymTag.TUPLE_TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                                                               Names.EMPTY, env.enclPkg.symbol.pkgID, null,
                                                               env.scope.owner, pos, VIRTUAL);

        return ImmutableTypeCloner.getImmutableIntersectionType(pos, types, new BTupleType(tupleTypeSymbol, tupleTypes),
                                                                env, symTable, anonymousModelHelper, names,
                                                                new HashSet<>());
    }
}
