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
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;

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
    private SymbolEnter symbolEnter;
    private static final CompilerContext.Key<TypeNarrower> TYPE_NARROWER_KEY = new CompilerContext.Key<>();

    private TypeNarrower(CompilerContext context) {
        context.put(TYPE_NARROWER_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.types = Types.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
    }

    public static TypeNarrower getInstance(CompilerContext context) {
        TypeNarrower typeNarrower = context.get(TYPE_NARROWER_KEY);
        if (typeNarrower == null) {
            typeNarrower = new TypeNarrower(context);
        }
        return typeNarrower;
    }

    /**
     * Evaluate an expression to truth value. Returns an environment containing the symbols with their narrowed types,
     * defined by the truth of the expression. If there are no symbols that get affected by type narrowing, then this
     * will return the same environment.
     *
     * @param expr         Expression to evaluate
     * @param targetNode   Node to which the type narrowing applies
     * @param env          Current environment
     * @param isBinaryExpr Indicates whether the current context is a binary expression
     * @return target environment
     */
    public SymbolEnv evaluateTruth(BLangExpression expr, BLangNode targetNode, SymbolEnv env, boolean isBinaryExpr) {
        Map<BVarSymbol, NarrowedTypes> narrowedTypes = getNarrowedTypes(expr, env);
        if (narrowedTypes.isEmpty()) {
            return env;
        }

        SymbolEnv targetEnv = getTargetEnv(targetNode, env);
        Set<Map.Entry<BVarSymbol, NarrowedTypes>> entrySet = narrowedTypes.entrySet();

        for (Map.Entry<BVarSymbol, NarrowedTypes> entry : entrySet) {
            BVarSymbol symbol = entry.getKey();
            NarrowedTypes typeInfo = entry.getValue();
            BType narrowedType = isBinaryExpr && typeInfo.trueType == symTable.semanticError ? typeInfo.falseType :
                    typeInfo.trueType;
            BVarSymbol originalSym = getOriginalVarSymbol(symbol);
            symbolEnter.defineTypeNarrowedSymbol(expr.pos, targetEnv, originalSym, narrowedType,
                                                 originalSym.origin == VIRTUAL);
        }

        return targetEnv;
    }

    public SymbolEnv evaluateTruth(BLangExpression expr, BLangNode targetNode, SymbolEnv env) {
        return evaluateTruth(expr, targetNode, env, false);
    }

    /**
     * Evaluate an expression to false value. Returns an environment containing the symbols
     * with their narrowed types, defined by the falsity of the expression. If there are no 
     * symbols that get affected by type narrowing, then this will return the same environment. 
     * 
     * @param expr Expression to evaluate
     * @param targetNode node to which the type narrowing applies
     * @param env Current environment
     * @return target environment
     */
    public SymbolEnv evaluateFalsity(BLangExpression expr, BLangNode targetNode, SymbolEnv env) {
        Map<BVarSymbol, NarrowedTypes> narrowedTypes = getNarrowedTypes(expr, env);
        if (narrowedTypes.isEmpty()) {
            return env;
        }

        SymbolEnv targetEnv = getTargetEnv(targetNode, env);
        narrowedTypes.forEach((symbol, typeInfo) -> {
            BVarSymbol originalSym = getOriginalVarSymbol(symbol);
            symbolEnter.defineTypeNarrowedSymbol(expr.pos, targetEnv, originalSym,
                                                 typeInfo.falseType, originalSym.origin == VIRTUAL);
        });

        return targetEnv;
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        if (unaryExpr.operator != OperatorKind.NOT) {
            return;
        }

        Map<BVarSymbol, NarrowedTypes> narrowedTypes = getNarrowedTypes(unaryExpr.expr, env);
        Map<BVarSymbol, BType.NarrowedTypes> newMap = new HashMap<>(narrowedTypes.size());
        for (Map.Entry<BVarSymbol, NarrowedTypes> entry : narrowedTypes.entrySet()) {
            newMap.put(entry.getKey(), new NarrowedTypes(entry.getValue().falseType, entry.getValue().trueType));
        }

        unaryExpr.narrowedTypeInfo = newMap;
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        Map<BVarSymbol, NarrowedTypes> t1 = getNarrowedTypes(binaryExpr.lhsExpr, env);
        Map<BVarSymbol, NarrowedTypes> t2 = getNarrowedTypes(binaryExpr.rhsExpr, env);

        Set<BVarSymbol> updatedSymbols = new LinkedHashSet<>(t1.keySet());
        updatedSymbols.addAll(t2.keySet());

        if (binaryExpr.opKind == OperatorKind.AND || binaryExpr.opKind == OperatorKind.OR) {
            binaryExpr.narrowedTypeInfo.putAll(updatedSymbols.stream()
                    .collect(Collectors.toMap(
                            symbol -> getOriginalVarSymbol(symbol),
                            symbol -> getNarrowedTypesForBinaryOp(t1, t2, getOriginalVarSymbol(symbol),
                                    binaryExpr.opKind))));
        }
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        analyzeExpr(groupExpr.expression, env);
        groupExpr.narrowedTypeInfo.putAll(groupExpr.expression.narrowedTypeInfo);
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

        BType trueType = types.getTypeIntersection(varSymbol.type, typeTestExpr.typeNode.type);
        BType falseType = types.getRemainingType(varSymbol.type, typeTestExpr.typeNode.type);
        typeTestExpr.narrowedTypeInfo.put(getOriginalVarSymbol(varSymbol), new NarrowedTypes(trueType, falseType));
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
            case GROUP_EXPR:
            case UNARY_EXPR:
                break;
            default:
                if (expr.narrowedTypeInfo == null) {
                    expr.narrowedTypeInfo = new HashMap<>();
                }
                return;
        }

        SymbolEnv prevEnv = this.env;
        this.env = env;
        if (expr.narrowedTypeInfo == null) {
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
            // Swapping the types if the RHS true type is a semantic error to disregard the RHS expr when continuing
            // with the evaluation of the expression.
            // e.g., ((x is int && x is boolean) && x is float) where x is of type int|string|float
            // `x is boolean` will result in a semantic error since boolean is not in the above union. The false type
            // of this type test contains the result from the last, semantically valid type test result. Since the
            // true type is a semantic error, we swap the true and false types to use the last known correct narrowed
            // type for future expr evaluations.
            if (rhsTrueType.tag == TypeTags.SEMANTIC_ERROR && operator == OperatorKind.AND) {
                rhsTrueType = rhsFalseType;
                rhsFalseType = types.getRemainingType(symbol.type, rhsTrueType);
            }
        } else {
            rhsTrueType = rhsFalseType = symbol.type;
        }

        BType trueType, falseType;
        if (operator == OperatorKind.AND) {
            trueType = types.getTypeIntersection(lhsTrueType, rhsTrueType);
            BType tmpType = types.getTypeIntersection(lhsTrueType, rhsFalseType);
            falseType = getTypeUnion(lhsFalseType, tmpType);
        } else {
            BType tmpType = types.getTypeIntersection(lhsFalseType, rhsTrueType);
            trueType = getTypeUnion(lhsTrueType, tmpType);
            falseType = types.getTypeIntersection(lhsFalseType, rhsFalseType);
        }
        return new NarrowedTypes(trueType, falseType);
    }

    private BType getTypeUnion(BType currentType, BType targetType) {
        LinkedHashSet<BType> union = new LinkedHashSet<>(types.getAllTypes(currentType));
        List<BType> targetComponentTypes = types.getAllTypes(targetType);
        for (BType newType : targetComponentTypes) {
            if (newType.tag != TypeTags.NULL_SET) {
                for (BType existingType : union) {
                    if (!types.isAssignable(newType, existingType)) {
                        union.add(newType);
                        break;
                    }
                }
            }
        }

        if (union.contains(symTable.semanticError)) {
            return symTable.semanticError;
        } else if (union.size() == 1) {
            return union.toArray(new BType[1])[0];
        }
        return BUnionType.create(null, union);
    }

    BVarSymbol getOriginalVarSymbol(BVarSymbol varSymbol) {
        if (varSymbol.originalSymbol == null) {
            return varSymbol;
        }

        return getOriginalVarSymbol(varSymbol.originalSymbol);
    }

    private SymbolEnv getTargetEnv(BLangNode targetNode, SymbolEnv env) {
        SymbolEnv targetEnv = SymbolEnv.createTypeNarrowedEnv(targetNode, env);
        if (targetNode.getKind() == NodeKind.BLOCK) {
            ((BLangBlockStmt) targetNode).scope = targetEnv.scope;
        }
        if (targetNode.getKind() == NodeKind.BLOCK_FUNCTION_BODY) {
            ((BLangBlockFunctionBody) targetNode).scope = targetEnv.scope;
        }

        return targetEnv;
    }
}
