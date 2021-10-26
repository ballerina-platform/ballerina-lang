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

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
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
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.model.symbols.SymbolOrigin.SOURCE;
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
    private TypeChecker typeChecker;
    private static final CompilerContext.Key<TypeNarrower> TYPE_NARROWER_KEY = new CompilerContext.Key<>();

    private TypeNarrower(CompilerContext context) {
        context.put(TYPE_NARROWER_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.typeChecker = TypeChecker.getInstance(context);
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

    /**
     * Evaluate an expression to truth value. Remove the given type from the type of expression and new expression type
     * is assigned to the target node.
     *
     * @param expr         Expression to evaluate
     * @param typeToRemove Type which is going to remove from expr
     * @param targetNode   Node to which the type narrowing applies
     * @param env          Current environment
     * @return target environment
     */
    public SymbolEnv evaluateTruth(BLangExpression expr, BType typeToRemove, BLangNode targetNode, SymbolEnv env) {
        if (expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF || typeToRemove == null) {
            return env;
        }

        BLangSimpleVarRef varRef = (BLangSimpleVarRef) expr;
        Name varName = new Name(varRef.variableName.value);
        BType originalType;
        if (env.scope.entries.containsKey(varName)) {
            originalType = env.scope.entries.get(varName).symbol.type;
        } else {
            originalType = varRef.getBType();
        }
        BType remainingType = types.getRemainingMatchExprType(originalType, typeToRemove);
        if (remainingType == symTable.semanticError) {
            return env;
        }
        SymbolEnv targetEnv = getTargetEnv(targetNode, env);
        BVarSymbol originalVarSym = getOriginalVarSymbol((BVarSymbol) varRef.symbol);
        symbolEnter.defineTypeNarrowedSymbol(varRef.pos, targetEnv, originalVarSym, remainingType,
                originalVarSym.origin == VIRTUAL);
        return targetEnv;
    }

    /**
     * Evaluate the expression in an `if` statement to its false value, following the `if` statement
     * without an `else` clause if its statement block cannot complete normally. Returns an environment
     * containing the symbols with their narrowed types, defined by the falsity of the expression.
     * If there are no symbols that get affected by type narrowing, then this will return the same environment.
     *
     * @param expr Expression to evaluate
     * @param currentEnv Current environment
     * @return target environment
     */
    public SymbolEnv evaluateFalsityFollowingIfWithoutElse(BLangExpression expr, SymbolEnv currentEnv,
                                                           boolean isConstTrueCondition) {
        if (!checkValidExpressionToEvaluateFalsity(expr)) {
            return currentEnv;
        }

        Map<BVarSymbol, NarrowedTypes> narrowedTypes = getNarrowedTypes(expr, currentEnv);
        if (narrowedTypes.isEmpty()) {
            return currentEnv;
        }

        SymbolEnv narrowedEnv = SymbolEnv.createTypeNarrowedEnv(expr, currentEnv);

        for (Map.Entry<BVarSymbol, NarrowedTypes> narrowedType : narrowedTypes.entrySet()) {
            BVarSymbol originalSym = getOriginalVarSymbol(narrowedType.getKey());
            BType falseType = narrowedType.getValue().falseType;
            symbolEnter.defineTypeNarrowedSymbol(expr.pos, narrowedEnv, originalSym,
                    isConstTrueCondition ? symTable.neverType : falseType, originalSym.origin == VIRTUAL);
        }

        return narrowedEnv;
    }

    private boolean checkValidExpressionToEvaluateFalsity(BLangExpression expr) {
        switch (expr.getKind()) {
            case TYPE_TEST_EXPR:
            case LITERAL:
            case NUMERIC_LITERAL:
                return true;
            case GROUP_EXPR:
                return checkValidExpressionToEvaluateFalsity(((BLangGroupExpr) expr).expression);
            case BINARY_EXPR:
                BLangBinaryExpr binaryExpr = (BLangBinaryExpr) expr;
                return checkValidExpressionToEvaluateFalsity(binaryExpr.lhsExpr) &&
                        checkValidExpressionToEvaluateFalsity(binaryExpr.rhsExpr);
            case UNARY_EXPR:
                return checkValidExpressionToEvaluateFalsity(((BLangUnaryExpr) expr).expr);
            case SIMPLE_VARIABLE_REF:
                return expr.getBType().tag == TypeTags.FINITE;
            default:
                return false;
        }
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
    public SymbolEnv evaluateFalsity(BLangExpression expr, BLangNode targetNode, SymbolEnv env, boolean isBinaryExpr) {
        Map<BVarSymbol, NarrowedTypes> narrowedTypes = getNarrowedTypes(expr, env);
        if (narrowedTypes.isEmpty()) {
            return env;
        }

        SymbolEnv targetEnv = getTargetEnv(targetNode, env);
        for (Map.Entry<BVarSymbol, NarrowedTypes> narrowedType : narrowedTypes.entrySet()) {
            BType falseType = narrowedType.getValue().falseType;
            BType trueType = narrowedType.getValue().trueType;

            BVarSymbol originalSym = getOriginalVarSymbol(narrowedType.getKey());

            if (isBinaryExpr) {
                falseType = falseType == symTable.semanticError ?
                        types.getRemainingType(originalSym.type, trueType) : falseType;
            } else {
                falseType =  targetNode.getKind() != NodeKind.IF &&
                        falseType == trueType ? symTable.neverType : falseType;
            }

            symbolEnter.defineTypeNarrowedSymbol(expr.pos, targetEnv, originalSym,
                    falseType == symTable.semanticError ? symTable.neverType : falseType,
                    originalSym.origin == VIRTUAL);
        }

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
        BLangExpression lhsExpr = binaryExpr.lhsExpr;
        BLangExpression rhsExpr = binaryExpr.rhsExpr;
        OperatorKind opKind = binaryExpr.opKind;

        if (opKind == OperatorKind.EQUAL || opKind == OperatorKind.NOT_EQUAL) {
            // eg a == 5, a == (), a == ONE, a == b : One side should be a variable and other side should be an expr
            narrowTypeForEqualOrNotEqual(binaryExpr, lhsExpr, rhsExpr);
            // eg 5 == a, () == a, ONE == a, b == a
            narrowTypeForEqualOrNotEqual(binaryExpr, rhsExpr, lhsExpr);
            return;
        }

        Map<BVarSymbol, NarrowedTypes> t1 = getNarrowedTypes(lhsExpr, env);
        Map<BVarSymbol, NarrowedTypes> t2 = getNarrowedTypes(rhsExpr, env);

        Set<BVarSymbol> updatedSymbols = new LinkedHashSet<>(t1.keySet());
        updatedSymbols.addAll(t2.keySet());

        if (opKind == OperatorKind.AND || opKind == OperatorKind.OR) {
            for (BVarSymbol symbol : updatedSymbols) {
                binaryExpr.narrowedTypeInfo.put(getOriginalVarSymbol(symbol),
                        getNarrowedTypesForBinaryOp(t1, t2, getOriginalVarSymbol(symbol), binaryExpr.opKind));
            }
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
        BLangExpression lhsExpression = typeTestExpr.expr;
        if (lhsExpression.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return;
        }

        BSymbol symbol = ((BLangSimpleVarRef) lhsExpression).symbol;
        if (symbol == symTable.notFoundSymbol) {
            // Terminate for undefined symbols
            return;
        }

        typeChecker.markAndRegisterClosureVariable(symbol, lhsExpression.pos, env);
        if (symbol.closure || (symbol.owner.tag & SymTag.PACKAGE) == SymTag.PACKAGE) {
            return;
        }

        BVarSymbol varSymbol = (BVarSymbol) symbol;

        setNarrowedTypeInfo(typeTestExpr, varSymbol, typeTestExpr.typeNode.getBType());
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
        var nonLoggingContext = Types.IntersectionContext.typeTestIntersectionCalculationContext();
        if (operator == OperatorKind.AND) {
            trueType = types.getTypeIntersection(nonLoggingContext, lhsTrueType, rhsTrueType, this.env);
            BType tmpType = types.getTypeIntersection(nonLoggingContext, lhsTrueType, rhsFalseType, this.env);
            falseType = getTypeUnion(lhsFalseType, tmpType);
        } else {
            BType tmpType = types.getTypeIntersection(nonLoggingContext, lhsFalseType, rhsTrueType, this.env);
            trueType = getTypeUnion(lhsTrueType, tmpType);
            falseType = types.getTypeIntersection(nonLoggingContext, lhsFalseType, rhsFalseType, this.env);
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

    private BFiniteType createFiniteType(BLangExpression expr) {
        BTypeSymbol finiteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE,
                Flags.asMask(EnumSet.noneOf(Flag.class)), Names.EMPTY, env.enclPkg.symbol.pkgID, null,
                env.scope.owner, expr.pos, SOURCE);

        BFiniteType finiteType = new BFiniteType(finiteTypeSymbol);
        expr.setBType(symTable.getTypeFromTag(expr.getBType().tag));
        finiteType.addValue(expr);
        finiteTypeSymbol.type = finiteType;

        return finiteType;
    }

    private void narrowTypeForEqualOrNotEqual(BLangBinaryExpr binaryExpr, BLangExpression lhsExpr,
                                              BLangExpression rhsExpr) {
        if (lhsExpr.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return;
        }

        BSymbol lhsVarSymbol = ((BLangSimpleVarRef) lhsExpr).symbol;
        if (((lhsVarSymbol.tag & SymTag.VARIABLE) != SymTag.VARIABLE)) {
            return;
        }

        typeChecker.markAndRegisterClosureVariable(lhsVarSymbol, lhsExpr.pos, env);
        if (lhsVarSymbol.closure || (lhsVarSymbol.owner.tag & SymTag.PACKAGE) == SymTag.PACKAGE) {
            return;
        }

        NodeKind rhsExperKind = rhsExpr.getKind();
        if (rhsExperKind == NodeKind.LITERAL || rhsExperKind == NodeKind.NUMERIC_LITERAL) {
            setNarrowedTypeInfo(binaryExpr, (BVarSymbol) lhsVarSymbol, createFiniteType(rhsExpr));
        } else if (rhsExperKind == NodeKind.SIMPLE_VARIABLE_REF) {
            BSymbol rhsVarSymbol = ((BLangSimpleVarRef) rhsExpr).symbol;
            if (rhsVarSymbol != symTable.notFoundSymbol && rhsVarSymbol.kind == SymbolKind.CONSTANT) {
                setNarrowedTypeInfo(binaryExpr, (BVarSymbol) lhsVarSymbol, rhsVarSymbol.type);
            }
        }
    }

    private void setNarrowedTypeInfo(BLangExpression expr, BVarSymbol varSymbol, BType narrowWithType) {
        var nonLoggingContext = Types.IntersectionContext.typeTestIntersectionCalculationContext();
        BType trueType;
        BType falseType;
        if (expr.getKind() == NodeKind.BINARY_EXPR && ((BLangBinaryExpr) expr).opKind == OperatorKind.NOT_EQUAL) {
            trueType = types.getRemainingType(varSymbol.type, narrowWithType);
            falseType = types.getTypeIntersection(nonLoggingContext, varSymbol.type, narrowWithType, this.env);
        } else if (expr.getKind() == NodeKind.TYPE_TEST_EXPR && ((BLangTypeTestExpr) expr).isNegation) {
            trueType = types.getRemainingType(varSymbol.type, narrowWithType);
            falseType = types.getTypeIntersection(nonLoggingContext, varSymbol.type, narrowWithType, this.env);
        } else {
            trueType = types.getTypeIntersection(nonLoggingContext, varSymbol.type, narrowWithType, this.env);
            falseType = types.getRemainingType(varSymbol.type, narrowWithType);
        }

        expr.narrowedTypeInfo.put(getOriginalVarSymbol(varSymbol), new NarrowedTypes(trueType, falseType));
    }
}
