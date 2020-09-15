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
package org.wso2.ballerinalang.compiler.tree.statements;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.statements.BlockStatementNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * @since 0.94
 */
public class BLangBlockStmt extends BLangStatement implements BlockStatementNode {

    public List<BLangStatement> stmts;

    public BVarSymbol mapSymbol;

    public boolean isBreakable;
    /**
     * We need to keep a reference to the block statements scope here.
     * This is the only place where we have a link from the node to a scope
     */
    public Scope scope;

    public BLangBlockStmt() {
        this.stmts = new ArrayList<>();
    }

    @Override
    public List<BLangStatement> getStatements() {
        return stmts;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void addStatement(StatementNode statement) {
        this.stmts.add((BLangStatement) statement);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.BLOCK;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("; ");
        this.stmts.forEach(stmt -> sj.add(stmt.toString()));
        return sj.toString();
    }
}
