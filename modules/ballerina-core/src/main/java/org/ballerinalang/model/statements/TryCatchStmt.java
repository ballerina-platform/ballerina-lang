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
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.symbols.BLangSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code TryCatchStmt} represents a try/catch statement.
 *
 * @since 0.8.0
 */
public class TryCatchStmt extends AbstractStatement {
    private Statement tryBlock;
    private CatchBlock[] catchBlocks;
    private FinallyBlock finallyBlock;

    private TryCatchStmt(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor,
                         Statement tryBlock, CatchBlock[] catchBlocks, FinallyBlock     finallyBlock) {
        super(location);
        this.whiteSpaceDescriptor = whiteSpaceDescriptor;
        this.tryBlock = tryBlock;
        this.catchBlocks = catchBlocks;
        this.finallyBlock = finallyBlock;
    }

    public Statement getTryBlock() {
        return tryBlock;
    }

    public CatchBlock[] getCatchBlocks() {
        return catchBlocks;
    }

    public FinallyBlock getFinallyBlock() {
        return finallyBlock;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Represents CatchBlock of a Try-Catch statement.
     */
    public static class CatchBlock implements SymbolScope {

        private final SymbolScope enclosingScope;
        private ParameterDef parameterDef;
        private Map<SymbolName, BLangSymbol> symbolMap;
        private BlockStmt catchBlock;
        private WhiteSpaceDescriptor whiteSpaceDescriptor;

        public CatchBlock(SymbolScope enclosingScope) {
            this.enclosingScope = enclosingScope;
            this.symbolMap = new HashMap<>();
        }

        public ParameterDef getParameterDef() {
            return parameterDef;
        }

        public void setParameterDef(ParameterDef parameterDef) {
            this.parameterDef = parameterDef;
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

        public BlockStmt getCatchBlockStmt() {
            return catchBlock;
        }

        void setCatchBlockStmt(BlockStmt catchBlock) {
            this.catchBlock = catchBlock;
        }

        public WhiteSpaceDescriptor getWhiteSpaceDescriptor() {
            return whiteSpaceDescriptor;
        }

        public void setWhiteSpaceDescriptor(WhiteSpaceDescriptor whiteSpaceDescriptor) {
            this.whiteSpaceDescriptor = whiteSpaceDescriptor;
        }
    }

    /**
     * Represents CatchBlock of a Try-Catch statement.
     */
    public static class FinallyBlock implements SymbolScope {

        private final SymbolScope enclosingScope;
        private Map<SymbolName, BLangSymbol> symbolMap;
        private BlockStmt finallyBlock;

        public FinallyBlock(SymbolScope enclosingScope) {
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

        public BlockStmt getFinallyBlockStmt() {
            return finallyBlock;
        }

        void setFinallyBlockStmt(BlockStmt catchBlock) {
            this.finallyBlock = catchBlock;
        }

    }

    /**
     * Builds a {@code {@link TryCatchStmt}} statement.
     *
     * @since 0.8.0
     */
    public static class TryCatchStmtBuilder {
        private Statement tryBlock;
        private List<CatchBlock> catchBlock = new ArrayList<>();
        private FinallyBlock finallyBlock;
        private NodeLocation location;
        private WhiteSpaceDescriptor whiteSpaceDescriptor;

        public Statement getTryBlock() {
            return tryBlock;
        }

        public void setTryBlock(Statement tryBlock) {
            this.tryBlock = tryBlock;
        }

        public CatchBlock getLastCatchBlock() {
            return catchBlock.get(catchBlock.size() - 1);
        }

        public void setLastCatchBlockStmt(Statement statement) {
            this.catchBlock.get(catchBlock.size() - 1).setCatchBlockStmt((BlockStmt) statement);
        }

        public void addCatchBlock(CatchBlock catchBlock) {
            this.catchBlock.add(catchBlock);
        }

        public FinallyBlock getFinallyBlock() {
            return finallyBlock;
        }

        public void setFinallyBlockStmt(Statement statement) {
            this.finallyBlock.setFinallyBlockStmt((BlockStmt) statement);
        }

        public void setFinallyBlock(FinallyBlock finallyBlock) {
            this.finallyBlock = finallyBlock;
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

        public TryCatchStmt build() {
            return new TryCatchStmt(
                    location,
                    whiteSpaceDescriptor,
                    tryBlock,
                    catchBlock.toArray(new CatchBlock[0]),
                    finallyBlock);
        }
    }
}
