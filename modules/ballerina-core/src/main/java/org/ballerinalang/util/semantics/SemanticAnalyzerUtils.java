/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util.semantics;

import org.ballerinalang.model.CallableUnit;
import org.ballerinalang.model.CallableUnitSymbolName;
import org.ballerinalang.model.NativeUnit;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.expressions.AbstractExpression;
import org.ballerinalang.model.expressions.BasicLiteral;
import org.ballerinalang.model.expressions.CallableUnitInvocationExpr;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BJSONConstrainedType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.BuiltinTypeName;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.model.types.TypeConstants;
import org.ballerinalang.model.types.TypeEdge;
import org.ballerinalang.model.types.TypeLattice;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.natives.NativeUnitProxy;
import org.ballerinalang.util.codegen.InstructionCodes;

/**
 * This class contains a set of utility methods used by the semantic analyzer.
 *
 * @since 0.92
 */
public class SemanticAnalyzerUtils {

    public static AssignabilityResult performAssignabilityCheck(BType lhsType, Expression rhsExpr) {
        AssignabilityResult assignabilityResult = new AssignabilityResult();
        BType rhsType = rhsExpr.getType();
        if (lhsType.equals(rhsType)) {
            assignabilityResult.assignable = true;
            return assignabilityResult;
        }

        if (rhsType.equals(BTypes.typeNull) && !BTypes.isValueType(lhsType)) {
            assignabilityResult.assignable = true;
            return assignabilityResult;
        }

        if ((rhsType instanceof BJSONConstrainedType) && (lhsType.equals(BTypes.typeJSON))) {
            assignabilityResult.assignable = true;
            return assignabilityResult;
        }

        // Now check whether an implicit cast is available;
        TypeCastExpression implicitCastExpr = checkWideningPossible(lhsType, rhsExpr);
        if (implicitCastExpr != null) {
            assignabilityResult.assignable = true;
            assignabilityResult.expression = implicitCastExpr;
            return assignabilityResult;
        }

        // Now check whether left-hand side type is 'any', then an implicit cast is possible;
        if (isImplicitCastPossible(lhsType, rhsType)) {
            implicitCastExpr = new TypeCastExpression(rhsExpr.getNodeLocation(),
                    null, rhsExpr, lhsType);
            implicitCastExpr.setOpcode(InstructionCodes.NOP);

            assignabilityResult.assignable = true;
            assignabilityResult.expression = implicitCastExpr;
            return assignabilityResult;
        }

        if (lhsType.equals(BTypes.typeFloat) && rhsType.equals(BTypes.typeInt) && rhsExpr instanceof BasicLiteral) {
            BasicLiteral newExpr = new BasicLiteral(rhsExpr.getNodeLocation(), rhsExpr.getWhiteSpaceDescriptor(),
                    new BuiltinTypeName(TypeConstants.FLOAT_TNAME), new BFloat(((BasicLiteral) rhsExpr)
                    .getBValue().intValue()));
//            visitSingleValueExpr(newExpr); //TODO uncomment once method is moved here
            assignabilityResult.assignable = true;
            assignabilityResult.expression = newExpr;
            return assignabilityResult;
        }

        // SemanticAnalyzer 3913-3931

        return assignabilityResult;
    }

    public static boolean isImplicitCastPossible(BType lhsType, BType rhsType) {
        if (lhsType.equals(BTypes.typeAny)) {
            return true;
        }

        // 2) Check whether both types are array types
        if (lhsType.getTag() == TypeTags.ARRAY_TAG || rhsType.getTag() == TypeTags.ARRAY_TAG) {
            return isImplicitArrayCastPossible(lhsType, rhsType);
        }

        return false;
    }

