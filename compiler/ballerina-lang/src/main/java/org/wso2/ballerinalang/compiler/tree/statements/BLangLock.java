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

import org.ballerinalang.model.clauses.OnFailClauseNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.statements.BlockStatementNode;
import org.ballerinalang.model.tree.statements.LockNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnFailClause;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation for the lock tree node.
 *
 * @since 0.961.0
 */
public class BLangLock extends BLangStatement implements LockNode {

    public BLangBlockStmt body;

    public String uuid;

    public BLangOnFailClause onFailClause;

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
    public OnFailClauseNode getOnFailClause() {
        return this.onFailClause;
    }

    @Override
    public void setOnFailClause(OnFailClauseNode onFailClause) {
        this.onFailClause = (BLangOnFailClause) onFailClause;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.LOCK;
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

        public BLangLockStmt(DiagnosticPos pos) {
            this.pos = pos;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return "lock [" + lockVariables.stream().map(s -> s.name.value).collect(Collectors.joining(", "));
        }

        public boolean addLockVariable(BVarSymbol variable) {
            return lockVariables.add(variable);
        }

        public boolean addLockVariable(Set<BVarSymbol> variables) {
            return lockVariables.addAll(variables);
        }
    }

    /**
     * Implementation for the unlock statement, used only in desugar phase.
     *
     * @since 1.0.0
     */
    public static class BLangUnLockStmt extends BLangLock {

        public BLangLockStmt relatedLock;

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
