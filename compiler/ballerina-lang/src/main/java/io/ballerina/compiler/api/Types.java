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

package io.ballerina.compiler.api;

import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.Flags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the Types of a semantic model.
 *
 * @since 2.0.0
* */

public class Types {

    private static final CompilerContext.Key<Types> TYPES_KEY = new CompilerContext.Key<>();
    private final SymbolFactory symbolFactory;
    private final SymbolTable symbolTable;
    private final SymbolResolver symbolResolver;
    private final PackageCache packageCache;

    public final TypeSymbol BOOLEAN;
    public final TypeSymbol INT;
    public final TypeSymbol FLOAT;
    public final TypeSymbol DECIMAL;
    public final TypeSymbol STRING;
    public final TypeSymbol NIL;
    public final TypeSymbol XML;
    public final TypeSymbol ERROR;
    public final TypeSymbol FUNCTION;
    public final TypeSymbol FUTURE;
    public final TypeSymbol TYPEDESC;
    public final TypeSymbol HANDLE;
    public final TypeSymbol STREAM;
    public final TypeSymbol ANY;
    public final TypeSymbol ANYDATA;
    public final TypeSymbol NEVER;
    public final TypeSymbol READONLY;
    public final TypeSymbol JSON;
    public final TypeSymbol BYTE;
    public final TypeSymbol COMPILATION_ERROR;


    private Types(CompilerContext context) {
        context.put(TYPES_KEY, this);
        TypesFactory typesFactory = TypesFactory.getInstance(context);
        this.symbolFactory = SymbolFactory.getInstance(context);
        this.symbolTable = SymbolTable.getInstance(context);
        this.symbolResolver = SymbolResolver.getInstance(context);
        this.packageCache = PackageCache.getInstance(context);

        this.BOOLEAN = typesFactory.getTypeDescriptor(symbolTable.booleanType);
        this.INT = typesFactory.getTypeDescriptor(symbolTable.intType);
        this.FLOAT = typesFactory.getTypeDescriptor(symbolTable.floatType);
        this.DECIMAL = typesFactory.getTypeDescriptor(symbolTable.decimalType);
        this.STRING = typesFactory.getTypeDescriptor(symbolTable.stringType);
        this.NIL = typesFactory.getTypeDescriptor(symbolTable.nilType);
        this.XML = typesFactory.getTypeDescriptor(symbolTable.xmlType);
        this.ERROR = typesFactory.getTypeDescriptor(symbolTable.errorType);
        this.FUNCTION = typesFactory.getTypeDescriptor(symbolTable.invokableType);
        this.FUTURE = typesFactory.getTypeDescriptor(symbolTable.futureType);
        this.TYPEDESC = typesFactory.getTypeDescriptor(symbolTable.typeDesc);
        this.HANDLE = typesFactory.getTypeDescriptor(symbolTable.handleType);
        this.STREAM = typesFactory.getTypeDescriptor(symbolTable.streamType);
        this.ANY = typesFactory.getTypeDescriptor(symbolTable.anyType);
        this.ANYDATA = typesFactory.getTypeDescriptor(symbolTable.anydataType);
        this.NEVER = typesFactory.getTypeDescriptor(symbolTable.neverType);
        this.READONLY = typesFactory.getTypeDescriptor(symbolTable.readonlyType);
        this.JSON = typesFactory.getTypeDescriptor(symbolTable.jsonType);
        this.BYTE = typesFactory.getTypeDescriptor(symbolTable.byteType);
        this.COMPILATION_ERROR = typesFactory.getTypeDescriptor(symbolTable.semanticError);
    }

    public Optional<Symbol> getByName(String org, String moduleName, String version, String typeDefName) {
        PackageID packageID = new PackageID(Names.fromString(org), Names.fromString(moduleName),
                Names.fromString(version));

        return getTypeDefByName(packageID, typeDefName);

    }

    public Optional<Map<String, Symbol>> typesInModule(String org, String moduleName, String version) {
        PackageID packageID = new PackageID(Names.fromString(org), Names.fromString(moduleName),
                Names.fromString(version));

        return getTypeDefSymbolsInModule(packageID);
    }

    public static Types getInstance(CompilerContext context) {
        Types types = context.get(TYPES_KEY);
        if (types == null) {
            types = new Types(context);
        }

        return types;
    }

