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
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType.NarrowedTypes;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 
 * Responsible for evaluate an expression and narrow the types of the variables
 * that affected by type narrowing.
 * 
 * @since 0.991.0
 */
public class TypeNarrower extends BLangNodeVisitor {

    private SymbolEnv env;
    private SymbolTable symTable;
    private Types types;

    private static final CompilerContext.Key<TypeNarrower> TYPE_NARROWER_KEY = new CompilerContext.Key<>();

    private TypeNarrower(CompilerContext context) {
        context.put(TYPE_NARROWER_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.types = Types.getInstance(context);
    }

    public static TypeNarrower getInstance(CompilerContext context) {
        TypeNarrower typeNarrower = context.get(TYPE_NARROWER_KEY);
        if (typeNarrower == null) {
            typeNarrower = new TypeNarrower(context);
        }
        return typeNarrower;
    }

    /**
     * Evaluate an expression to truth value. Update the types of the variable symbols in the expression
     * to their respective types implied by the truth of the expression.
     * 
     * @param expr Expression to evaluate
     * @param env Current environment
     */
    public void evaluateTruth(BLangExpression expr, SymbolEnv env) {
        getNarrowedTypes(expr, env).forEach((symbol, typeInfo) -> symbol.type = typeInfo.trueType);
    }

    /**
     * Evaluate an expression to false value. Update the types of the variable symbols in the expression
     * to their respective types implied by the falsity of the expression.
     * 
     * @param expr Expression to evaluate
     * @param env Current environment
     */
    public void evaluateFalsity(BLangExpression expr, SymbolEnv env) {
        getNarrowedTypes(expr, env).forEach((symbol, typeInfo) -> symbol.type = typeInfo.falseType);
    }

    /**
     * Reset the types variable symbols in an expression to the type which was the type
     * before evaluating the expression.
     * 
     * @param expr Expression to reset
     * @param env Current environment
     */
    public void reset(BLangExpression expr, SymbolEnv env) {
        if (expr == null || expr.narrowedTypeInfo == null) {
            return;
        }

        expr.narrowedTypeInfo.forEach((symbol, typeInfo) -> symbol.type = typeInfo.prevType);
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        if (unaryExpr.operator != OperatorKind.NOT) {
            return;
        }

        unaryExpr.narrowedTypeInfo = getNarrowedTypes(unaryExpr.expr, env).entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> new NarrowedTypes(entry.getValue().falseType, entry.getValue().trueType,
                                entry.getValue().prevType)));
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        Map<BVarSymbol, NarrowedTypes> t1 = getNarrowedTypes(binaryExpr.lhsExpr, env);
        Map<BVarSymbol, NarrowedTypes> t2 = getNarrowedTypes(binaryExpr.rhsExpr, env);

        Set<BVarSymbol> updatedSymbols = new LinkedHashSet<>(t1.keySet());
        updatedSymbols.addAll(t2.keySet());

        if (binaryExpr.opKind == OperatorKind.AND || binaryExpr.opKind == OperatorKind.OR) {
            binaryExpr.narrowedTypeInfo.putAll(updatedSymbols.stream().collect(Collectors.toMap(symbol -> symbol,
                    symbol -> getNarrowedTypesForBinaryOp(t1, t2, symbol, binaryExpr.opKind))));
        }
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        bracedOrTupleExpr.expressions.forEach(expr -> analyzeExpr(expr, env));
        if (bracedOrTupleExpr.isBracedExpr) {
            bracedOrTupleExpr.narrowedTypeInfo.putAll(bracedOrTupleExpr.expressions.get(0).narrowedTypeInfo);
        }
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        analyzeExpr(typeTestExpr.expr, env);
        if (typeTestExpr.expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return;
        }

        BVarSymbol varSymbol = (BVarSymbol) ((BLangSimpleVarRef) typeTestExpr.expr).symbol;
        if (varSymbol == null) {
            // Terminate for undefined symbols
            return;
        }

