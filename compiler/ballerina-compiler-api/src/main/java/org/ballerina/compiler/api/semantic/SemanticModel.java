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
package org.ballerina.compiler.api.semantic;

import io.ballerinalang.compiler.syntax.tree.Span;
import io.ballerinalang.compiler.text.TextPosition;
import org.ballerina.compiler.api.model.BCompiledSymbol;
import org.ballerina.compiler.api.semantic.visitors.SymbolsLookupVisitor;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
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
import java.util.stream.Collectors;

/**
 * Semantic model representation of a given syntax tree.
 * 
 * @since 1.3.0
 */
public class SemanticModel extends AbstractSemanticModel {
    
    private BLangPackage bLangPackage;
    private CompilerContext compilerContext;

    public SemanticModel(BLangCompilationUnit compilationUnit, BLangPackage bLangPackage, CompilerContext context) {
        super(compilationUnit);
        this.compilerContext = context;
        this.bLangPackage = bLangPackage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BCompiledSymbol> lookupSymbols(TextPosition textPosition) {
        List<BCompiledSymbol> compiledSymbols = new ArrayList<>();
        SymbolResolver symbolResolver = SymbolResolver.getInstance(this.compilerContext);
        SymbolTable symbolTable = SymbolTable.getInstance(this.compilerContext);
        SymbolEnv symbolEnv = symbolTable.pkgEnvMap.get(this.bLangPackage.symbol);
        SymbolsLookupVisitor lookupVisitor = new SymbolsLookupVisitor(textPosition, symbolEnv);
        Map<Name, List<Scope.ScopeEntry>> scopeSymbols =
                symbolResolver.getAllVisibleInScopeSymbols(lookupVisitor.lookUp(this.compilationUnit));
        
        scopeSymbols.forEach((name, scopeEntries) -> scopeEntries.forEach(scopeEntry -> {
            if (!(scopeEntry.symbol instanceof BOperatorSymbol)) {
                compiledSymbols.add(SymbolFactory.getBCompiledSymbol(scopeEntry.symbol, name.getValue()));
            }
        }));
//        scopeSymbols.values()
//                .forEach(scopeEntries ->
//                        scopeEntries.forEach(scopeEntry -> {
//                            if (!(scopeEntry.symbol instanceof BOperatorSymbol)) {
//                                compiledSymbols.add(SymbolFactory.getBCompiledSymbol(scopeEntry.symbol, scopeEntry.));
//                            }
//                        }));
        
        return compiledSymbols;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BCompiledSymbol lookupSymbol(int position) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BCompiledSymbol> getSymbolByName(TextPosition textPosition, String name) {
        List<BCompiledSymbol> compiledSymbols = this.lookupSymbols(textPosition);
        return compiledSymbols.parallelStream()
                .filter(symbol -> name.equals(symbol.getName()))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Diagnostic> getDiagnostics(Span span, DiagnosticKind kind) {
        return null;
    }
}
