/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.model.statements;

import org.wso2.ballerina.core.model.NodeExecutor;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.SymbolScope;
import org.wso2.ballerina.core.model.symbols.BLangSymbol;

import java.util.ArrayList;
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

    // Scope related variables
    private SymbolScope enclosingScope;
    private Map<SymbolName, BLangSymbol> symbolMap;

    private BlockStmt(NodeLocation location,
              Statement[] statements,
              SymbolScope enclosingScope,
              Map<SymbolName, BLangSymbol> symbolMap) {
        super(location);
        this.statements = statements;
        this.enclosingScope = enclosingScope;
        this.symbolMap = symbolMap;
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

    /**
     * Builds a {@code BlockStmt}.
     *
     * @since 0.8.0
     */
    public static class BlockStmtBuilder implements SymbolScope {
        private NodeLocation location;
        private List<Statement> statementList = new ArrayList<>();

        // Scope related variables
        private SymbolScope enclosingScope;
        private Map<SymbolName, BLangSymbol> symbolMap = new HashMap<>();

        public BlockStmtBuilder(SymbolScope enclosingScope) {
            this.enclosingScope = enclosingScope;
        }

        public void setNodeLocation(NodeLocation location) {
            this.location = location;
        }

        public void addStmt(Statement statement) {
            statementList.add(statement);
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

        public BlockStmt build() {
            return new BlockStmt(
                    location,
                    statementList.toArray(new Statement[statementList.size()]),
                    enclosingScope,
                    symbolMap);
        }
    }
}

