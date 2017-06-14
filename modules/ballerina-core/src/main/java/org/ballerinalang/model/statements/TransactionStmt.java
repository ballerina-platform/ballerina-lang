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


import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.symbols.BLangSymbol;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@code TransactionStmt} represents a Transaction statement.
 *
 * @since 0.87
 */
public class TransactionStmt extends AbstractStatement {
    private Statement transactionBlock;
    private AbortedBlock abortedBlock;
    private CommittedBlock committedBlock;

    private TransactionStmt(NodeLocation location, Statement transactionBlock, AbortedBlock abortedBlock,
                            CommittedBlock committedBlock) {
        super(location);
        this.transactionBlock = transactionBlock;
        this.abortedBlock = abortedBlock;
        this.committedBlock = committedBlock;
    }

    public Statement getTransactionBlock() {
        return transactionBlock;
    }

    public AbortedBlock getAbortedBlock() {
        return abortedBlock;
    }

    public CommittedBlock getCommittedBlock() {
        return committedBlock;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Represents Aborted block of a Transaction statement.
     */
    public static class AbortedBlock implements SymbolScope {

        private final SymbolScope enclosingScope;
        private Map<SymbolName, BLangSymbol> symbolMap;
        private BlockStmt abortedBlock;

        public AbortedBlock(SymbolScope enclosingScope) {
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

        public BlockStmt getAbortedBlockStmt() {
            return abortedBlock;
        }

        void setAbortedBlockStmt(BlockStmt abortedBlock) {
            this.abortedBlock = abortedBlock;
        }
    }

    /**
     * Represents Committed block of a Transaction statement.
     */
    public static class CommittedBlock implements SymbolScope {

        private final SymbolScope enclosingScope;
        private Map<SymbolName, BLangSymbol> symbolMap;
        private BlockStmt committedBlock;

        public CommittedBlock(SymbolScope enclosingScope) {
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

        public BlockStmt getCommittedBlockStmt() {
            return committedBlock;
        }

        void setCommittedBlockStmt(BlockStmt abortedBlock) {
            this.committedBlock = abortedBlock;
        }
    }

    /**
     * Builds a {@code {@link TransactionStmt}} statement.
     *
     * @since 0.87
     */
    public static class TransactionStmtBuilder {
        private Statement transactionBlock;
        private AbortedBlock abortedBlock;
        private CommittedBlock committedBlock;
        private NodeLocation location;
        private WhiteSpaceDescriptor whiteSpaceDescriptor;

        public void setTransactionBlock(Statement transactionBlock) {
            this.transactionBlock = transactionBlock;
        }

        public void setAbortedBlockStmt(Statement statement) {
            this.abortedBlock.setAbortedBlockStmt((BlockStmt) statement);
        }

        public void setAbortedBlock(AbortedBlock abortedBlock) {
            this.abortedBlock = abortedBlock;
        }

        public void setCommittedBlockStmt(Statement statement) {
            this.committedBlock.setCommittedBlockStmt((BlockStmt) statement);
        }

        public void setCommittedBlock(CommittedBlock committedBlock) {
            this.committedBlock = committedBlock;
        }

        public NodeLocation getLocation() {
            return location;
        }

        public void setLocation(NodeLocation location) {
            this.location = location;
        }

        public WhiteSpaceDescriptor getWhiteSpaceDescriptor() {
            return whiteSpaceDescriptor;
        }

        public void setWhiteSpaceDescriptor(WhiteSpaceDescriptor whiteSpaceDescriptor) {
            this.whiteSpaceDescriptor = whiteSpaceDescriptor;
        }

        public TransactionStmt build() {
            TransactionStmt transactionStmt = new TransactionStmt(location, transactionBlock,
                    abortedBlock, committedBlock);
            transactionStmt.setWhiteSpaceDescriptor(whiteSpaceDescriptor);
            return transactionStmt;
        }
    }
}
