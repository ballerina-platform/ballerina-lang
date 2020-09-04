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
package org.ballerina.compiler.impl;

import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextRange;
import org.ballerina.compiler.api.SemanticModel;
import org.ballerina.compiler.api.symbols.Symbol;
import org.ballerina.compiler.impl.symbols.SymbolFactory;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.Scope.ScopeEntry;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Semantic model representation of a given syntax tree.
 *
 * @since 2.0.0
 */
public class BallerinaSemanticModel implements SemanticModel {

    private final BLangPackage bLangPackage;
    private final CompilerContext compilerContext;
    protected BLangCompilationUnit compilationUnit;

    public BallerinaSemanticModel(BLangCompilationUnit compilationUnit, BLangPackage bLangPackage,
            CompilerContext context) {
        this.compilationUnit = compilationUnit;
        this.compilerContext = context;
        this.bLangPackage = bLangPackage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Symbol> visibleSymbols(LinePosition linePosition) {
        List<Symbol> compiledSymbols = new ArrayList<>();
        SymbolResolver symbolResolver = SymbolResolver.getInstance(this.compilerContext);
        SymbolTable symbolTable = SymbolTable.getInstance(this.compilerContext);
        SymbolEnv symbolEnv = symbolTable.pkgEnvMap.get(this.bLangPackage.symbol);
        SymbolsLookupVisitor lookupVisitor = new SymbolsLookupVisitor(linePosition, symbolEnv);
        Map<Name, List<Scope.ScopeEntry>> scopeSymbols =
                symbolResolver.getAllVisibleInScopeSymbols(lookupVisitor.lookUp(this.compilationUnit));

        for (Entry<Name, List<ScopeEntry>> entry : scopeSymbols.entrySet()) {
            Name name = entry.getKey();
            List<ScopeEntry> scopeEntries = entry.getValue();
            for (ScopeEntry scopeEntry : scopeEntries) {
                if (!(scopeEntry.symbol instanceof BOperatorSymbol)) {
                    compiledSymbols.add(SymbolFactory.getBCompiledSymbol(scopeEntry.symbol, name.getValue()));
                }
            }
        }

        return compiledSymbols;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Symbol> symbol(LinePosition position) {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Diagnostic> diagnostics(TextRange range) {
        return new ArrayList<>();
    }
}