        BType trueType = getTypeIntersection(varSymbol.type, typeTestExpr.typeNode.type);
        BType falseType = types.getRemainingType(varSymbol.type, typeTestExpr.typeNode.type);
        typeTestExpr.narrowedTypeInfo.put(varSymbol, new NarrowedTypes(trueType, falseType, varSymbol.type));
    }

    // Private methods

    private Map<BVarSymbol, NarrowedTypes> getNarrowedTypes(BLangExpression expr, SymbolEnv env) {
        analyzeExpr(expr, env);
        return expr.narrowedTypeInfo;
    }

    private void analyzeExpr(BLangExpression expr, SymbolEnv env) {
        switch (expr.getKind()) {
            case BINARY_EXPR:
            case TYPE_TEST_EXPR:
            case BRACED_TUPLE_EXPR:
            case UNARY_EXPR:
                break;
            default:
                expr.narrowedTypeInfo = new HashMap<>();
                return;
        }

        SymbolEnv prevEnv = this.env;
        this.env = env;
        if (expr != null && expr.narrowedTypeInfo == null) {
            expr.narrowedTypeInfo = new HashMap<>();
            expr.accept(this);
        }
        this.env = prevEnv;
    }

    private NarrowedTypes getNarrowedTypesForBinaryOp(Map<BVarSymbol, NarrowedTypes> lhsTypes,
                                                      Map<BVarSymbol, NarrowedTypes> rhsTypes, BVarSymbol symbol,
                                                      OperatorKind operator) {
        BType lhsTrueType, lhsFalseType, rhsTrueType, rhsFalseType;
        if (lhsTypes.containsKey(symbol)) {
            NarrowedTypes narrowedTypes = lhsTypes.get(symbol);
            lhsTrueType = narrowedTypes.trueType;
            lhsFalseType = narrowedTypes.falseType;
        } else {
            lhsTrueType = lhsFalseType = symbol.type;
        }

        if (rhsTypes.containsKey(symbol)) {
            NarrowedTypes narrowedTypes = rhsTypes.get(symbol);
            rhsTrueType = narrowedTypes.trueType;
            rhsFalseType = narrowedTypes.falseType;
        } else {
            rhsTrueType = rhsFalseType = symbol.type;
        }

        BType trueType, falseType;
        if (operator == OperatorKind.AND) {
            trueType = getTypeIntersection(lhsTrueType, rhsTrueType);
            BType tmpType = getTypeIntersection(lhsTrueType, rhsFalseType);
            falseType = getTypeUnion(lhsFalseType, tmpType);
        } else {
            BType tmpType = getTypeIntersection(lhsFalseType, rhsTrueType);
            trueType = getTypeUnion(lhsTrueType, tmpType);
            falseType = getTypeIntersection(lhsFalseType, rhsFalseType);
        }
        return new NarrowedTypes(trueType, falseType, symbol.type);
    }

    private BType getTypeIntersection(BType currentType, BType targetType) {
        List<BType> narrowingTypes = types.getAllTypes(targetType);
        List<BType> intersection = narrowingTypes.stream().map(type -> {
            if (types.isAssignable(type, currentType)) {
                return type;
            } else if (types.isAssignable(currentType, type)) {
                return currentType;
            }
            return null;
        }).filter(type -> type != null).collect(Collectors.toList());

        if (intersection.isEmpty() || intersection.contains(symTable.semanticError)) {
            return symTable.semanticError;
        } else if (intersection.size() == 1) {
            return intersection.get(0);
        } else {
            LinkedHashSet<BType> memberTypes = new LinkedHashSet<>(intersection);
            return new BUnionType(null, memberTypes, memberTypes.contains(symTable.nilType));
        }
    }

    private BType getTypeUnion(BType currentType, BType targetType) {
        LinkedHashSet<BType> union = new LinkedHashSet<>(types.getAllTypes(currentType));
        types.getAllTypes(targetType).stream()
                .filter(newType -> union.stream().anyMatch(existingType -> !types.isAssignable(newType, existingType)))
                .forEach(newType -> union.add(newType));

        if (union.contains(symTable.semanticError)) {
            return symTable.semanticError;
        } else if (union.size() == 1) {
            return union.toArray(new BType[1])[0];
        }
        return new BUnionType(null, union, union.contains(symTable.nilType));
    }
}
