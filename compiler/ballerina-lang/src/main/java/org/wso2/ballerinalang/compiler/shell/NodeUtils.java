/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.shell;

import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;

import java.util.List;

/**
 * Utility class with helper functions
 * to build the nodes.
 */
public class NodeUtils {
    public static BLangInvocation createInvocation(BInvokableSymbol invokableSymbol, List<BLangExpression> args) {
        BLangInvocation invocationNode = new BLangInvocation();
        BLangIdentifier name = new BLangIdentifier();

        name.isLiteral = false;
        name.value = invokableSymbol.name.value;
        invocationNode.pkgAlias = new BLangIdentifier();
        invocationNode.name = name;
        invocationNode.symbol = invokableSymbol;
        invocationNode.type = invokableSymbol.retType;
        invocationNode.requiredArgs = args;
        return invocationNode;
    }

    public static BLangLiteral createStringLiteral(SymbolTable symTable, String value) {
        BLangLiteral stringVarParam = new BLangLiteral();
        stringVarParam.value = value;
        stringVarParam.type = symTable.stringType;
        return stringVarParam;
    }

    public static BLangExpression createTypeCastExpr(Types types, BLangExpression expr, BType targetType) {
        // Include a check if necessary
        if (types.containsErrorType(expr.type) && !types.containsErrorType(targetType)) {
            BLangCheckPanickedExpr checkedExpr = new BLangCheckPanickedExpr();
            checkedExpr.expr = expr;
            checkedExpr.pos = expr.pos;
            expr = checkedExpr;
        }

        BLangTypeConversionExpr conversionExpr = new BLangTypeConversionExpr();
        conversionExpr.pos = expr.pos;
        conversionExpr.expr = expr;
        conversionExpr.type = targetType;
        conversionExpr.targetType = targetType;
        conversionExpr.internal = true;
        return conversionExpr;
    }

    public static BLangExpressionStmt createStatement(BLangExpression expr) {
        BLangExpressionStmt expressionStmt = new BLangExpressionStmt();
        expressionStmt.expr = expr;
        expressionStmt.pos = expr.pos;
        return expressionStmt;
    }
}
