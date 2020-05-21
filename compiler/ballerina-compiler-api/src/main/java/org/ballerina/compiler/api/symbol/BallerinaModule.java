/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerina.compiler.api.symbol;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstructorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a ballerina module.
 *
 * @since 2.0.0
 */
public class BallerinaModule extends BallerinaSymbol {

    private BPackageSymbol packageSymbol;

    private List<BallerinaTypeDefinition> typeDefs;

    private List<BallerinaFunctionSymbol> functions;

    private List<BallerinaConstantSymbol> constants;

    private List<BallerinaVariable> listeners;

    protected BallerinaModule(String name,
                              PackageID moduleID,
                              BPackageSymbol packageSymbol) {
        super(name, moduleID, BallerinaSymbolKind.MODULE, packageSymbol);
        this.packageSymbol = packageSymbol;
    }

    /**
     * Get the public type definitions defined within the module.
     *
     * @return {@link List} of type definitions
     */
    public List<BallerinaTypeDefinition> typeDefinitions() {
        if (this.typeDefs == null) {
            this.typeDefs = getTypeDefinitions();
        }

        return this.typeDefs;
    }

    /**
     * Get the public functions defined within the module.
     *
     * @return {@link List} of type definitions
     */
    public List<BallerinaFunctionSymbol> functions() {
        if (this.functions == null) {
            this.functions = getFunctions();
        }

        return functions;
    }

    /**
     * Get the public constants defined within the module.
     *
     * @return {@link List} of type definitions
     */
    public List<BallerinaConstantSymbol> constants() {
        if (this.constants == null) {
            this.constants = getConstants();
        }

        return constants;
    }

    /**
     * Get the listeners in the Module.
     *
     * @return {@link List} of listeners
     */
    public List<BallerinaVariable> listeners() {
        if (this.listeners == null) {
            this.listeners = this.getListeners();
        }

        return listeners;
    }

    /**
     * Get all public the symbols within the module.
     *
     * @return {@link List} of type definitions
     */
    public List<BCompiledSymbol> allSymbols() {
        List<BCompiledSymbol> symbols = new ArrayList<>();
        symbols.addAll(this.typeDefinitions());
        symbols.addAll(this.functions());
        symbols.addAll(this.constants());

        return Collections.unmodifiableList(symbols);
    }

    private List<BallerinaTypeDefinition> getTypeDefinitions() {
        List<BallerinaTypeDefinition> typeDefs = new ArrayList<>();
        this.packageSymbol.scope.entries.forEach((name, scopeEntry) -> {
            if ((scopeEntry.symbol.flags & Flags.PUBLIC) != Flags.PUBLIC) {
                return;
            }
            if (scopeEntry.symbol instanceof BTypeSymbol) {
                String typeName = scopeEntry.symbol.getName().getValue();
                typeDefs.add(SymbolFactory.createTypeDefinition((BTypeSymbol) scopeEntry.symbol, typeName));
            } else if (scopeEntry.symbol instanceof BConstructorSymbol) {
                String typeName = scopeEntry.symbol.type.tsymbol.getName().getValue();
                typeDefs.add(SymbolFactory.createTypeDefinition(scopeEntry.symbol.type.tsymbol, typeName));
            }
        });

        return Collections.unmodifiableList(typeDefs);
    }

    private List<BallerinaFunctionSymbol> getFunctions() {
        List<BallerinaFunctionSymbol> functions = new ArrayList<>();
        this.packageSymbol.scope.entries.forEach((name, scopeEntry) -> {
            if (scopeEntry.symbol instanceof BInvokableSymbol
                    && (scopeEntry.symbol.flags & Flags.PUBLIC) == Flags.PUBLIC
                    && scopeEntry.symbol.kind != SymbolKind.ERROR_CONSTRUCTOR) {
                String funcName = scopeEntry.symbol.getName().getValue();
                functions.add(SymbolFactory.createFunctionSymbol((BInvokableSymbol) scopeEntry.symbol, funcName));
            }
        });
        return Collections.unmodifiableList(functions);
    }

    private List<BallerinaConstantSymbol> getConstants() {
        List<BallerinaConstantSymbol> constants = new ArrayList<>();
        this.packageSymbol.scope.entries.forEach((name, scopeEntry) -> {
            if (scopeEntry.symbol instanceof BConstantSymbol
                    && (scopeEntry.symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
                String constName = scopeEntry.symbol.getName().getValue();
                constants.add(SymbolFactory.createConstantSymbol((BConstantSymbol) scopeEntry.symbol, constName));
            }
        });

        return Collections.unmodifiableList(constants);
    }

    private List<BallerinaVariable> getListeners() {
        return new ArrayList<>();
    }

    /**
     * Represents Ballerina Module Symbol Builder.
     *
     * @since 1.3.0
     */
    public static class ModuleSymbolBuilder extends SymbolBuilder<ModuleSymbolBuilder> {

        public ModuleSymbolBuilder(String name,
                                   PackageID moduleID,
                                   BPackageSymbol packageSymbol) {
            super(name, moduleID, BallerinaSymbolKind.MODULE, packageSymbol);
        }

        /**
         * {@inheritDoc}
         */
        public BallerinaModule build() {
            if (this.bSymbol == null) {
                throw new AssertionError("Package Symbol cannot be null");
            }
            return new BallerinaModule(this.name,
                    this.moduleID,
                    (BPackageSymbol) this.bSymbol);
        }
    }
}