    private Optional<Symbol> getTypeDefByName(PackageID packageID, String typeDefName) {
        BPackageSymbol packageSymbol = packageCache.getSymbol(packageID);
        if (packageSymbol == null) {
            return Optional.empty();
        }

        SymbolEnv pkgEnv = symbolTable.pkgEnvMap.get(packageSymbol);

        BSymbol bSymbol;
        Name typeDef = Names.fromString(typeDefName);
        bSymbol = symbolResolver.lookupSymbolInMainSpace(pkgEnv, typeDef);

        if (bSymbol == symbolTable.notFoundSymbol) {
            bSymbol = findByNameInImportedPackages(packageSymbol.imports, typeDef);
        }

        if (isValidTypeDef(bSymbol)) {
            return Optional.ofNullable(symbolFactory.getBCompiledSymbol(bSymbol, typeDefName));
        }

        return Optional.empty();
    }

    private BSymbol findByNameInImportedPackages(List<BPackageSymbol> imports, Name typeDef) {
        for (BPackageSymbol packageSymbol : imports) {
            SymbolEnv pkgEnv = symbolTable.pkgEnvMap.get(packageSymbol);
            BSymbol bSymbol = symbolResolver.lookupSymbolInMainSpace(pkgEnv, typeDef);
            if (bSymbol != symbolTable.notFoundSymbol) {
                return bSymbol;
            }
        }

        return symbolTable.notFoundSymbol;
    }

    private Optional<Map<String, Symbol>> getTypeDefSymbolsInModule(PackageID packageID) {
        BPackageSymbol packageSymbol = packageCache.getSymbol(packageID);
        if (packageSymbol == null) {
            return Optional.empty();
        }

        SymbolEnv pkgEnv = symbolTable.pkgEnvMap.get(packageSymbol);
        Scope pkgEnvScope = pkgEnv.scope;
        if (pkgEnvScope != null && pkgEnvScope.entries != null) {
            Map<String, Symbol> typeDefSymbols = new HashMap<>();
            for (Scope.ScopeEntry scopeEntry : pkgEnvScope.entries.values()) {
                BSymbol bSymbol = scopeEntry.symbol;
                if (isValidTypeDef(bSymbol)) {
                    String typeDefName = bSymbol.getName().getValue();
                    typeDefSymbols.put(typeDefName, symbolFactory.getBCompiledSymbol(bSymbol, typeDefName));
                }
            }

            for (BPackageSymbol importedPkgs : packageSymbol.imports) {
                findTypeDefsInPackageSymbol(importedPkgs, typeDefSymbols);
            }

            return Optional.of(typeDefSymbols);
        }

        return Optional.empty();
    }

    private void findTypeDefsInPackageSymbol(BSymbol packageSymbol, Map<String, Symbol> typeDefSymbols) {
        if (packageSymbol != null) {
            SymbolEnv pkgEnv = symbolTable.pkgEnvMap.get(packageSymbol);
            Scope pkgEnvScope = pkgEnv.scope;
            if (pkgEnvScope != null && pkgEnvScope.entries != null) {
                for (Scope.ScopeEntry scopeEntry : pkgEnvScope.entries.values()) {
                    BSymbol bSymbol = scopeEntry.symbol;
                    if (isValidTypeDef(bSymbol)) {
                        String typeDefName = packageSymbol.getName().getValue() + ":" + bSymbol.getName().getValue();
                        typeDefSymbols.put(typeDefName, symbolFactory.getBCompiledSymbol(bSymbol, typeDefName));
                    } else if (bSymbol.tag == SymTag.PACKAGE) {
                        findTypeDefsInPackageSymbol(bSymbol, typeDefSymbols);
                    }
                }
            }
        }

    }

    private boolean isValidTypeDef(BSymbol bSymbol) {
        return bSymbol != null && bSymbol != symbolTable.notFoundSymbol && bSymbol.getOrigin() != SymbolOrigin.VIRTUAL
                && (bSymbol.tag == SymTag.TYPE_DEF
                || bSymbol.tag == SymTag.CONSTANT
                || bSymbol.tag == SymTag.ENUM
                || Flags.unMask(bSymbol.flags).contains(Flag.CLASS));
    }

}