    public static boolean isImplicitArrayCastPossible(BType lhsType, BType rhsType) {
        if (lhsType.getTag() == TypeTags.ARRAY_TAG && rhsType.getTag() == TypeTags.ARRAY_TAG) {
            // Both types are array types
            BArrayType lhrArrayType = (BArrayType) lhsType;
            BArrayType rhsArrayType = (BArrayType) rhsType;
            return isImplicitArrayCastPossible(lhrArrayType.getElementType(), rhsArrayType.getElementType());

        } else if (rhsType.getTag() == TypeTags.ARRAY_TAG) {
            // Only the right-hand side is an array type
            // Then lhs type should 'any' type
            return lhsType.equals(BTypes.typeAny);

        } else if (lhsType.getTag() == TypeTags.ARRAY_TAG) {
            // Only the left-hand side is an array type
            return false;
        }

        // Now both types are not array types
        if (lhsType.equals(rhsType)) {
            return true;
        }

        // In this case, lhs type should be of type 'any' and the rhs type cannot be a value type
        return lhsType.getTag() == BTypes.typeAny.getTag() && !BTypes.isValueType(rhsType);
    }

    public static TypeCastExpression checkWideningPossible(BType lhsType, Expression rhsExpr) {
        TypeCastExpression typeCastExpr = null;
        BType rhsType = rhsExpr.getType();

        TypeEdge typeEdge = TypeLattice.getImplicitCastLattice().getEdgeFromTypes(rhsType, lhsType, null);
        if (typeEdge != null) {
            typeCastExpr = new TypeCastExpression(rhsExpr.getNodeLocation(),
                    rhsExpr.getWhiteSpaceDescriptor(), rhsExpr, lhsType);
            typeCastExpr.setOpcode(typeEdge.getOpcode());
        }
        return typeCastExpr;
    }

    /**
     * Helper method to match the callable unit with invocation (check whether parameters map, do cast if applicable).
     *
     * @param callableIExpr  invocation expression
     * @param symbolName     callable symbol name
     * @param callableSymbol matching symbol
     * @param currentScope   current symbol scope
     * @return callableSymbol  matching symbol
     */
    public static BLangSymbol matchAndUpdateArguments(AbstractExpression callableIExpr,
                                                      CallableUnitSymbolName symbolName,
                                                      BLangSymbol callableSymbol,
                                                      SymbolScope currentScope) {
        if (callableSymbol == null) {
            return null;
        }

        Expression[] argExprs = ((CallableUnitInvocationExpr) callableIExpr).getArgExprs();
        Expression[] updatedArgExprs = new Expression[argExprs.length];

        CallableUnitSymbolName funcSymName = (CallableUnitSymbolName) callableSymbol.getSymbolName();
        if (!funcSymName.isNameAndParamCountMatch(symbolName)) {
            return null;
        }

        boolean implicitCastPossible = true;

        if (callableSymbol instanceof NativeUnitProxy) {
            NativeUnit nativeUnit = ((NativeUnitProxy) callableSymbol).load();
            for (int i = 0; i < argExprs.length; i++) {
                Expression argExpr = argExprs[i];
                updatedArgExprs[i] = argExpr;
                SimpleTypeName simpleTypeName = nativeUnit.getArgumentTypeNames()[i];
                BType lhsType = BTypes.resolveType(simpleTypeName, currentScope, callableIExpr.getNodeLocation());

                AssignabilityResult result = performAssignabilityCheck(lhsType, argExpr);
                if (result.expression != null) {
                    updatedArgExprs[i] = result.expression;
                } else if (!result.assignable) {
                    // TODO do we need to throw an error here?
                    implicitCastPossible = false;
                    break;
                }
            }
        } else {
            for (int i = 0; i < argExprs.length; i++) {
                Expression argExpr = argExprs[i];
                updatedArgExprs[i] = argExpr;
                BType lhsType = ((CallableUnit) callableSymbol).getParameterDefs()[i].getType();

                AssignabilityResult result = performAssignabilityCheck(lhsType, argExpr);
                if (result.expression != null) {
                    updatedArgExprs[i] = result.expression;
                } else if (!result.assignable) {
                    // TODO do we need to throw an error here?
                    implicitCastPossible = false;
                    break;
                }
            }
        }

        if (!implicitCastPossible) {
            return null;
        }

        for (int i = 0; i < updatedArgExprs.length; i++) {
            ((CallableUnitInvocationExpr) callableIExpr).getArgExprs()[i] = updatedArgExprs[i];
        }
        return callableSymbol;
    }
}
