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

import io.ballerina.compiler.api.impl.BallerinaKeywordsProvider;
import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.tools.diagnostics.Location;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents the implementation of a Compiled Ballerina Symbol.
 *
 * @since 2.0.0
 */
public class BallerinaSymbol implements Symbol {

    protected final CompilerContext context;
    private final String name;
    private final SymbolKind symbolKind;
    private final Location position;
    private final BSymbol internalSymbol;
    private ModuleSymbol module;
    private boolean moduleEvaluated;

    protected BallerinaSymbol(String name, SymbolKind symbolKind, BSymbol symbol, CompilerContext context) {
        this.name = name;
        this.symbolKind = symbolKind;
        this.context = context;

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

    @Override
    public Optional<String> getName() {
        return Optional.ofNullable(this.name);
    }

    @Override
    public Optional<ModuleSymbol> getModule() {
        if (this.module != null || this.moduleEvaluated) {
            return Optional.ofNullable(this.module);
        }

        this.moduleEvaluated = true;
        BSymbol symbol = this.internalSymbol.owner;
        while (symbol != null) {
            if (symbol instanceof BPackageSymbol) {
                break;
            }
            symbol = symbol.owner;
        }

        if (symbol == null) {
            return Optional.empty();
        }

        SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);
        this.module = symbolFactory.createModuleSymbol((BPackageSymbol) symbol, symbol.name.value);
        return Optional.of(this.module);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SymbolKind kind() {
        return this.symbolKind;
    }

    @Override
    public Location location() {
        return this.position;
    }

    @Override
    public Optional<Location> getLocation() {
        return Optional.ofNullable(this.position);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Symbol)) {
            return false;
        }

        Symbol symbol = (Symbol) obj;
        return isSameName(this.getName(), symbol.getName())
                && isSameModule(this.getModule(), symbol.getModule())
                && isSameLocation(this.getLocation(), symbol.getLocation())
                && this.kind().equals(symbol.kind());

    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getName().orElse(null), this.getModule().orElse(null), this.kind(),
                            this.getLocation().isPresent() ? this.getLocation().get().lineRange() : null);
    }

    public BSymbol getInternalSymbol() {
        return this.internalSymbol;
    }

    Documentation getDocAttachment(BSymbol symbol) {
        return symbol == null ? null : new BallerinaDocumentation(symbol.markdownDocumentation);
    }

    private boolean isSameName(Optional<String> name1, Optional<String> name2) {
        if (name1.isEmpty() || name2.isEmpty()) {
            return false;
        }

        return name1.get().equals(name2.get());
    }

    private boolean isSameModule(Optional<ModuleSymbol> mod1, Optional<ModuleSymbol> mod2) {
        if (mod1.isEmpty() || mod2.isEmpty()) {
            return false;
        }

        return mod1.get().id().equals(mod2.get().id());
    }

    private boolean isSameLocation(Optional<Location> loc1, Optional<Location> loc2) {
        if (loc1.isEmpty() || loc2.isEmpty()) {
            return false;
        }

        return loc1.get().lineRange().equals(loc2.get().lineRange());
    }

    public boolean isReservedKeyword(String value) {
        return BallerinaKeywordsProvider.BALLERINA_KEYWORDS.contains(value);
    }

    public String escapeReservedKeyword(String value) {
        if (BallerinaKeywordsProvider.BALLERINA_KEYWORDS.contains(value)) {
            return "'" + value;
        }

        return value;
    }

    /**
     * Represents Ballerina Symbol Builder.
     *
     * @param <T> Symbol Type
     */
    protected abstract static class SymbolBuilder<T extends SymbolBuilder<T>> {

        protected String name;
        protected SymbolKind ballerinaSymbolKind;
        protected BSymbol bSymbol;
        protected CompilerContext context;

        /**
         * Symbol Builder Constructor.
         *
         * @param name       Symbol Name
         * @param symbolKind symbol kind
         * @param bSymbol    symbol to evaluate
         * @param context    context of the compilation
         */
        public SymbolBuilder(String name, SymbolKind symbolKind, BSymbol bSymbol, CompilerContext context) {
            this.name = name;
            this.ballerinaSymbolKind = symbolKind;
            this.bSymbol = bSymbol;
            this.context = context;
        }

        /**
         * Build the Ballerina Symbol.
         *
         * @return {@link BallerinaSymbol} built
         */
        public abstract BallerinaSymbol build();
    }
}
