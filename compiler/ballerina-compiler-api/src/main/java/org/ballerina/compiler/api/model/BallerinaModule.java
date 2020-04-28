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
package org.ballerina.compiler.api.model;

import org.ballerina.compiler.api.semantic.SymbolFactory;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a ballerina module.
 * 
 * @since 1.3.0
 */
public class BallerinaModule extends BallerinaSymbol {
    private BPackageSymbol packageSymbol;
    
    protected BallerinaModule(String name,
                              PackageID moduleID,
                              BallerinaSymbolKind ballerinaSymbolKind,
                              BPackageSymbol packageSymbol) {
        super(name, moduleID, ballerinaSymbolKind, packageSymbol);
        this.packageSymbol = packageSymbol;
    }

    /**
     * Get the public type definitions defined within the module.
     * 
     * @return {@link List} of type definitions
     */
    public List<BallerinaTypeDefinition> getTypeDefinitions() {
        List<BallerinaTypeDefinition> typeDefs = new ArrayList<>();
        this.packageSymbol.scope.entries.forEach((name, scopeEntry) -> {
            if (scopeEntry.symbol instanceof BTypeSymbol
                    && (scopeEntry.symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
                typeDefs.add(SymbolFactory.createTypeDefinition((BTypeSymbol) scopeEntry.symbol));
            }
        });
        
        return typeDefs;
    }

    /**
     * Get the public functions defined within the module.
     *
     * @return {@link List} of type definitions
     */
    public List<BallerinaFunctionSymbol> getFunctions() {
        List<BallerinaFunctionSymbol> functions = new ArrayList<>();
        this.packageSymbol.scope.entries.forEach((name, scopeEntry) -> {
            if (scopeEntry.symbol instanceof BInvokableSymbol
                    && (scopeEntry.symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
                functions.add(SymbolFactory.createFunctionSymbol((BInvokableSymbol) scopeEntry.symbol));
            }
        });
        return functions;
    }

    /**
     * Get the public constants defined within the module.
     *
     * @return {@link List} of type definitions
     */
    public List<BallerinaConstantSymbol> getConstants() {
        List<BallerinaConstantSymbol> constants = new ArrayList<>();
        this.packageSymbol.scope.entries.forEach((name, scopeEntry) -> {
            if (scopeEntry.symbol instanceof BConstantSymbol
                    && (scopeEntry.symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
                constants.add(SymbolFactory.createConstantSymbol((BConstantSymbol) scopeEntry.symbol));
            }
        });
        
        return constants;
    }

    /**
     * Get all public the symbols within the module.
     *
     * @return {@link List} of type definitions
     */
    public List<BCompiledSymbol> getAllSymbols() {
        List<BCompiledSymbol> symbols = new ArrayList<>();
        this.packageSymbol.scope.entries.forEach((name, scopeEntry) -> {
            if (scopeEntry.symbol instanceof BConstantSymbol
                    && (scopeEntry.symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
                symbols.add(SymbolFactory.getBCompiledSymbol(scopeEntry.symbol));
            }
        });
        
        return symbols;
    }
    
    /**
     * Represents Ballerina Module Symbol Builder.
     * 
     * @since 1.3.0
     */
    public static class ModuleSymbolBuilder extends SymbolBuilder<ModuleSymbolBuilder> {
        
        public ModuleSymbolBuilder(String name,
                                   PackageID moduleID,
                                   BallerinaSymbolKind symbolKind,
                                   BPackageSymbol packageSymbol) {
            super(name, moduleID, symbolKind, packageSymbol);
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
                    this.ballerinaSymbolKind,
                    (BPackageSymbol) this.bSymbol);
        }
    }
}
