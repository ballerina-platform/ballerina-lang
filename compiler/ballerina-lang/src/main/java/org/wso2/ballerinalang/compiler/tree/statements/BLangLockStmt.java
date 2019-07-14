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
package org.wso2.ballerinalang.compiler.tree.statements;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.statements.DLockNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangStructFieldAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implementation for the lock statement, used only in desugar phase.
 *
 * @since 1.0.0
 */
public class BLangLockStmt extends BLangStatement implements DLockNode {

    public Set<BVarSymbol> lockVariables = new HashSet<>();

    public Map<BVarSymbol, Set<BLangStructFieldAccessExpr>> fieldVariables = new HashMap<>();

    public BLangLockStmt() {
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.LOCK; //TODO fix
    }

    @Override
    public String toString() {
        return "lock {}"; //TODO fix
    }

    public boolean addLockVariable(BVarSymbol variable) {
        return lockVariables.add(variable);
    }

    public void addFieldVariable(BLangStructFieldAccessExpr expr) {
        fieldVariables.putIfAbsent((BVarSymbol) ((BLangVariableReference) expr.expr).symbol,
                new HashSet<>());

        Set<BLangStructFieldAccessExpr> exprList = fieldVariables.get(((BLangVariableReference) expr.expr).symbol);

        // remove the existing one to avoid duplicates if same field already exist
        exprList.removeIf(fieldExpr ->
                ((BLangLiteral) fieldExpr.indexExpr).value.equals(((BLangLiteral) expr.indexExpr).value));
        exprList.add(expr);
    }
}
