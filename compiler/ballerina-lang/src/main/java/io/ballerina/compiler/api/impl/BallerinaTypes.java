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

import io.ballerina.compiler.api.TypeBuilder;
import io.ballerina.compiler.api.Types;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;
import io.ballerina.compiler.api.impl.util.FieldMap;
import io.ballerina.compiler.api.impl.util.SymbolUtils;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.projects.Document;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.parser.BLangNodeBuilder;
import org.wso2.ballerinalang.compiler.semantics.analyzer.TypeResolver;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
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

    private final TypeResolver typeResolver;

    public BallerinaTypes(BLangPackage bLangPackage, CompilerContext context) {
        super(bLangPackage, context);
        context.put(TYPES_KEY, this);
        this.typeResolver = TypeResolver.getInstance(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TypeSymbol> getType(Document document, String text) {
        // Obtain the compilation unit
        Optional<BLangCompilationUnit> compilationUnit = SymbolUtils.getCompilationUnit(bLangPackage, document);
        if (compilationUnit.isEmpty()) {
            return Optional.empty();
        }

        SymbolEnv pkgEnv = symbolTable.pkgEnvMap.get(bLangPackage.symbol);
        return getType(text, pkgEnv, compilationUnit.get().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TypeSymbol> getType(Document document, String text, Map<String, BLangPackage> importModules) {
        // Obtain the compilation unit
        Optional<BLangCompilationUnit> compilationUnit = SymbolUtils.getCompilationUnit(bLangPackage, document);
        if (compilationUnit.isEmpty()) {
            return Optional.empty();
        }

        // Define the packages in the environment
        SymbolEnv pkgEnv = symbolTable.pkgEnvMap.get(bLangPackage.symbol);
        Name compUnitName = Names.fromString(compilationUnit.get().getName());
        importModules.forEach((prefix, importPackage) -> {
            importPackage.symbol.compUnit = compUnitName;
            pkgEnv.scope.define(Names.fromString(prefix), importPackage.symbol);
        });

       return getType(text, pkgEnv, compUnitName.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
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

    /**
     * {@inheritDoc}
     */
    @Override
    public TypeBuilder builder() {
        return new BallerinaTypeBuilder(context);
    }

    private Optional<TypeSymbol> getType(String text, SymbolEnv pkgEnv, String compUnitName) {
        // Obtain the ST node
        TypeDescriptorNode typeDescriptorNode = NodeParser.parseTypeDescriptor(text);
        if (typeDescriptorNode == null || typeDescriptorNode.hasDiagnostics()) {
            return Optional.empty();
        }

        // Obtain the AST node
        BLangNodeBuilder bLangNodeBuilder =
                new BLangNodeBuilder(context, bLangPackage.packageID, compUnitName);
        BLangNode bLangNode = typeDescriptorNode.apply(bLangNodeBuilder);

        // Resolve the type
        BType resolvedType;
        if (bLangNode.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) bLangNode;
            BSymbol symbolOfVarRef = typeResolver.getSymbolOfVarRef(simpleVarRef.pos, pkgEnv,
                    Names.fromString(simpleVarRef.pkgAlias.value),
                    Names.fromString(simpleVarRef.variableName.value));
            resolvedType = symbolOfVarRef.type;
        } else if (bLangNode instanceof BLangType bLangType) {
            try {
                typeResolver.resolveTypeDesc(bLangType, pkgEnv);
                resolvedType = bLangType.getBType();
            } catch (Throwable ignored) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
        if (resolvedType.getKind() == TypeKind.OTHER) {
            return Optional.empty();
        }

        // Generate the type symbol
        return Optional.of(TypesFactory.getInstance(context).getTypeDescriptor(resolvedType));
    }

    private boolean isValidTypeDef(BSymbol bSymbol) {
        return bSymbol != null && bSymbol.getOrigin() != SymbolOrigin.VIRTUAL
                && (bSymbol.getKind() == SymbolKind.TYPE_DEF
                || bSymbol.getKind() == SymbolKind.CONSTANT
                || bSymbol.getKind() == SymbolKind.ENUM
                || Symbols.isFlagOn(bSymbol.flags, Flags.CLASS));
    }
}
