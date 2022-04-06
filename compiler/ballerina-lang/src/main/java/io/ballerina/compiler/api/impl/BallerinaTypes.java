/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.api.impl;

import io.ballerina.compiler.api.Types;
import io.ballerina.compiler.api.impl.util.FieldMap;
import io.ballerina.compiler.api.symbols.Symbol;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.Flags;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the set of built-in and user-defined types available in a single semantic context.
 *
 * @since 2201.1.0
 */
public class BallerinaTypes extends Types {

    private BallerinaTypes(CompilerContext context) {
        super(context);
        context.put(TYPES_KEY, this);
    }

    public static Types getInstance(CompilerContext context) {
        Types types = context.get(TYPES_KEY);
        if (types == null) {
            types = new BallerinaTypes(context);
        }

        return types;
    }

    /**
     * {@inheritDoc}
     */
    public Optional<Symbol> getTypeByName(String org, String moduleName, String version, String typeDefName) {
        if (org == null || moduleName == null || version == null || typeDefName == null) {
            throw new IllegalArgumentException("Null parameters are not allowed. Found parameter values are org: " +
                    org + " moduleName: " + moduleName + ", version: " + version + ", and typeDefName: " + typeDefName);
        }

        PackageID packageID = new PackageID(Names.fromString(org), Names.fromString(moduleName),
                Names.fromString(version));

        BPackageSymbol packageSymbol = packageCache.getSymbol(packageID);
        if (packageSymbol == null) {
            return Optional.empty();
        }

        SymbolEnv pkgEnv = symbolTable.pkgEnvMap.get(packageSymbol);
        Scope.ScopeEntry entry = pkgEnv.scope.lookup(Names.fromString(typeDefName));
        if (isValidTypeDef(entry.symbol)) {
            return Optional.of(symbolFactory.getBCompiledSymbol(entry.symbol, typeDefName));
        }

        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    public Optional<Map<String, Symbol>> typesInModule(String org, String moduleName, String version) {
        if (org == null || moduleName == null || version == null) {
            throw new IllegalArgumentException("Null parameters are not allowed. Found parameter values are org: " +
                    org + " moduleName: " + moduleName + ", and version: " + version);
        }

        PackageID packageID = new PackageID(Names.fromString(org), Names.fromString(moduleName),
                Names.fromString(version));

        BPackageSymbol packageSymbol = packageCache.getSymbol(packageID);
        if (packageSymbol == null) {
            return Optional.empty();
        }

        SymbolEnv pkgEnv = symbolTable.pkgEnvMap.get(packageSymbol);
        Scope pkgEnvScope = pkgEnv.scope;

        if (pkgEnvScope == null || pkgEnvScope.entries == null) {
            return Optional.empty();
        }

        Map<String, Symbol> typeDefSymbols = new FieldMap<>();
        for (Scope.ScopeEntry scopeEntry : pkgEnvScope.entries.values()) {
            BSymbol bSymbol = scopeEntry.symbol;
            if (isValidTypeDef(bSymbol)) {
                String typeDefName = bSymbol.getOriginalName().getValue();
                typeDefSymbols.put(typeDefName, symbolFactory.getBCompiledSymbol(bSymbol, typeDefName));
            }
        }

        return Optional.of(Collections.unmodifiableMap(typeDefSymbols));
    }

    private boolean isValidTypeDef(BSymbol bSymbol) {
        return bSymbol != null && bSymbol.getOrigin() != SymbolOrigin.VIRTUAL
                && (bSymbol.getKind() == SymbolKind.TYPE_DEF
                || bSymbol.getKind() == SymbolKind.CONSTANT
                || bSymbol.getKind() == SymbolKind.ENUM
                || Symbols.isFlagOn(bSymbol.flags, Flags.CLASS));
    }
}
