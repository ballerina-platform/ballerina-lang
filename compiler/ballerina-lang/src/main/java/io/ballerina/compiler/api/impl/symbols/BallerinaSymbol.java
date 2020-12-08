/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.api.impl.symbols;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.impl.BallerinaModuleID;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;

import java.util.Optional;

/**
 * Represents the implementation of a Compiled Ballerina Symbol.
 *
 * @since 2.0.0
 */
public class BallerinaSymbol implements Symbol {

    private final String name;
    private final PackageID moduleID;
    private final SymbolKind symbolKind;
    private final Documentation docAttachment;
    private final Location position;
    private final BSymbol internalSymbol;

    protected BallerinaSymbol(String name, PackageID moduleID, SymbolKind symbolKind, BSymbol symbol) {
        this.name = name;
        this.moduleID = moduleID;
        this.symbolKind = symbolKind;
        this.docAttachment = getDocAttachment(symbol);

        if (symbol == null) {
            throw new IllegalArgumentException("'symbol' cannot be null");
        }

        this.internalSymbol = symbol;
        this.position = new BLangDiagnosticLocation(symbol.pos.lineRange().filePath(),
                                                    symbol.pos.lineRange().startLine().line(),
                                                    symbol.pos.lineRange().endLine().line(),
                                                    symbol.pos.lineRange().startLine().offset(),
                                                    symbol.pos.lineRange().endLine().offset());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleID moduleID() {
        return new BallerinaModuleID(this.moduleID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SymbolKind kind() {
        return this.symbolKind;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Documentation> docAttachment() {
        return Optional.ofNullable(this.docAttachment);
    }

    @Override
    public Location location() {
        return this.position;
    }

    public BSymbol getInternalSymbol() {
        return this.internalSymbol;
    }

    private Documentation getDocAttachment(BSymbol symbol) {
        return symbol == null ? null : new BallerinaDocumentation(symbol.markdownDocumentation);
    }

    /**
     * Represents Ballerina Symbol Builder.
     *
     * @param <T> Symbol Type
     */
    protected abstract static class SymbolBuilder<T extends SymbolBuilder<T>> {

        protected String name;
        protected PackageID moduleID;
        protected SymbolKind ballerinaSymbolKind;
        protected BSymbol bSymbol;

        /**
         * Symbol Builder Constructor.
         *
         * @param name       Symbol Name
         * @param moduleID   module ID of the symbol
         * @param symbolKind symbol kind
         * @param bSymbol    symbol to evaluate
         */
        public SymbolBuilder(String name, PackageID moduleID, SymbolKind symbolKind, BSymbol bSymbol) {
            this.name = name;
            this.moduleID = moduleID;
            this.ballerinaSymbolKind = symbolKind;
            this.bSymbol = bSymbol;
        }

        /**
         * Build the Ballerina Symbol.
         *
         * @return {@link BallerinaSymbol} built
         */
        public abstract BallerinaSymbol build();
    }
}
