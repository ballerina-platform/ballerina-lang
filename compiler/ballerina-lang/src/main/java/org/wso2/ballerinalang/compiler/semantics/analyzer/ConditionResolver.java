/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.model.tree.OperatorKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;

import java.util.HashSet;

/**
 * This analyzes the condition in if statement and while statement.
 *
 * @since 2.0.0
 */
public class ConditionResolver {

    static BType checkConstCondition(Types types, SymbolTable symTable, BLangExpression condition) {
        switch (condition.getKind()) {
            case GROUP_EXPR:
                return checkConstCondition(types, symTable, ((BLangGroupExpr) condition).expression);
            case LITERAL:
                Object value = ((BLangLiteral) condition).value;
                if (value instanceof Boolean) {
                    return value == Boolean.TRUE ? symTable.trueType : symTable.falseType;
                }
                return new BFiniteType(null, new HashSet<>() { { add(condition); } });
            case NUMERIC_LITERAL:
                return new BFiniteType(null, new HashSet<>() { { add(condition); } });
            case TYPE_TEST_EXPR:
                BLangTypeTestExpr typeTestExpr = (BLangTypeTestExpr) condition;
                BType exprType = typeTestExpr.expr.getBType();
                boolean isAssignable = types.isAssignable(exprType, typeTestExpr.typeNode.getBType());
                if (typeTestExpr.isNegation) {
                    return isAssignable ? symTable.falseType : exprType == symTable.semanticError ?
                            symTable.trueType : symTable.semanticError;
                }
                return isAssignable ? symTable.trueType : exprType == symTable.semanticError ?
                        symTable.falseType : symTable.semanticError;
            case BINARY_EXPR:
                BLangBinaryExpr binaryExpr = (BLangBinaryExpr) condition;
                boolean operatorIsAND = binaryExpr.opKind == OperatorKind.AND;
                boolean operatorIsOR = binaryExpr.opKind == OperatorKind.OR;
                boolean operatorIsEqual = binaryExpr.opKind == OperatorKind.EQUAL;
                boolean operatorIsNotEqual = binaryExpr.opKind == OperatorKind.NOT_EQUAL;
                if (!(operatorIsAND || operatorIsOR || operatorIsEqual || operatorIsNotEqual)) {
                    return symTable.semanticError;
                }
                BType lhsConst = checkConstCondition(types, symTable, binaryExpr.lhsExpr);
                if (operatorIsOR && lhsConst == symTable.trueType) {
                    return lhsConst;
                }
                BType rhsConst = checkConstCondition(types, symTable, binaryExpr.rhsExpr);
                if (operatorIsOR) {
                    if (rhsConst == symTable.trueType) {
                        return rhsConst;
                    }
                    if (lhsConst == rhsConst && lhsConst == symTable.falseType) {
                        return lhsConst;
                    }
                    return symTable.semanticError;
                }
                if (operatorIsAND) {
                    if (lhsConst == symTable.falseType || rhsConst == symTable.falseType) {
                        return symTable.falseType;
                    }
                    if (lhsConst == symTable.semanticError || rhsConst == symTable.semanticError) {
                        return symTable.semanticError;
                    }
                    return lhsConst == rhsConst && lhsConst == symTable.trueType ?
                            symTable.trueType : symTable.falseType;
                }
                if (!(types.isSingletonType(lhsConst) && types.isSingletonType(rhsConst))) {
                    return symTable.semanticError;
                }
                if (operatorIsEqual) {
                    return types.isSameSingletonType((BFiniteType) lhsConst, (BFiniteType) rhsConst) ?
                            symTable.trueType : symTable.falseType;
                }
                return !types.isSameSingletonType((BFiniteType) lhsConst, (BFiniteType) rhsConst) ?
                        symTable.trueType : symTable.falseType;
            case UNARY_EXPR:
                BType conditionValue = checkConstCondition(types, symTable, ((BLangUnaryExpr) condition).expr);
                if (conditionValue == symTable.trueType) {
                    return symTable.falseType;
                }
                if (conditionValue == symTable.falseType) {
                    return symTable.trueType;
                }
                return symTable.semanticError;
            case SIMPLE_VARIABLE_REF:
                BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) condition;
                BType type = (simpleVarRef.symbol.tag & SymTag.CONSTANT) == SymTag.CONSTANT ?
                        simpleVarRef.symbol.type : condition.getBType();
                if (!types.isSingletonType(type)) {
                    return symTable.semanticError;
                }
                return checkConstCondition(types, symTable, ((BFiniteType) Types.getReferredType(type))
                        .getValueSpace().iterator().next());
            default:
                return symTable.semanticError;
        }
    }
}
