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
package org.ballerinalang.model.statements;

import org.ballerinalang.model.NodeExecutor;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.nodes.GotoNode;
import org.ballerinalang.model.symbols.BLangSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A BlockStmt represents a list of statements between balanced braces.
 *
 * @since 0.8.0
 */
public class BlockStmt extends AbstractStatement implements SymbolScope {
    private Statement[] statements;
    private GotoNode gotoNode;

    // Scope related variables
    private SymbolScope enclosingScope;
    private Map<SymbolName, BLangSymbol> symbolMap;

    private BlockStmt(NodeLocation location, SymbolScope enclosingScope) {
        super(location);
        this.enclosingScope = enclosingScope;
        this.symbolMap = new HashMap<>();
    }

    public Statement[] getStatements() {
        return this.statements;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void execute(NodeExecutor executor) {
        executor.visit(this);
    }

    @Override
    public ScopeName getScopeName() {
        return ScopeName.LOCAL;
    }

    @Override
    public SymbolScope getEnclosingScope() {
        return enclosingScope;
    }

    @Override
    public void define(SymbolName name, BLangSymbol symbol) {
        symbolMap.put(name, symbol);
    }

    @Override
    public BLangSymbol resolve(SymbolName name) {
        return resolve(symbolMap, name);
    }

    @Override
    public Map<SymbolName, BLangSymbol> getSymbolMap() {
        return Collections.unmodifiableMap(this.symbolMap);
    }

    public GotoNode getGotoNode() {
        return gotoNode;
    }

    public void setGotoNode(GotoNode gotoNode) {
        this.gotoNode = gotoNode;
    }

    /**
     * Builds a {@code BlockStmt}.
     *
     * @since 0.8.0
     */
    public static class BlockStmtBuilder {
        private BlockStmt blockStmt;
        private List<Statement> statementList = new ArrayList<>();

        public BlockStmtBuilder(NodeLocation location, SymbolScope enclosingScope) {
            blockStmt = new BlockStmt(location, enclosingScope);
        }

        public SymbolScope getCurrentScope() {
            return blockStmt;
        }

        public void addStmt(Statement statement) {
            statementList.add(statement);
        }

        public BlockStmt build() {
            this.blockStmt.statements = statementList.toArray(new Statement[statementList.size()]);
            return blockStmt;
        }
    }
}

