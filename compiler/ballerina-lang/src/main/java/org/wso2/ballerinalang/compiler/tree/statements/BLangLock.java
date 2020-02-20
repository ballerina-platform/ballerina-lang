/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.model.tree.statements.BlockStatementNode;
import org.ballerinalang.model.tree.statements.LockNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangStructFieldAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Implementation for the lock tree node.
 *
 * @since 0.961.0
 */
public class BLangLock extends BLangStatement implements LockNode {

    public BLangBlockStmt body;

    public Set<BVarSymbol> lockVariables = new HashSet<>();

    public Map<BVarSymbol, Set<BLangStructFieldAccessExpr>> fieldVariables = new HashMap<>();

    public String uuid;

    public BLangLock() {
    }

    @Override
    public BLangBlockStmt getBody() {
        return body;
    }

    @Override
    public void setBody(BlockStatementNode body) {
        this.body = (BLangBlockStmt) body;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.LOCK;
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

    @Override
    public String toString() {
        return "lock {"
                + (body != null ? String.valueOf(body) : "") + "}";
    }

    /**
     * Implementation for the lock statement, used only in desugar phase.
     *
     * @since 1.0.0
     */
    public static class BLangLockStmt extends BLangLock {

        public Set<BVarSymbol> lockVariables = new HashSet<>();

        public Map<BVarSymbol, Set<String>> fieldVariables = new HashMap<>();

        public BLangLockStmt(DiagnosticPos pos) {
            this.pos = pos;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return "lock [" + lockVariables.stream().map(s -> s.name.value).collect(Collectors.joining(", "))
                    + "] [" + fieldVariables.entrySet().stream().map(e -> e.getKey().name.value + "["
                    + String.join(", ", e.getValue()) + "]").collect(Collectors.joining(", ")) + "]";

        }

        public boolean addLockVariable(BVarSymbol variable) {
            return lockVariables.add(variable);
        }

        public void addFieldVariable(BVarSymbol varSymbol, String field) {
            fieldVariables.putIfAbsent(varSymbol, new TreeSet<>());
            Set<String> exprList = fieldVariables.get(varSymbol);
            exprList.add(field);
        }
    }

    /**
     * Implementation for the unlock statement, used only in desugar phase.
     *
     * @since 1.0.0
     */
    public static class BLangUnLockStmt extends BLangLock {

        public BLangUnLockStmt(DiagnosticPos pos) {
            this.pos = pos;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return "unlock []";
        }
    }
}
