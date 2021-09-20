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

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.util.BooleanCondition;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.Set;

/**
 * This analyzes the condition in if statement and while statement.
 *
 * @since 2.0.0
 */
public class ConditionResolver extends BLangNodeVisitor {
    private final Types types;
    private final SymbolTable symTable;

    public ConditionResolver(Types types, SymbolTable symTable) {
        this.types = types;
        this.symTable = symTable;
    }

    BooleanCondition checkConstCondition(BLangExpression condition) {
        switch (condition.getKind()) {
            case GROUP_EXPR:
                return checkConstCondition(((BLangGroupExpr) condition).expression);
            case LITERAL:
                return ((BLangLiteral) condition).value == Boolean.TRUE ? BooleanCondition.TRUE :
                        BooleanCondition.FALSE;
            case TYPE_TEST_EXPR:
                BLangTypeTestExpr typeTestExpr = (BLangTypeTestExpr) condition;
                boolean isAssignable = types.isAssignable(typeTestExpr.expr.getBType(),
                        typeTestExpr.typeNode.getBType());
                if (typeTestExpr.isNegation) {
                    return isAssignable ? BooleanCondition.FALSE :
                            typeTestExpr.expr.getBType() == symTable.semanticError ? BooleanCondition.TRUE :
                                    BooleanCondition.NOT_CONST_BOOLEAN;
                }
                return isAssignable ? BooleanCondition.TRUE :
                        typeTestExpr.expr.getBType() == symTable.semanticError ? BooleanCondition.FALSE :
                                BooleanCondition.NOT_CONST_BOOLEAN;
            case BINARY_EXPR:
                BLangBinaryExpr binaryExpr = (BLangBinaryExpr) condition;
                if (binaryExpr.opKind != OperatorKind.AND && !(binaryExpr.lhsExpr.getKind() == NodeKind.LITERAL ||
                        binaryExpr.lhsExpr.getKind() == NodeKind.TYPE_TEST_EXPR) &&
                        !(binaryExpr.rhsExpr.getKind() == NodeKind.LITERAL ||
                                binaryExpr.rhsExpr.getKind() == NodeKind.TYPE_TEST_EXPR)) {
                    return BooleanCondition.NOT_CONST_BOOLEAN;
                }
                BooleanCondition lhsConst = checkConstCondition(binaryExpr.lhsExpr);
                BooleanCondition rhsConst = checkConstCondition(binaryExpr.rhsExpr);
                if (lhsConst == BooleanCondition.FALSE || rhsConst == BooleanCondition.FALSE) {
                    return BooleanCondition.FALSE;
                }
                if (lhsConst == BooleanCondition.NOT_CONST_BOOLEAN ||
                        rhsConst == BooleanCondition.NOT_CONST_BOOLEAN) {
                    return BooleanCondition.NOT_CONST_BOOLEAN;
                }
                return lhsConst == rhsConst && lhsConst == BooleanCondition.TRUE ?
                        BooleanCondition.TRUE : BooleanCondition.FALSE;
            case UNARY_EXPR:
                BLangUnaryExpr unaryExpr = (BLangUnaryExpr) condition;
                if (unaryExpr.operator != OperatorKind.NOT) {
                    return BooleanCondition.NOT_CONST_BOOLEAN;
                }
                BooleanCondition conditionValue = checkConstCondition(unaryExpr.expr);
                if (conditionValue == BooleanCondition.TRUE) {
                    return BooleanCondition.FALSE;
                }
                if (conditionValue == BooleanCondition.FALSE) {
                    return BooleanCondition.TRUE;
                }
                return BooleanCondition.NOT_CONST_BOOLEAN;
            case SIMPLE_VARIABLE_REF:
                BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) condition;
                BType type = (simpleVarRef.symbol.tag & SymTag.CONSTANT) == SymTag.CONSTANT ?
                        simpleVarRef.symbol.type : condition.getBType();
                if (type.tag != TypeTags.FINITE) {
                    return BooleanCondition.NOT_CONST_BOOLEAN;
                }
                Set<BLangExpression> valueSpace = ((BFiniteType) type).getValueSpace();
                if (valueSpace.size() != 1) {
                    return BooleanCondition.NOT_CONST_BOOLEAN;
                }
                return checkConstCondition(valueSpace.iterator().next());
            default:
                return BooleanCondition.NOT_CONST_BOOLEAN;
        }
    }
}
