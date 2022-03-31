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
package io.ballerina.compiler.api.types;

import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.Flags;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the set of built-in and user-defined types available in a single semantic context.
 *
 * @since 2.0.0
 */

public class Types {

    private static final CompilerContext.Key<Types> TYPES_KEY = new CompilerContext.Key<>();
    private final CompilerContext context;
    private final SymbolFactory symbolFactory;
    private final SymbolTable symbolTable;
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
        this.context = context;
        TypesFactory typesFactory = TypesFactory.getInstance(context);
        this.symbolFactory = SymbolFactory.getInstance(context);
        this.symbolTable = SymbolTable.getInstance(context);
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

    public TypeBuilder builder() {
        return BallerinaTypeBuilder.getInstance(this.context);
    }

    /**
     * Lookup for the symbol of a user defined type within a given module. This would be considering type
     * definitions, enums, enum members, and class definitions as valid user defined types when
     * looking up. The module is determined by the provided org, module name and the version.
     *
     * @param org           The organization of the looking up module
     * @param moduleName    The name of the looking up module
     * @param version       The version of the looking up module
     * @param typeDefName   The type definition
     * @return The {@link Symbol} of the user defined type
     */
    public Optional<Symbol> getTypeByName(String org, String moduleName, String version, String typeDefName) {
        PackageID packageID = new PackageID(Names.fromString(org), Names.fromString(moduleName),
                Names.fromString(version));

        return getTypeDefByName(packageID, typeDefName);
    }

    /**
     * Lookup for all the symbols of user defined types within a given module. This would be considering type
     * definitions, enums, enum members, and class definitions as valid user defined types when looking up.
     * The module is determined by the provided org, module name and the version.
     *
     * @param org           The organization of the looking up module
     * @param moduleName    The name of the looking up module
     * @param version       The version of the looking up module
     * @return A {@link Map} of the user defined type symbols
     */
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
        Scope.ScopeEntry entry = pkgEnv.scope.lookup(Names.fromString(typeDefName));
        if (entry != null) {
            if (isValidTypeDef(entry.symbol)) {
                return Optional.ofNullable(symbolFactory.getBCompiledSymbol(entry.symbol, typeDefName));
            }
        }

        return Optional.empty();
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
                    String typeDefName = bSymbol.getOriginalName().getValue();
                    typeDefSymbols.put(typeDefName, symbolFactory.getBCompiledSymbol(bSymbol, typeDefName));
                }
            }

            return Optional.of(Collections.unmodifiableMap(typeDefSymbols));
        }

        return Optional.empty();
    }

    private boolean isValidTypeDef(BSymbol bSymbol) {
        return bSymbol != null && bSymbol.getOrigin() != SymbolOrigin.VIRTUAL
                && (bSymbol.getKind() == SymbolKind.TYPE_DEF
                || bSymbol.getKind() == SymbolKind.CONSTANT
                || bSymbol.getKind() == SymbolKind.ENUM
                || Symbols.isFlagOn(bSymbol.flags, Flags.CLASS));
    }
}
