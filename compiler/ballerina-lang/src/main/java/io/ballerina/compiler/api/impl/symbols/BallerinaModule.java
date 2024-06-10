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
import io.ballerina.compiler.api.SymbolTransformer;
import io.ballerina.compiler.api.SymbolVisitor;
import io.ballerina.compiler.api.impl.BallerinaModuleID;
import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.EnumSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.Scope.ScopeEntry;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BClassSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEnumSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.model.symbols.SymbolOrigin.BUILTIN;
import static org.ballerinalang.model.symbols.SymbolOrigin.COMPILED_SOURCE;
import static org.ballerinalang.model.symbols.SymbolOrigin.SOURCE;
import static org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols.isFlagOn;

/**
 * Represents a ballerina module.
 *
 * @since 2.0.0
 */
public class BallerinaModule extends BallerinaSymbol implements ModuleSymbol {

    private BPackageSymbol packageSymbol;
    private List<TypeDefinitionSymbol> typeDefs;
    private List<ClassSymbol> classes;
    private List<FunctionSymbol> functions;
    private List<ConstantSymbol> constants;
    private List<EnumSymbol> enums;
    private List<Symbol> allSymbols;
    private ModuleID id;

    protected BallerinaModule(CompilerContext context, String name, BPackageSymbol packageSymbol) {
        super(name, SymbolKind.MODULE, packageSymbol, context);
        this.packageSymbol = packageSymbol;
    }

    @Override
    public Optional<ModuleSymbol> getModule() {
        return Optional.of(this);
    }

    @Override
    public ModuleID id() {
        if (this.id != null) {
            return this.id;
        }

        ModuleID moduleID;
        if (this.packageSymbol.importPrefix != null) {
            moduleID = new BallerinaModuleID(this.packageSymbol.pkgID, this.packageSymbol.importPrefix);
        } else {
            moduleID = new BallerinaModuleID(this.packageSymbol.pkgID);
        }
        this.id = moduleID;
        return this.id;
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

        SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);
        List<FunctionSymbol> functions = new ArrayList<>();

        for (Map.Entry<Name, ScopeEntry> entry : this.packageSymbol.scope.entries.entrySet()) {
            ScopeEntry scopeEntry = entry.getValue();

            if (scopeEntry.symbol != null
                    && scopeEntry.symbol.kind == org.ballerinalang.model.symbols.SymbolKind.FUNCTION
                    && isFlagOn(scopeEntry.symbol.flags, Flags.PUBLIC)
                    && (scopeEntry.symbol.origin == SOURCE || scopeEntry.symbol.origin == COMPILED_SOURCE)) {
                String funcName = scopeEntry.symbol.getOriginalName().getValue();
                functions.add(symbolFactory.createFunctionSymbol((BInvokableSymbol) scopeEntry.symbol, funcName));
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
    public List<TypeDefinitionSymbol> typeDefinitions() {
        if (this.typeDefs == null) {
            this.typeDefs = this.allSymbols().stream()
                    .filter(symbol -> symbol.kind() == SymbolKind.TYPE_DEFINITION)
                    .map(symbol -> (TypeDefinitionSymbol) symbol)
                    .toList();
        }

        return this.typeDefs;
    }

    /**
     * Get the public class definitions defined within the module.
     *
     * @return {@link List} of class definitions
     */
    @Override
    public List<ClassSymbol> classes() {
        if (this.classes != null) {
            return this.classes;
        }

        SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);
        List<ClassSymbol> classes = new ArrayList<>();

        for (Map.Entry<Name, ScopeEntry> entry : this.packageSymbol.scope.entries.entrySet()) {
            ScopeEntry scopeEntry = entry.getValue();
            if (scopeEntry.symbol instanceof BClassSymbol &&
                    (scopeEntry.symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
                String constName = scopeEntry.symbol.getOriginalName().getValue();
                classes.add(symbolFactory.createClassSymbol((BClassSymbol) scopeEntry.symbol, constName));
            }
        }

        this.classes = Collections.unmodifiableList(classes);
        return this.classes;
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

        SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);
        List<ConstantSymbol> constants = new ArrayList<>();

        for (Map.Entry<Name, ScopeEntry> entry : this.packageSymbol.scope.entries.entrySet()) {
            ScopeEntry scopeEntry = entry.getValue();

            if (scopeEntry.symbol instanceof BConstantSymbol &&
                    (scopeEntry.symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
                String constName = scopeEntry.symbol.getOriginalName().getValue();
                constants.add(symbolFactory.createConstantSymbol((BConstantSymbol) scopeEntry.symbol, constName));
            }
        }

        this.constants = Collections.unmodifiableList(constants);
        return this.constants;
    }

    @Override
    public List<EnumSymbol> enums() {
        if (this.enums != null) {
            return this.enums;
        }

        SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);
        List<EnumSymbol> enums = new ArrayList<>();

        for (Map.Entry<Name, ScopeEntry> entry : this.packageSymbol.scope.entries.entrySet()) {
            ScopeEntry scopeEntry = entry.getValue();

            if (scopeEntry.symbol instanceof BEnumSymbol && Symbols.isFlagOn(scopeEntry.symbol.flags, Flags.PUBLIC)) {
                String constName = scopeEntry.symbol.getOriginalName().getValue();
                enums.add(symbolFactory.createEnumSymbol((BEnumSymbol) scopeEntry.symbol, constName));
            }
        }

        this.enums = Collections.unmodifiableList(enums);
        return this.enums;
    }

    /**
     * Get all public the symbols within the module.
     *
     * @return {@link List} of type definitions
     */
    @Override
    public List<Symbol> allSymbols() {
        if (this.allSymbols == null) {
            SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);
            List<Symbol> symbols = new ArrayList<>();

            for (Map.Entry<Name, ScopeEntry> entry : this.packageSymbol.scope.entries.entrySet()) {
                ScopeEntry scopeEntry = entry.getValue();
                if (!isFlagOn(scopeEntry.symbol.flags, Flags.PUBLIC)
                        || (scopeEntry.symbol.origin != COMPILED_SOURCE && scopeEntry.symbol.origin != SOURCE
                        && scopeEntry.symbol.origin != BUILTIN)) {
                    continue;
                }
                symbols.add(symbolFactory.getBCompiledSymbol(scopeEntry.symbol,
                                                             scopeEntry.symbol.getOriginalName().getValue()));
            }

            this.allSymbols = Collections.unmodifiableList(symbols);
        }

        return this.allSymbols;
    }

    @Override
    public void accept(SymbolVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(SymbolTransformer<T> transformer) {
        return transformer.transform(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ModuleSymbol symbol)) {
            return false;
        }

        return this.id().equals(symbol.id());
    }

    @Override
    public int hashCode() {
        return this.id().hashCode();
    }

    /**
     * Represents Ballerina Module Symbol Builder.
     *
     * @since 2.0.0
     */
    public static class ModuleSymbolBuilder extends SymbolBuilder<ModuleSymbolBuilder> {

        public ModuleSymbolBuilder(CompilerContext context, String name, BPackageSymbol packageSymbol) {
            super(name, SymbolKind.MODULE, packageSymbol, context);
            this.context = context;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public BallerinaModule build() {
            if (this.bSymbol == null) {
                throw new AssertionError("Package Symbol cannot be null");
            }
            return new BallerinaModule(this.context, this.name, (BPackageSymbol) this.bSymbol);
        }
    }
}
