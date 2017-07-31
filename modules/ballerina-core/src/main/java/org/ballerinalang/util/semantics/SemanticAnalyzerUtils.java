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

import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BJSONConstrainedType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeEdge;
import org.ballerinalang.model.types.TypeLattice;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.util.codegen.InstructionCodes;

/**
 * This class contains a set of utility methods used by the semantic analyzer.
 *
 * @since 0.92
 */
public class SemanticAnalyzerUtils {

    private AssignabilityResult performAssignabilityCheck(BType lhsType, Expression rhsExpr) {
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

        // SemanticAnalyzer 3902-3910

        // SemanticAnalyzer 3913-3931

        return assignabilityResult;
    }

    private boolean isImplicitCastPossible(BType lhsType, BType rhsType) {
        if (lhsType.equals(BTypes.typeAny)) {
            return true;
        }

        // 2) Check whether both types are array types
        if (lhsType.getTag() == TypeTags.ARRAY_TAG || rhsType.getTag() == TypeTags.ARRAY_TAG) {
            return isImplicitArrayCastPossible(lhsType, rhsType);
        }

        return false;
    }

    private boolean isImplicitArrayCastPossible(BType lhsType, BType rhsType) {
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

    private TypeCastExpression checkWideningPossible(BType lhsType, Expression rhsExpr) {
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
}
