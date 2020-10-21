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

import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ServiceSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.Scope.ScopeEntry;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ballerinalang.model.symbols.SymbolOrigin.COMPILED_SOURCE;
import static org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols.isFlagOn;

/**
 * Represents a ballerina module.
 *
 * @since 2.0.0
 */
public class BallerinaModule extends BallerinaSymbol implements ModuleSymbol {

    private BPackageSymbol packageSymbol;
    private List<TypeSymbol> typeDefs;
    private List<FunctionSymbol> functions;
    private List<ConstantSymbol> constants;
    private List<TypeSymbol> listeners;
    private List<Symbol> allSymbols;

    protected BallerinaModule(String name, PackageID moduleID, BPackageSymbol packageSymbol) {
        super(name, moduleID, SymbolKind.MODULE, packageSymbol);
        this.packageSymbol = packageSymbol;
    }

    /**
     * Get the public functions defined within the module.
     *
     * @return {@link List} of type definitions
     */
    @Override
    public List<FunctionSymbol> functions() {
        if (this.functions != null) {
            return functions;
        }

        List<FunctionSymbol> functions = new ArrayList<>();
        for (Map.Entry<Name, ScopeEntry> entry : this.packageSymbol.scope.entries.entrySet()) {
            ScopeEntry scopeEntry = entry.getValue();
            if (scopeEntry.symbol != null
                    && scopeEntry.symbol.kind == org.ballerinalang.model.symbols.SymbolKind.FUNCTION
                    && isFlagOn(scopeEntry.symbol.flags, Flags.PUBLIC)
                    && scopeEntry.symbol.origin == COMPILED_SOURCE) {
                String funcName = scopeEntry.symbol.getName().getValue();
                functions.add(SymbolFactory.createFunctionSymbol((BInvokableSymbol) scopeEntry.symbol, funcName));
            }
        }

        this.functions = Collections.unmodifiableList(functions);
        return this.functions;
    }

    /**
     * Get the public type definitions defined within the module.
     *
     * @return {@link List} of type definitions
     */
    @Override
    public List<TypeSymbol> typeDefinitions() {
        if (this.typeDefs == null) {
            this.typeDefs = this.allSymbols().stream()
                    .filter(symbol -> symbol.kind() == SymbolKind.TYPE)
                    .map(symbol -> (TypeSymbol) symbol)
                    .collect(Collectors.toUnmodifiableList());
        }

        return this.typeDefs;
    }

    /**
     * Get the public constants defined within the module.
     *
     * @return {@link List} of type definitions
     */
    @Override
    public List<ConstantSymbol> constants() {
        if (this.constants != null) {
            return this.constants;
        }

        List<ConstantSymbol> constants = new ArrayList<>();
        for (Map.Entry<Name, ScopeEntry> entry : this.packageSymbol.scope.entries.entrySet()) {
            ScopeEntry scopeEntry = entry.getValue();
            if (scopeEntry.symbol instanceof BConstantSymbol &&
                    (scopeEntry.symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
                String constName = scopeEntry.symbol.getName().getValue();
                constants.add(SymbolFactory.createConstantSymbol((BConstantSymbol) scopeEntry.symbol, constName));
            }
        }

        this.constants = Collections.unmodifiableList(constants);
        return this.constants;
    }

    /**
     * Get the listeners in the Module.
     *
     * @return {@link List} of listeners
     */
    @Override
    public List<TypeSymbol> listeners() {
        if (this.listeners != null) {
            return listeners;
        }

        // TODO:
        this.listeners = new ArrayList<>();
        return this.listeners;
    }

    @Override
    public List<ServiceSymbol> services() {
        return new ArrayList<>();
    }

    /**
     * Get all public the symbols within the module.
     *
     * @return {@link List} of type definitions
     */
    @Override
    public List<Symbol> allSymbols() {
        if (this.allSymbols == null) {
            List<Symbol> symbols = new ArrayList<>();
            for (Map.Entry<Name, ScopeEntry> entry : this.packageSymbol.scope.entries.entrySet()) {
                ScopeEntry scopeEntry = entry.getValue();
                if (!isFlagOn(scopeEntry.symbol.flags, Flags.PUBLIC) || scopeEntry.symbol.origin != COMPILED_SOURCE) {
                    continue;
                }
                symbols.add(SymbolFactory.getBCompiledSymbol(scopeEntry.symbol,
                        scopeEntry.symbol.getName().getValue()));
            }
            this.allSymbols = Collections.unmodifiableList(symbols);
        }

        return this.allSymbols;
    }

    /**
     * Represents Ballerina Module Symbol Builder.
     *
     * @since 2.0.0
     */
    public static class ModuleSymbolBuilder extends SymbolBuilder<ModuleSymbolBuilder> {

        public ModuleSymbolBuilder(String name, PackageID moduleID, BPackageSymbol packageSymbol) {
            super(name, moduleID, SymbolKind.MODULE, packageSymbol);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public BallerinaModule build() {
            if (this.bSymbol == null) {
                throw new AssertionError("Package Symbol cannot be null");
            }
            return new BallerinaModule(this.name, this.moduleID, (BPackageSymbol) this.bSymbol);
        }
    }
}
