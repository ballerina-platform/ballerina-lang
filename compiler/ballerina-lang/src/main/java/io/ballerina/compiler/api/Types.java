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

import io.ballerina.compiler.api.impl.BallerinaTypes;
import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.projects.Document;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Map;
import java.util.Optional;

/**
 * Represents the abstract interface of the Types API. Used to access the set of built-in and user-defined
 * types available in a single semantic context.
 *
 * @since 2201.1.0
 */
public abstract class Types {
    protected static final CompilerContext.Key<BallerinaTypes> TYPES_KEY = new CompilerContext.Key<>();
    protected final CompilerContext context;
    protected final SymbolFactory symbolFactory;
    protected final SymbolTable symbolTable;
    protected final PackageCache packageCache;
    protected final BLangPackage bLangPackage;

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
    public final TypeSymbol REGEX;
    public final TypeSymbol RAW_TEMPLATE;

    protected Types(BLangPackage bLangPackage, CompilerContext context) {
        this.bLangPackage = bLangPackage;
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
        this.REGEX = typesFactory.getTypeDescriptor(symbolTable.regExpType);
        this.RAW_TEMPLATE = typesFactory.getTypeDescriptor(symbolTable.rawTemplateType);
    }

    /**
     * Parses a string representation of a Ballerina type descriptor into a {@link TypeSymbol}.
     * Referenced named types within the {@code text} are resolved by searching built-in types,
     * user-defined types declared within the current module (provided by the {@code document}),
     * and types accessible via the import statements within that {@code document}.
     *
     * @param document The document used to resolve types within the current module and its imports.
     * @param text     The string representation of the Ballerina type descriptor to parse.
     * @return An {@link Optional} containing the resolved {@link TypeSymbol}, or empty if parsing fails or the type is
     * invalid or not found within the accessible scope.
     */
    public abstract Optional<TypeSymbol> getType(Document document, String text);

    /**
     * Parses a string representation of a Ballerina type descriptor into a {@link TypeSymbol}. Resolves types using
     * built-ins, the current module (via {@code document}), its existing imports, and the additionally provided
     * {@code importModules}.
     *
     * @param document      The document for resolving types within the current module and its imports.
     * @param text          The string representation of the Ballerina type descriptor.
     * @param importModules Additional modules that are not imported in the {@code document} can be defined with
     *                      'module_prefix -> module', and are accounted for in type resolution.
     * @return An {@link Optional} containing the resolved {@link TypeSymbol}, or empty if parsing fails or the type is
     * not found/invalid within the combined scope.
     */
    public abstract Optional<TypeSymbol> getType(Document document, String text,
                                                 Map<String, BLangPackage> importModules);

    /**
     * Lookup for the symbol of a user defined type within a given module. This would be considering type
     * definitions, constants, enums, enum members, and class definitions as valid user defined types when looking up.
     * An empty Optional instance is returned if the provided module is not found or, if the given type is either
     * invalid or not defined within the provided module. The module is determined by the provided org, module name
     * and the version. All parameters are expected to be non-null values.
     *
     * @param org           The organization of the looking up module
     * @param moduleName    The name of the looking up module
     * @param version       The version of the looking up module
     * @param typeDefName   The type definition
     * @return The {@link Symbol} of the user defined type
     */
    public abstract Optional<Symbol> getTypeByName(String org, String moduleName, String version, String typeDefName);

    /**
     * Lookup for all the symbols of user defined types within a given module. This would be considering type
     * definitions, constants, enums, enum members, and class definitions as valid user defined types when looking up.
     * An empty Optional instance is returned if no valid user defined type is found within the provided module. The
     * module is determined by the provided org, module name and the version. All parameters are expected to be
     * non-null values.
     *
     * @param org           The organization of the looking up module
     * @param moduleName    The name of the looking up module
     * @param version       The version of the looking up module
     * @return A {@link Map} of the user defined type symbols
     */
    public abstract Optional<Map<String, Symbol>> typesInModule(String org, String moduleName, String version);

    /**
     * Retrieves a single instance of the builders used to construct more complex types.
     *
     * @return An instance of the {@link TypeBuilder} for a given semantic context
     */
    public abstract TypeBuilder builder();
}
