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
import org.ballerinalang.model.symbols.BLangSymbol;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code TransactionStmt} represents a Transaction/rollback statement.
 *
 * @since 0.87
 */
public class TransactionRollbackStmt extends AbstractStatement {
    private Statement transactionBlock;
    private RollbackBlock rollbackBlock;

    private TransactionRollbackStmt(NodeLocation location, Statement transactionBlock, RollbackBlock rollbackBlock) {
        super(location);
        this.transactionBlock = transactionBlock;
        this.rollbackBlock = rollbackBlock;
    }

    public Statement getTransactionBlock() {
        return transactionBlock;
    }

    public RollbackBlock getRollbackBlock() {
        return rollbackBlock;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void execute(NodeExecutor executor) {
        executor.visit(this);
    }

    /**
     * Represents RollbackBlock of a Transaction-Rollback statement.
     */
    public static class RollbackBlock implements SymbolScope {

        private final SymbolScope enclosingScope;
        private Map<SymbolName, BLangSymbol> symbolMap;
        private BlockStmt rollbackBlock;

        public RollbackBlock(SymbolScope enclosingScope) {
            this.enclosingScope = enclosingScope;
            this.symbolMap = new HashMap<>();
        }

        @Override
        public ScopeName getScopeName() {
            return ScopeName.LOCAL;
        }

        @Override
        public SymbolScope getEnclosingScope() {
            return this.enclosingScope;
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

        public BlockStmt getRollbackBlockStmt() {
            return rollbackBlock;
        }

        void setRollbackBlockStmt(BlockStmt rollbackBlock) {
            this.rollbackBlock = rollbackBlock;
        }
    }

    /**
     * Builds a {@code {@link TransactionRollbackStmt}} statement.
     *
     * @since 0.87
     */
    public static class TransactionRollbackStmtBuilder {
        private Statement transactionBlock;
        private RollbackBlock rollbackBlock;
        private NodeLocation location;

        public void setTransactionBlock(Statement transactionBlock) {
            this.transactionBlock = transactionBlock;
        }

        public void setRollbackBlockStmt(Statement statement) {
            this.rollbackBlock.setRollbackBlockStmt((BlockStmt) statement);
        }

        public void setRollbackBlock(RollbackBlock rollbackBlock) {
            this.rollbackBlock = rollbackBlock;
        }

        public NodeLocation getLocation() {
            return location;
        }

        public void setLocation(NodeLocation location) {
            this.location = location;
        }

        public TransactionRollbackStmt build() {
            return new TransactionRollbackStmt(
                    location,
                    transactionBlock,
                    rollbackBlock);
        }
    }
}
